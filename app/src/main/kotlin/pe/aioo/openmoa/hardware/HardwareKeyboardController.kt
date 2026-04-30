package pe.aioo.openmoa.hardware

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import pe.aioo.openmoa.BuildConfig
import pe.aioo.openmoa.IMEMode
import pe.aioo.openmoa.floating.FloatingIndicatorManager
import pe.aioo.openmoa.isKoreanFamily
import pe.aioo.openmoa.settings.SettingsActivity
import pe.aioo.openmoa.settings.SettingsPreferences

class HardwareKeyboardController(
    context: Context,
    private val onToggleLanguageRequested: () -> Unit,
    private val currentImeMode: () -> IMEMode,
    private val onJamoInput: (String) -> Unit = {},
    private val inputConnectionProvider: () -> InputConnection? = { null },
) {
    private val context: Context = context.applicationContext
    private val prefs: SharedPreferences = this.context
        .getSharedPreferences(SettingsPreferences.PREFS_NAME, Context.MODE_PRIVATE)

    private var cachedFloatingEnabled =
        SettingsPreferences.getFloatingIndicatorEnabled(this.context)
    private var cachedHardwareKbConnected = computeHardwareKeyboardConnected()
    private var cachedCapsLockToCtrl = SettingsPreferences.getCapsLockToCtrl(this.context)
    private var cachedTabVimMode = SettingsPreferences.getTabVimMode(this.context)

    private val detector = HardwareKeyShortcutDetector()
    private val capsLockRemapper = CapsLockRemapper().also { it.enabled = cachedCapsLockToCtrl }
    private val tabHoldVimDetector = TabHoldVimDetector().also { it.enabled = cachedTabVimMode }
    private val indicator = FloatingIndicatorManager(this.context)
    private var isInputActive = false

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            SettingsPreferences.KEY_FLOATING_INDICATOR_ENABLED ->
                cachedFloatingEnabled = SettingsPreferences.getFloatingIndicatorEnabled(this.context)
            SettingsPreferences.KEY_HW_CAPSLOCK_TO_CTRL -> {
                cachedCapsLockToCtrl = SettingsPreferences.getCapsLockToCtrl(this.context)
                capsLockRemapper.enabled = cachedCapsLockToCtrl
                capsLockRemapper.reset()
            }
            SettingsPreferences.KEY_HW_TAB_VIM_MODE -> {
                cachedTabVimMode = SettingsPreferences.getTabVimMode(this.context)
                tabHoldVimDetector.enabled = cachedTabVimMode
                tabHoldVimDetector.reset()
            }
            else -> return@OnSharedPreferenceChangeListener
        }
        evaluateAndUpdate()
    }

    init {
        indicator.setOnTapListener { onToggleLanguageRequested() }
        indicator.setOnLongTapListener {
            val intent = Intent(context, SettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    fun onKeyDown(event: KeyEvent): Boolean {
        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "onKeyDown kc=${event.keyCode} shift=${event.isShiftPressed} " +
                    "ctrl=${event.isCtrlPressed} alt=${event.isAltPressed}"
            )
        }

        when (val capsResult = capsLockRemapper.onKeyDown(event.keyCode, event.metaState)) {
            CapsLockRemapper.Result.Consumed -> return true
            is CapsLockRemapper.Result.RewriteAsCtrl -> {
                inputConnectionProvider()?.let { ic ->
                    val now = SystemClock.uptimeMillis()
                    ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, capsResult.keyCode, 0, capsResult.metaState))
                    ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, capsResult.keyCode, 0, capsResult.metaState))
                }
                return true
            }
            CapsLockRemapper.Result.Pass -> Unit
        }

        return handleKeyDownEvent(event)
    }

    private fun handleKeyDownEvent(event: KeyEvent): Boolean {
        val vimAction = tabHoldVimDetector.onKeyDown(
            keyCode = event.keyCode,
            isShift = event.isShiftPressed,
            isRepeat = event.repeatCount > 0,
        )
        if (vimAction != VimAction.PassThrough) {
            inputConnectionProvider()?.let { ic -> VimActionExecutor.execute(vimAction, ic) }
            updateVimIndicator()
            return true
        }

        val shortcut = handle(
            detector.onKeyDown(
                keyCode = event.keyCode,
                isShift = event.isShiftPressed,
                isCtrl = event.isCtrlPressed,
                isAlt = event.isAltPressed,
            )
        )
        if (shortcut) return true
        if (tryHangulInput(event)) return true
        return tryForceLowercase(event)
    }

    private fun tryForceLowercase(event: KeyEvent): Boolean {
        if (!capsLockRemapper.enabled) return false
        if (currentImeMode() == IMEMode.IME_KO) return false
        if (!cachedHardwareKbConnected) return false
        if (event.isCtrlPressed || event.isAltPressed) return false
        if (event.metaState and KeyEvent.META_CAPS_LOCK_ON == 0) return false
        val lower = letterChar(event.keyCode) ?: return false
        val char = if (event.isShiftPressed) lower.uppercaseChar() else lower
        val ic = inputConnectionProvider() ?: return false
        ic.commitText(char.toString(), 1)
        return true
    }

    private fun tryHangulInput(event: KeyEvent): Boolean {
        if (currentImeMode() != IMEMode.IME_KO) return false
        if (!cachedHardwareKbConnected) return false
        if (event.isCtrlPressed || event.isAltPressed) return false
        val char = letterChar(event.keyCode) ?: return false
        val jamo = HardwareDubeolsikMapper.toJamo(char, event.isShiftPressed) ?: return false
        onJamoInput(jamo)
        return true
    }

    private fun letterChar(keyCode: Int): Char? =
        if (keyCode in KeyEvent.KEYCODE_A..KeyEvent.KEYCODE_Z) {
            'a' + (keyCode - KeyEvent.KEYCODE_A)
        } else null

    fun onKeyUp(event: KeyEvent): Boolean {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onKeyUp kc=${event.keyCode}")
        }

        if (capsLockRemapper.onKeyUp(event.keyCode) != CapsLockRemapper.Result.Pass) return true

        val vimAction = tabHoldVimDetector.onKeyUp(event.keyCode)
        if (vimAction != VimAction.PassThrough) {
            inputConnectionProvider()?.let { ic -> VimActionExecutor.execute(vimAction, ic) }
            updateVimIndicator()
            return true
        }

        return handle(detector.onKeyUp(event.keyCode))
    }

    private fun updateVimIndicator() {
        if (!cachedTabVimMode) return
        val mode = when {
            tabHoldVimDetector.isTabHeld && tabHoldVimDetector.isVisualEnteredThisTab -> "V"
            tabHoldVimDetector.isTabHeld -> "F"
            else -> null
        }
        indicator.updateVimMode(mode)
    }

    private fun handle(action: ShortcutAction): Boolean = when (action) {
        ShortcutAction.ConsumeToggleLanguage -> {
            onToggleLanguageRequested()
            true
        }
        ShortcutAction.Pass -> false
    }

    fun onLanguageChanged() {
        if (indicator.isShowing()) {
            indicator.updateLanguage(currentImeMode().isKoreanFamily())
        }
    }

    fun onInputStarted() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onInputStarted")
        if (isInputActive) return
        isInputActive = true
        evaluateAndUpdate()
    }

    fun onInputFinished() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onInputFinished")
        if (!isInputActive) return
        isInputActive = false
        detector.reset()
        capsLockRemapper.reset()
        tabHoldVimDetector.reset()
        evaluateAndUpdate()
    }

    fun onWindowShown() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onWindowShown")
    }

    fun onWindowHidden() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onWindowHidden")
        tabHoldVimDetector.reset()
    }

    fun onConfigurationChanged() {
        cachedHardwareKbConnected = computeHardwareKeyboardConnected()
        detector.reset()
        capsLockRemapper.reset()
        tabHoldVimDetector.reset()
        evaluateAndUpdate()
    }

    fun onDestroy() {
        prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
        indicator.hide()
    }

    private fun evaluateAndUpdate() {
        val hwKb = cachedHardwareKbConnected
        val canDraw = indicator.canShow()
        if (hwKb && isInputActive && !canDraw && cachedFloatingEnabled) {
            OverlayPermissionNotifier.notifyOnce(context)
        }
        if (hwKb && canDraw) {
            OverlayPermissionNotifier.cancel(context)
        }
        if (shouldShow()) {
            indicator.show(currentImeMode().isKoreanFamily())
        } else {
            indicator.hide()
        }
    }

    private fun shouldShow(): Boolean =
        cachedFloatingEnabled &&
            cachedHardwareKbConnected &&
            isInputActive &&
            indicator.canShow()

    private fun computeHardwareKeyboardConnected(): Boolean {
        val cfg = context.resources.configuration
        return cfg.keyboard == Configuration.KEYBOARD_QWERTY &&
            cfg.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO
    }

    companion object {
        private const val TAG = "OpenMoaHwKb"
    }
}
