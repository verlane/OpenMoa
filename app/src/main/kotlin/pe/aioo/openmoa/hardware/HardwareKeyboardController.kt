package pe.aioo.openmoa.hardware

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import android.view.KeyEvent
import pe.aioo.openmoa.BuildConfig
import pe.aioo.openmoa.IMEMode
import pe.aioo.openmoa.floating.FloatingIndicatorManager
import pe.aioo.openmoa.isKoreanFamily
import pe.aioo.openmoa.settings.SettingsPreferences

class HardwareKeyboardController(
    context: Context,
    private val onToggleLanguageRequested: () -> Unit,
    private val currentImeMode: () -> IMEMode,
    private val onJamoInput: (String) -> Unit = {},
) {
    private val context: Context = context.applicationContext
    private val prefs: SharedPreferences = this.context
        .getSharedPreferences(SettingsPreferences.PREFS_NAME, Context.MODE_PRIVATE)

    @Volatile private var cachedFloatingEnabled =
        SettingsPreferences.getFloatingIndicatorEnabled(this.context)
    @Volatile private var cachedLanguageSwitchEnabled =
        SettingsPreferences.getHwLanguageSwitchEnabled(this.context)
    @Volatile private var cachedRAltEnabled =
        SettingsPreferences.getHwRAltEnabled(this.context)
    @Volatile private var cachedShiftSpaceEnabled =
        SettingsPreferences.getHwShiftSpaceEnabled(this.context)
    @Volatile private var cachedHardwareKbConnected = computeHardwareKeyboardConnected()

    private val detector = HardwareKeyShortcutDetector(
        isLanguageSwitchEnabled = { cachedLanguageSwitchEnabled },
        isShiftSpaceEnabled = { cachedShiftSpaceEnabled },
        isRAltEnabled = { cachedRAltEnabled },
    )
    private val indicator = FloatingIndicatorManager(this.context)
    private var isInputActive = false

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            SettingsPreferences.KEY_FLOATING_INDICATOR_ENABLED ->
                cachedFloatingEnabled = SettingsPreferences.getFloatingIndicatorEnabled(this.context)
            SettingsPreferences.KEY_HW_LANGUAGE_SWITCH_ENABLED ->
                cachedLanguageSwitchEnabled = SettingsPreferences.getHwLanguageSwitchEnabled(this.context)
            SettingsPreferences.KEY_HW_RALT_ENABLED ->
                cachedRAltEnabled = SettingsPreferences.getHwRAltEnabled(this.context)
            SettingsPreferences.KEY_HW_SHIFT_SPACE_ENABLED ->
                cachedShiftSpaceEnabled = SettingsPreferences.getHwShiftSpaceEnabled(this.context)
            else -> return@OnSharedPreferenceChangeListener
        }
        evaluateAndUpdate()
    }

    init {
        indicator.setOnTapListener { onToggleLanguageRequested() }
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
        val shortcut = handle(
            detector.onKeyDown(
                keyCode = event.keyCode,
                isShift = event.isShiftPressed,
                isCtrl = event.isCtrlPressed,
                isAlt = event.isAltPressed,
            )
        )
        if (shortcut) return true
        return tryHangulInput(event)
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
        return handle(detector.onKeyUp(event.keyCode))
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
        evaluateAndUpdate()
    }

    fun onWindowShown() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onWindowShown")
    }

    fun onWindowHidden() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onWindowHidden")
    }

    fun onConfigurationChanged() {
        cachedHardwareKbConnected = computeHardwareKeyboardConnected()
        detector.reset()
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
