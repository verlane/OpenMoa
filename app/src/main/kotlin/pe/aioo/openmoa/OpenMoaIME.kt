package pe.aioo.openmoa

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.graphics.drawable.Icon
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.inputmethod.InputMethodManager
import android.text.InputType
import android.util.Size
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InlineSuggestionsRequest
import android.view.inputmethod.InlineSuggestionsResponse
import android.widget.inline.InlinePresentationSpec
import androidx.autofill.inline.UiVersions
import androidx.autofill.inline.common.ImageViewStyle
import androidx.autofill.inline.common.TextViewStyle
import androidx.autofill.inline.common.ViewStyle
import androidx.autofill.inline.v1.InlineSuggestionUi
import androidx.core.content.ContextCompat
import androidx.core.view.isEmpty
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import pe.aioo.openmoa.config.Config
import pe.aioo.openmoa.suggestion.KoreanSuggestionEngine
import pe.aioo.openmoa.suggestion.SuggestionEngine
import pe.aioo.openmoa.suggestion.UserWordStore
import pe.aioo.openmoa.suggestion.WordTokenizer
import pe.aioo.openmoa.config.HangulInputMode
import pe.aioo.openmoa.view.feedback.KeyFeedbackPlayer
import pe.aioo.openmoa.config.KeyboardSkin
import pe.aioo.openmoa.config.OneHandMode
import pe.aioo.openmoa.databinding.OpenMoaImeBinding
import pe.aioo.openmoa.hangul.HangulAssembler
import pe.aioo.openmoa.hotstring.HotstringMatcher
import pe.aioo.openmoa.hotstring.HotstringRepository
import pe.aioo.openmoa.clipboard.ClipboardEditActivity
import pe.aioo.openmoa.clipboard.ClipboardRepository
import pe.aioo.openmoa.settings.SettingsActivity
import pe.aioo.openmoa.settings.SettingsPreferences
import pe.aioo.openmoa.view.keyboardview.*
import pe.aioo.openmoa.view.keyboardview.qwerty.QuertyView
import pe.aioo.openmoa.view.message.SpecialKey
import pe.aioo.openmoa.view.skin.SkinApplier
import java.io.Serializable
import kotlin.math.roundToInt

class OpenMoaIME : InputMethodService(), KoinComponent {

    private lateinit var binding: OpenMoaImeBinding
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var keyboardViews: Map<IMEMode, View>
    private val config: Config by inject()
    private val feedbackPlayer: KeyFeedbackPlayer by inject()
    private val suggestionEngine: SuggestionEngine by inject()
    private val koreanSuggestionEngine: KoreanSuggestionEngine by inject()
    private val userWordStore: UserWordStore by inject(named("en"))
    private val koreanUserWordStore: UserWordStore by inject(named("ko"))
    private val hangulAssembler = HangulAssembler()
    private var imeMode = IMEMode.IME_KO
    private var previousImeMode = IMEMode.IME_KO
    private var composingText = ""
    private var lastSpaceTime = 0L
    private var lastAppliedSkin: KeyboardSkin = KeyboardSkin.DEFAULT
    private var isPasswordField = false
    private var isHotstringEnabled = false
    private var lastHotstringTrigger: String? = null
    private var lastHotstringExpansion: String? = null
    private var hotstringBuffer = ""
    private var lastLearnedWord: String? = null
    private var lastLearnedIsKo: Boolean = false
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var suggestionJob: Job? = null
    private var isSuggestionBarActive = false
    private var isTextSelected = false
    private var isClipboardPanelVisible = false
    private var suggestionLongPressPopup: android.widget.PopupWindow? = null
    private var currentSuggestions: List<String> = emptyList()
    private var currentHotstringExpansions: Set<String> = emptySet()
    private var clipboardManager: ClipboardManager? = null
    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        if (!isPasswordField && config.clipboardEnabled) {
            getClipboardText()?.let {
                ClipboardRepository.add(this, it)
                if (isClipboardPanelVisible && this::binding.isInitialized) {
                    binding.clipboardPanel.refresh(this)
                }
            }
        }
    }

    private fun finishComposing() {
        currentInputConnection?.finishComposingText()
        hangulAssembler.clear()
        composingText = ""
    }

    private fun showClipboardPanel() {
        if (!this::binding.isInitialized) return
        if (!config.clipboardEnabled) return
        isClipboardPanelVisible = true
        finishComposing()
        deactivateSuggestionBar()
        if (!isPasswordField) {
            getClipboardText()?.let { ClipboardRepository.add(this, it) }
        }
        ClipboardRepository.purgeExpired(this)
        binding.clipboardPanel.refresh(this)
        binding.clipboardPanel.visibility = View.VISIBLE
        binding.keyboardFrameLayout.visibility = View.INVISIBLE
    }

    private fun hideClipboardPanel() {
        if (!this::binding.isInitialized) return
        isClipboardPanelVisible = false
        binding.clipboardPanel.visibility = View.GONE
        binding.keyboardFrameLayout.visibility = View.VISIBLE
    }

    private fun refreshSuggestions(prefix: String) {
        if (!this::binding.isInitialized) return
        val isEnMode = imeMode == IMEMode.IME_EN
        val isKoMode = imeMode == IMEMode.IME_KO
        val enEnabled = config.wordSuggestionEnabled && isEnMode
        val koEnabled = config.koreanWordSuggestionEnabled && isKoMode
        if (isPasswordField || (!enEnabled && !koEnabled)) {
            deactivateSuggestionBar()
            return
        }
        if (prefix.isEmpty()) {
            showIdleSuggestionBar(isTextSelected)
            return
        }
        suggestionJob?.cancel()
        val capturedIsKoMode = isKoMode
        val capturedComposing = composingText
        val capturedHotstringBuffer = hotstringBuffer
        val capturedUnresolved = if (isKoMode) hangulAssembler.getUnresolved() else null
        suggestionJob = serviceScope.launch {
            // 첫 활성화 시 키 미리보기 dismiss 딜레이 완료 후 표시
            if (config.keyPreviewEnabled && !isSuggestionBarActive) {
                delay(250)
            }
            if (composingText != capturedComposing) return@launch
            val minCount = config.minLearnCount
            val words = if (capturedIsKoMode) {
                koreanSuggestionEngine.suggest(capturedComposing, capturedUnresolved, minCount)
            } else {
                suggestionEngine.suggest(prefix, minCount)
            }
            if (!this@OpenMoaIME::binding.isInitialized) return@launch
            val triggerToMatch = if (capturedIsKoMode) capturedComposing else capturedHotstringBuffer
            val hotstringFirst = HotstringRepository.getCached(this@OpenMoaIME)
                .filter { it.enabled && it.trigger == triggerToMatch }
                .map { it.expansion }
            val wordsSet = words.toSet()
            val hotstringSet = hotstringFirst.toSet()
            val finalWords = (hotstringFirst.filter { it !in wordsSet } + words)
                .take(config.maxSuggestionCount)
            if (finalWords.isEmpty()) {
                showIdleSuggestionBar(isTextSelected)
            } else {
                isSuggestionBarActive = true
                currentSuggestions = finalWords
                currentHotstringExpansions = hotstringSet
                binding.wordSuggestionBar.setSuggestions(finalWords, hotstringSet)
                binding.wordSuggestionBar.visibility = View.VISIBLE
            }
        }
    }

    private fun deactivateSuggestionBar() {
        if (!this::binding.isInitialized) return
        isSuggestionBarActive = false
        binding.wordSuggestionBar.setSuggestions(emptyList())
        binding.wordSuggestionBar.visibility = View.GONE
    }

    private fun showIdleSuggestionBar(hasSelection: Boolean) {
        if (!this::binding.isInitialized) return
        if (!isSuggestionBarActive) return
        if (hasSelection) {
            binding.wordSuggestionBar.showSelectionActions(
                onCut = { sendKeyDownUpEvent(KeyEvent.KEYCODE_X, KeyEvent.META_CTRL_ON) },
                onCopy = { sendKeyDownUpEvent(KeyEvent.KEYCODE_C, KeyEvent.META_CTRL_ON) },
            )
        } else {
            val clipText = getClipboardText()
            if (clipText != null) {
                binding.wordSuggestionBar.showClipboard(clipText) { text ->
                    finishComposing()
                    currentInputConnection?.commitText(text, 1)
                    deactivateSuggestionBar()
                }
            } else {
                binding.wordSuggestionBar.showEmpty()
            }
        }
        binding.wordSuggestionBar.visibility = View.VISIBLE
    }

    private fun getClipboardText(): String? {
        return try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val item = clipboard?.primaryClip?.getItemAt(0) ?: return null
            item.coerceToText(this)?.toString()?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    private fun onSuggestionPicked(word: String, isHotstring: Boolean) {
        if (isHotstring) {
            if (imeMode == IMEMode.IME_EN) {
                val trigger = hotstringBuffer
                val currentComposingLen = composingText.length
                val committedPartLen = (trigger.length - currentComposingLen).coerceAtLeast(0)
                hangulAssembler.clear()
                composingText = ""
                currentInputConnection?.beginBatchEdit()
                try {
                    currentInputConnection?.finishComposingText()
                    currentInputConnection?.deleteSurroundingText(committedPartLen + currentComposingLen, 0)
                    currentInputConnection?.commitText(word, 1)
                } finally {
                    currentInputConnection?.endBatchEdit()
                }
            } else {
                hangulAssembler.clear()
                composingText = ""
                currentInputConnection?.commitText(word, 1)
            }
            hotstringBuffer = ""
        } else if (imeMode == IMEMode.IME_KO) {
            koreanUserWordStore.increment(word)
            hangulAssembler.clear()
            composingText = ""
            currentInputConnection?.commitText(word + " ", 1)
        } else {
            userWordStore.increment(word)
            composingText = ""
            hangulAssembler.clear()
            currentInputConnection?.commitText(word + " ", 1)
        }
        isTextSelected = false
        showIdleSuggestionBar(false)
        setShiftAutomatically()
    }

    private fun onSuggestionLongClick(word: String) {
        val isKo = imeMode == IMEMode.IME_KO
        val store = if (isKo) koreanUserWordStore else userWordStore
        val anchorView = binding.wordSuggestionBar

        val menuView = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
            val dp8 = (8 * resources.displayMetrics.density).toInt()
            setPadding(dp8, dp8, dp8, dp8)
        }
        suggestionLongPressPopup?.dismiss()
        val popup = android.widget.PopupWindow(
            menuView,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            false,
        )
        suggestionLongPressPopup = popup

        fun addItem(label: String, action: () -> Unit) {
            val dp12 = (12 * resources.displayMetrics.density).toInt()
            val tv = android.widget.TextView(this).apply {
                text = label
                textSize = 15f
                setPadding(dp12, dp12, dp12, dp12)
                background = obtainStyledAttributes(
                    intArrayOf(android.R.attr.selectableItemBackground)
                ).let { it.getDrawable(0).also { _ -> it.recycle() } }
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    popup.dismiss()
                    action()
                }
            }
            menuView.addView(tv)
        }

        addItem(getString(R.string.suggestion_long_click_remove)) {
            store.remove(word)
            refreshAfterSuggestionEdit(word)
        }
        addItem(getString(R.string.suggestion_long_click_blacklist)) {
            store.addToBlacklist(word)
            refreshAfterSuggestionEdit(word)
        }

        menuView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val xoff = anchorView.width / 2 - menuView.measuredWidth / 2
        val yoff = -(menuView.measuredHeight + anchorView.height)
        popup.showAsDropDown(anchorView, xoff, yoff)
    }

    private fun refreshAfterSuggestionEdit(removedWord: String) {
        val updated = currentSuggestions.filter { it != removedWord }
        currentSuggestions = updated
        if (updated.isNotEmpty()) {
            binding.wordSuggestionBar.setSuggestions(updated, currentHotstringExpansions)
        } else if (composingText.isNotEmpty()) {
            refreshSuggestions(composingText)
        } else {
            showIdleSuggestionBar(isTextSelected)
        }
    }

    private inline fun <reified T : Serializable> getKeyFromIntent(intent: Intent): T? {
        val extra = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_NAME, Serializable::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_NAME)
        }
        return extra as? T
    }

    private fun sendKeyDownUpEvent(keyCode: Int, metaState: Int = 0, withShift: Boolean = false) {
        var eventTime = SystemClock.uptimeMillis()
        if (withShift) {
            currentInputConnection.sendKeyEvent(
                KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT)
            )
        }
        currentInputConnection.sendKeyEvent(
            KeyEvent(
                eventTime,
                eventTime,
                KeyEvent.ACTION_DOWN,
                keyCode,
                0,
                metaState,
                0,
                0,
                KeyEvent.FLAG_SOFT_KEYBOARD or KeyEvent.FLAG_KEEP_TOUCH_MODE,
            )
        )
        eventTime = SystemClock.uptimeMillis()
        currentInputConnection.sendKeyEvent(
            KeyEvent(
                eventTime,
                eventTime,
                KeyEvent.ACTION_UP,
                keyCode,
                0,
                0,
                0,
                0,
                KeyEvent.FLAG_SOFT_KEYBOARD or KeyEvent.FLAG_KEEP_TOUCH_MODE,
            )
        )
        if (withShift) {
            currentInputConnection.sendKeyEvent(
                KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT)
            )
        }
    }

    private fun isTextEmpty(): Boolean {
        val text = currentInputConnection.getExtractedText(ExtractedTextRequest(), 0)
        return (text?.text ?: "") == ""
    }

    override fun onCreate() {
        super.onCreate()
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                suggestionLongPressPopup?.dismiss()
                val key = getKeyFromIntent<String>(intent)
                    ?: getKeyFromIntent<SpecialKey>(intent)
                    ?: return
                val beforeComposingText = composingText
                when (key) {
                    is SpecialKey -> {
                        // Process for special key
                        when (key) {
                            SpecialKey.BACKSPACE -> {
                                lastLearnedWord?.let {
                                    if (lastLearnedIsKo) koreanUserWordStore.decrement(it)
                                    else userWordStore.decrement(it)
                                    lastLearnedWord = null
                                }
                                val undoTrigger = lastHotstringTrigger
                                val undoExpansion = lastHotstringExpansion
                                if (undoTrigger != null && undoExpansion != null) {
                                    lastHotstringTrigger = null
                                    lastHotstringExpansion = null
                                    hotstringBuffer = ""
                                    finishComposing()
                                    currentInputConnection.deleteSurroundingText(undoExpansion.length, 0)
                                    currentInputConnection.commitText(undoTrigger, 1)
                                    return@onReceive
                                }
                                hotstringBuffer = hotstringBuffer.dropLast(1)
                                val unresolved = hangulAssembler.getUnresolved()
                                if (unresolved != null) {
                                    composingText = composingText.substring(
                                        0, composingText.length - unresolved.length
                                    )
                                    hangulAssembler.removeLastJamo()
                                    hangulAssembler.getUnresolved()?.let {
                                        composingText += it
                                    }
                                } else {
                                    if (composingText.isEmpty()) {
                                        sendKeyDownUpEvent(KeyEvent.KEYCODE_DEL)
                                    } else {
                                        composingText = composingText.substring(
                                            0, composingText.lastIndex
                                        )
                                    }
                                }
                            }
                            SpecialKey.ENTER -> {
                                hotstringBuffer = ""
                                finishComposing()
                                val action = currentInputEditorInfo.imeOptions and (
                                    EditorInfo.IME_MASK_ACTION or
                                    EditorInfo.IME_FLAG_NO_ENTER_ACTION
                                )
                                when (action) {
                                    EditorInfo.IME_ACTION_GO,
                                    EditorInfo.IME_ACTION_NEXT,
                                    EditorInfo.IME_ACTION_SEARCH,
                                    EditorInfo.IME_ACTION_SEND,
                                    EditorInfo.IME_ACTION_DONE -> {
                                        currentInputConnection.performEditorAction(action)
                                    }
                                    else -> {
                                        sendKeyDownUpEvent(KeyEvent.KEYCODE_ENTER)
                                    }
                                }
                            }
                            SpecialKey.LANGUAGE -> {
                                setKeyboard(
                                    when (imeMode) {
                                        IMEMode.IME_KO -> IMEMode.IME_EN
                                        IMEMode.IME_EN -> IMEMode.IME_KO
                                        IMEMode.IME_KO_PUNCTUATION,
                                        IMEMode.IME_KO_NUMBER,
                                        IMEMode.IME_KO_ARROW,
                                        IMEMode.IME_KO_PHONE,
                                        IMEMode.IME_EMOJI -> IMEMode.IME_KO
                                        IMEMode.IME_EN_PUNCTUATION,
                                        IMEMode.IME_EN_NUMBER,
                                        IMEMode.IME_EN_ARROW,
                                        IMEMode.IME_EN_PHONE -> IMEMode.IME_EN
                                    }
                                )
                            }
                            SpecialKey.HANJA_NUMBER_PUNCTUATION -> {
                                setKeyboard(
                                    when (imeMode) {
                                        IMEMode.IME_KO,
                                        IMEMode.IME_KO_NUMBER,
                                        IMEMode.IME_KO_ARROW,
                                        IMEMode.IME_KO_PHONE,
                                        IMEMode.IME_EMOJI -> IMEMode.IME_KO_PUNCTUATION
                                        IMEMode.IME_EN,
                                        IMEMode.IME_EN_NUMBER,
                                        IMEMode.IME_EN_ARROW,
                                        IMEMode.IME_EN_PHONE -> IMEMode.IME_EN_PUNCTUATION
                                        IMEMode.IME_KO_PUNCTUATION -> IMEMode.IME_KO_NUMBER
                                        IMEMode.IME_EN_PUNCTUATION -> IMEMode.IME_EN_NUMBER
                                    }
                                )
                            }
                            SpecialKey.ARROW -> {
                                setKeyboard(
                                    when (imeMode) {
                                        IMEMode.IME_KO,
                                        IMEMode.IME_KO_NUMBER,
                                        IMEMode.IME_KO_PUNCTUATION,
                                        IMEMode.IME_KO_ARROW,
                                        IMEMode.IME_KO_PHONE,
                                        IMEMode.IME_EMOJI -> IMEMode.IME_KO_ARROW
                                        IMEMode.IME_EN,
                                        IMEMode.IME_EN_NUMBER,
                                        IMEMode.IME_EN_PUNCTUATION,
                                        IMEMode.IME_EN_ARROW,
                                        IMEMode.IME_EN_PHONE -> IMEMode.IME_EN_ARROW
                                    }
                                )
                            }
                            SpecialKey.ARROW_UP -> {
                                hotstringBuffer = ""
                                if (!isTextEmpty()) {
                                    sendKeyDownUpEvent(KeyEvent.KEYCODE_DPAD_UP)
                                }
                            }
                            SpecialKey.ARROW_LEFT -> {
                                hotstringBuffer = ""
                                if (!isTextEmpty()) {
                                    sendKeyDownUpEvent(KeyEvent.KEYCODE_DPAD_LEFT)
                                }
                            }
                            SpecialKey.ARROW_RIGHT -> {
                                hotstringBuffer = ""
                                if (!isTextEmpty()) {
                                    sendKeyDownUpEvent(KeyEvent.KEYCODE_DPAD_RIGHT)
                                }
                            }
                            SpecialKey.ARROW_DOWN -> {
                                hotstringBuffer = ""
                                if (!isTextEmpty()) {
                                    sendKeyDownUpEvent(KeyEvent.KEYCODE_DPAD_DOWN)
                                }
                            }
                            SpecialKey.COPY_ALL -> {
                                sendKeyDownUpEvent(KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON)
                                sendKeyDownUpEvent(KeyEvent.KEYCODE_C, KeyEvent.META_CTRL_ON)
                            }
                            SpecialKey.COPY -> {
                                sendKeyDownUpEvent(KeyEvent.KEYCODE_C, KeyEvent.META_CTRL_ON)
                            }
                            SpecialKey.CUT_ALL -> {
                                hotstringBuffer = ""
                                sendKeyDownUpEvent(KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON)
                                sendKeyDownUpEvent(KeyEvent.KEYCODE_X, KeyEvent.META_CTRL_ON)
                            }
                            SpecialKey.CUT -> {
                                hotstringBuffer = ""
                                sendKeyDownUpEvent(KeyEvent.KEYCODE_X, KeyEvent.META_CTRL_ON)
                            }
                            SpecialKey.HOME -> {
                                hotstringBuffer = ""
                                sendKeyDownUpEvent(
                                    KeyEvent.KEYCODE_MOVE_HOME, KeyEvent.META_CTRL_ON
                                )
                            }
                            SpecialKey.END -> {
                                hotstringBuffer = ""
                                sendKeyDownUpEvent(
                                    KeyEvent.KEYCODE_MOVE_END, KeyEvent.META_CTRL_ON
                                )
                            }
                            SpecialKey.DELETE -> {
                                hotstringBuffer = hotstringBuffer.dropLast(1)
                                sendKeyDownUpEvent(KeyEvent.KEYCODE_DEL)
                            }
                            SpecialKey.PASTE -> {
                                hotstringBuffer = ""
                                sendKeyDownUpEvent(KeyEvent.KEYCODE_V, KeyEvent.META_CTRL_ON)
                            }
                            SpecialKey.SELECT_ALL -> {
                                hotstringBuffer = ""
                                sendKeyDownUpEvent(KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON)
                            }
                            SpecialKey.SELECT_ARROW_UP -> {
                                if (!isTextEmpty()) {
                                    sendKeyDownUpEvent(KeyEvent.KEYCODE_DPAD_UP, withShift = true)
                                }
                            }
                            SpecialKey.SELECT_ARROW_LEFT -> {
                                if (!isTextEmpty()) {
                                    sendKeyDownUpEvent(KeyEvent.KEYCODE_DPAD_LEFT, withShift = true)
                                }
                            }
                            SpecialKey.SELECT_ARROW_RIGHT -> {
                                if (!isTextEmpty()) {
                                    sendKeyDownUpEvent(
                                        KeyEvent.KEYCODE_DPAD_RIGHT, withShift = true
                                    )
                                }
                            }
                            SpecialKey.SELECT_ARROW_DOWN -> {
                                if (!isTextEmpty()) {
                                    sendKeyDownUpEvent(KeyEvent.KEYCODE_DPAD_DOWN, withShift = true)
                                }
                            }
                            SpecialKey.SELECT_END -> {
                                sendKeyDownUpEvent(
                                    KeyEvent.KEYCODE_MOVE_END, KeyEvent.META_CTRL_ON, true
                                )
                            }
                            SpecialKey.SELECT_HOME -> {
                                sendKeyDownUpEvent(
                                    KeyEvent.KEYCODE_MOVE_HOME, KeyEvent.META_CTRL_ON, true
                                )
                            }
                            SpecialKey.EMOJI -> {
                                if (imeMode == IMEMode.IME_EMOJI) {
                                    setKeyboard(previousImeMode)
                                } else {
                                    previousImeMode = imeMode
                                    setKeyboard(IMEMode.IME_EMOJI)
                                }
                            }
                            SpecialKey.OPEN_SETTINGS -> {
                                startActivity(
                                    Intent(this@OpenMoaIME, SettingsActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                )
                            }
                            SpecialKey.SHOW_IME_PICKER -> {
                                finishComposing()
                                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                imm?.showInputMethodPicker()
                            }
                            SpecialKey.CLIPBOARD_OPEN -> showClipboardPanel()
                        }
                    }
                    is String -> {
                        if (key.matches(Regex("^[A-Za-z]$"))) {
                            // Process for Alphabet key
                            composingText += key
                            hotstringBuffer = (hotstringBuffer + key).takeLast(50)
                        } else if (key.matches(HangulAssembler.JAMO_REGEX)) {
                            // Process for Jamo key
                            hangulAssembler.getUnresolved()?.let {
                                composingText = composingText.substring(
                                    0, composingText.length - it.length
                                )
                            }
                            hangulAssembler.appendJamo(key)?.let {
                                composingText += it
                            }
                            hangulAssembler.getUnresolved()?.let {
                                composingText += it
                            }
                            hotstringBuffer = (hotstringBuffer + key).takeLast(50)
                        } else {
                            // Process for another key
                            lastHotstringTrigger = null
                            lastHotstringExpansion = null
                            lastLearnedWord = null
                            if (key == " " && !isPasswordField) {
                                if (imeMode == IMEMode.IME_EN && composingText.isNotEmpty()) {
                                    WordTokenizer.extractEnglish(composingText)?.let {
                                        userWordStore.increment(it)
                                        lastLearnedWord = it
                                        lastLearnedIsKo = false
                                    }
                                } else if (imeMode == IMEMode.IME_KO && composingText.isNotEmpty()) {
                                    WordTokenizer.extractKorean(composingText)?.let {
                                        koreanUserWordStore.increment(it)
                                        lastLearnedWord = it
                                        lastLearnedIsKo = true
                                    }
                                }
                            }
                            if (key != " ") {
                                hotstringBuffer = (hotstringBuffer + key).takeLast(50)
                            } else {
                                hotstringBuffer = ""
                            }
                            finishComposing()
                            if (key == " ") {
                                if (tryExpandHotstring()) {
                                    lastSpaceTime = 0L
                                } else {
                                    val autoEnabled = SettingsPreferences.getAutoSpacePeriod(this@OpenMoaIME)
                                    val now = SystemClock.elapsedRealtime()
                                    if (autoEnabled && now - lastSpaceTime < 1000L &&
                                        currentInputConnection.getTextBeforeCursor(1, 0) == " ") {
                                        currentInputConnection.deleteSurroundingText(1, 0)
                                        currentInputConnection.commitText(". ", 1)
                                        lastSpaceTime = 0L
                                    } else {
                                        currentInputConnection.commitText(key, 1)
                                        lastSpaceTime = if (autoEnabled) now else 0L
                                    }
                                }
                            } else {
                                currentInputConnection.commitText(key, 1)
                                lastSpaceTime = 0L
                            }
                        }
                    }
                }
                if (beforeComposingText != composingText) {
                    currentInputConnection.setComposingText(composingText, 1)
                }
                if (composingText.isNotEmpty()) {
                    refreshSuggestions(composingText)
                } else {
                    suggestionJob?.cancel()
                    showIdleSuggestionBar(false)
                }
                setShiftAutomatically()
            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(
            broadcastReceiver, IntentFilter(INTENT_ACTION)
        )
    }

    private fun setKeyboard(mode: IMEMode) {
        finishComposing()
        hotstringBuffer = ""
        deactivateSuggestionBar()
        keyboardViews[mode]?.let {
            when (it) {
                is PunctuationView -> it.setPageOrNextPage(0)
                is PhoneView -> it.setPageOrNextPage(0)
                is ArrowView -> {
                    it.setSelectingOrToggleSelecting(false)
                    it.refreshOneHandMode()
                }
            }
            binding.keyboardFrameLayout.setKeyboardView(it)
        }
        imeMode = mode
    }

    private fun setShiftAutomatically() {
        if (!config.autoCapitalizeEnglish) return
        keyboardViews[imeMode]?.let { view ->
            if (view is QuertyView) {
                currentInputConnection?.let { inputConnection ->
                    view.setShiftEnabledAutomatically(
                        inputConnection.getCursorCapsMode(currentInputEditorInfo.inputType) != 0
                    )
                }
            }
        }
    }

    private fun returnFromNonStringKeyboard() {
        when (imeMode) {
            IMEMode.IME_KO,
            IMEMode.IME_KO_PUNCTUATION,
            IMEMode.IME_KO_NUMBER,
            IMEMode.IME_KO_ARROW,
            IMEMode.IME_KO_PHONE,
            IMEMode.IME_EMOJI -> setKeyboard(IMEMode.IME_KO)
            IMEMode.IME_EN,
            IMEMode.IME_EN_PUNCTUATION,
            IMEMode.IME_EN_NUMBER,
            IMEMode.IME_EN_ARROW,
            IMEMode.IME_EN_PHONE -> setKeyboard(IMEMode.IME_EN)
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateInputView(): View {
        super.onCreateInputView()
        lastAppliedSkin = SettingsPreferences.getKeyboardSkin(this)
        keyboardViews = buildKeyboardViews()
        val view = layoutInflater.inflate(R.layout.open_moa_ime, null)
        binding = OpenMoaImeBinding.bind(view)
        binding.wordSuggestionBar.onPick = ::onSuggestionPicked
        binding.wordSuggestionBar.onWordLongClick = { word -> onSuggestionLongClick(word) }
        binding.clipboardPanel.onPaste = { text ->
            finishComposing()
            currentInputConnection?.commitText(text, 1)
            hideClipboardPanel()
        }
        binding.clipboardPanel.onClose = { hideClipboardPanel() }
        binding.clipboardPanel.onEdit = { entry ->
            hideClipboardPanel()
            startActivity(
                Intent(this, ClipboardEditActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(ClipboardEditActivity.EXTRA_ENTRY_ID, entry.id)
                    putExtra(ClipboardEditActivity.EXTRA_ENTRY_TEXT, entry.text)
                }
            )
        }
        binding.clipboardPanel.onAddHotstring = { entry ->
            hideClipboardPanel()
            startActivity(
                Intent(this, pe.aioo.openmoa.settings.HotstringListActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(pe.aioo.openmoa.settings.HotstringListActivity.EXTRA_EXPANSION, entry.text)
                }
            )
        }
        binding.clipboardPanel.onOpenUrl = { url ->
            runCatching {
                val normalizedUrl = if (Uri.parse(url).scheme == null) "https://$url" else url
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(normalizedUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }.onFailure { android.util.Log.w("OpenMoaIME", "Failed to open URL: $url", it) }
        }
        binding.clipboardPanel.onOpenEmail = { email ->
            runCatching {
                startActivity(
                    Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }.onFailure { android.util.Log.w("OpenMoaIME", "Failed to open email: $email", it) }
        }
        applyKeyboardLayout()
        setKeyboard(imeMode)
        return view
    }

    private fun buildKeyboardViews(): MutableMap<IMEMode, View> {
        val punctuationView = PunctuationView(this)
        val numberView = NumberView(this)
        val arrowView = ArrowView(this)
        val phoneView = PhoneView(this)
        val emojiView = EmojiView(this)
        return mutableMapOf(
            IMEMode.IME_KO to OpenMoaView(this).also { it.jaumPreviewResolver = hangulAssembler::previewWithAppended },
            IMEMode.IME_EN to QuertyView(this),
            IMEMode.IME_KO_PUNCTUATION to punctuationView,
            IMEMode.IME_EN_PUNCTUATION to punctuationView,
            IMEMode.IME_KO_NUMBER to numberView,
            IMEMode.IME_EN_NUMBER to numberView,
            IMEMode.IME_KO_ARROW to arrowView,
            IMEMode.IME_EN_ARROW to arrowView,
            IMEMode.IME_KO_PHONE to phoneView,
            IMEMode.IME_EN_PHONE to phoneView,
            IMEMode.IME_EMOJI to emojiView,
        )
    }

    private fun refreshSkinIfNeeded() {
        val currentSkin = SettingsPreferences.getKeyboardSkin(this)
        if (currentSkin == lastAppliedSkin) return
        lastAppliedSkin = currentSkin
        keyboardViews = buildKeyboardViews()
        setKeyboard(imeMode)
    }

    private fun refreshOpenMoaViewIfNeeded() {
        val savedMode = SettingsPreferences.getHangulInputMode(this)
        val currentView = keyboardViews[IMEMode.IME_KO] as? OpenMoaView ?: return
        val currentIsMoakey = currentView.isMoakeyMode
        val currentShowsMoeumKey = currentView.moeumKeyVisible
        if (currentIsMoakey != savedMode.isMoakeyLayout ||
            currentShowsMoeumKey != savedMode.showsMoeumKey) {
            keyboardViews = keyboardViews + (IMEMode.IME_KO to OpenMoaView(this).also { it.jaumPreviewResolver = hangulAssembler::previewWithAppended })
        }
    }

    private fun tryExpandHotstring(): Boolean {
        if (!isHotstringEnabled) return false
        if (isPasswordField) return false
        val ic = currentInputConnection ?: return false
        val rules = HotstringRepository.getCached(this)
        val maxLen = HotstringMatcher.bufferLengthNeeded(rules)
        if (maxLen == 0) return false

        // Chrome의 인라인 자동완성은 selectionStart(사용자 입력 끝) ~ selectionEnd(완성 텍스트 끝)를
        // 선택 상태로 표시한다. 이 경우 getTextBeforeCursor가 완성 텍스트까지 포함해 반환하여
        // 트리거 매칭이 실패하므로, selectionStart 이전 텍스트로만 매칭한다.
        val et: ExtractedText? = ic.getExtractedText(ExtractedTextRequest(), 0)
        val selStart = et?.selectionStart ?: 0
        val selEnd = et?.selectionEnd ?: 0
        val etText = et?.text
        // et.text가 null이면 선택 범위를 신뢰할 수 없으므로 0으로 처리
        val inlineCompletionLen = if (selEnd > selStart && etText != null) selEnd - selStart else 0

        val buffer = if (inlineCompletionLen > 0 && etText != null) {
            val localSelStart = (selStart - et!!.startOffset).coerceAtLeast(0)
            etText.substring(maxOf(0, localSelStart - maxLen), localSelStart)
        } else {
            ic.getTextBeforeCursor(maxLen, 0)?.toString() ?: return false
        }

        val match = HotstringMatcher.findMatch(buffer, rules) ?: return false

        ic.beginBatchEdit()
        try {
            if (inlineCompletionLen > 0) {
                // 커서가 selStart 쪽에 있을 수 있으므로 selEnd로 명시적 이동 후 삭제
                ic.setSelection(selEnd, selEnd)
            }
            ic.deleteSurroundingText(match.trigger.length + inlineCompletionLen, 0)
            ic.commitText(match.expansion, 1)
            lastHotstringTrigger = match.trigger
            lastHotstringExpansion = match.expansion
        } finally {
            ic.endBatchEdit()
        }
        return true
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        suggestionLongPressPopup?.dismiss()
        finishComposing()
        hotstringBuffer = ""
        lastLearnedWord = null
        lastLearnedIsKo = false
        isHotstringEnabled = SettingsPreferences.getHotstringEnabled(this)
        val inputType = (info?.inputType ?: 0)
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        isPasswordField = variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
            variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
        clipboardManager?.removePrimaryClipChangedListener(clipboardListener)
        if (!isPasswordField && config.clipboardEnabled) {
            clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboardManager?.addPrimaryClipChangedListener(clipboardListener)
        }
        refreshSkinIfNeeded()
        refreshOpenMoaViewIfNeeded()
        (keyboardViews[IMEMode.IME_KO] as? OpenMoaView)?.refreshQuickPhraseBadges()
        (keyboardViews[IMEMode.IME_KO] as? OpenMoaView)?.refreshUserCharLabels()
        applyKeyboardLayout()
        when ((info?.inputType ?: 0) and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER -> {
                setKeyboard(
                    when(imeMode) {
                        IMEMode.IME_KO,
                        IMEMode.IME_KO_PUNCTUATION,
                        IMEMode.IME_KO_NUMBER,
                        IMEMode.IME_KO_ARROW,
                        IMEMode.IME_KO_PHONE,
                        IMEMode.IME_EMOJI -> IMEMode.IME_KO_NUMBER
                        IMEMode.IME_EN,
                        IMEMode.IME_EN_PUNCTUATION,
                        IMEMode.IME_EN_NUMBER,
                        IMEMode.IME_EN_ARROW,
                        IMEMode.IME_EN_PHONE -> IMEMode.IME_EN_NUMBER
                    }
                )
            }
            InputType.TYPE_CLASS_PHONE -> {
                setKeyboard(
                    when(imeMode) {
                        IMEMode.IME_KO,
                        IMEMode.IME_KO_PUNCTUATION,
                        IMEMode.IME_KO_NUMBER,
                        IMEMode.IME_KO_ARROW,
                        IMEMode.IME_KO_PHONE,
                        IMEMode.IME_EMOJI -> IMEMode.IME_KO_PHONE
                        IMEMode.IME_EN,
                        IMEMode.IME_EN_PUNCTUATION,
                        IMEMode.IME_EN_NUMBER,
                        IMEMode.IME_EN_ARROW,
                        IMEMode.IME_EN_PHONE -> IMEMode.IME_EN_PHONE
                    }
                )
            }
            else -> {
                setShiftAutomatically()
                returnFromNonStringKeyboard()
            }
        }
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart,
            oldSelEnd,
            newSelStart,
            newSelEnd,
            candidatesStart,
            candidatesEnd
        )
        if (composingText.isNotEmpty() &&
            (newSelStart != candidatesEnd || newSelEnd != candidatesEnd)
        ) {
            finishComposing()
            hotstringBuffer = ""
        }
        val hasSelection = newSelStart != newSelEnd
        if (hasSelection != isTextSelected) {
            isTextSelected = hasSelection
            if (isSuggestionBarActive && composingText.isEmpty()) {
                showIdleSuggestionBar(hasSelection)
            }
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        deactivateSuggestionBar()
        if (isClipboardPanelVisible) hideClipboardPanel()
        clipboardManager?.removePrimaryClipChangedListener(clipboardListener)
        clipboardManager = null
    }

    override fun onDestroy() {
        if (this::broadcastReceiver.isInitialized) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        }
        feedbackPlayer.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateInlineSuggestionsRequest(uiExtras: Bundle): InlineSuggestionsRequest {
        val styleBuilder = UiVersions.newStylesBuilder()
        val foregroundColor = ContextCompat.getColor(this@OpenMoaIME, R.color.key_foreground)
        val style = InlineSuggestionUi.newStyleBuilder()
            .setSingleIconChipStyle(
                ViewStyle.Builder()
                    .setBackground(
                        Icon.createWithResource(this, R.drawable.selector_key_background)
                    )
                    .setPadding(toDp(4), toDp(4), toDp(4), toDp(4))
                    .build()
            )
            .setChipStyle(
                ViewStyle.Builder()
                    .setBackground(
                        Icon.createWithResource(this, R.drawable.selector_key_background)
                    )
                    .setPadding(toDp(4), toDp(4), toDp(4), toDp(4))
                    .build()
            )
            .setStartIconStyle(
                ImageViewStyle.Builder()
                    .setPadding(0, 0, 0, 0)
                    .setLayoutMargin(toDp(4), 0, toDp(4), 0)
                    .build()
            )
            .setEndIconStyle(
                ImageViewStyle.Builder()
                    .setPadding(0, 0, 0, 0)
                    .setLayoutMargin(toDp(4), 0, toDp(4), 0)
                    .build()
            )
            .setTitleStyle(
                TextViewStyle.Builder()
                    .setTextColor(foregroundColor)
                    .setPadding(0, 0, 0, 0)
                    .setLayoutMargin(toDp(4), 0, toDp(4), 0)
                    .setTextSize(14F)
                    .build()
            )
            .setSubtitleStyle(
                TextViewStyle.Builder()
                    .setTextColor(
                        Color.argb(
                            127,
                            Color.red(foregroundColor),
                            Color.green(foregroundColor),
                            Color.blue(foregroundColor),
                        )
                    )
                    .setPadding(0, 0, 0, 0)
                    .setLayoutMargin(toDp(4), 0, toDp(4), 0)
                    .setTextSize(14F)
                    .build()
            )
            .build()
        styleBuilder.addStyle(style)
        val styleBundle = styleBuilder.build()
        // According to the document, when this list has only one element,
        // the first element should be used repeatedly. But it doesn't work that way on 1Password.
        // So I decide on the size of this list as the number of suggestions.
        // https://developer.android.com/reference/android/view/inputmethod/InlineSuggestionsRequest.Builder#public-constructors_1
        val presentationSpecs = List(config.maxSuggestionCount) {
            InlinePresentationSpec.Builder(
                Size(toDp(32), getHeight()), Size(toDp(640), getHeight())
            ).setStyle(styleBundle).build()
        }
        return InlineSuggestionsRequest.Builder(presentationSpecs)
            .setMaxSuggestionCount(config.maxSuggestionCount)
            .build()
    }

    private fun applyKeyboardLayout() {
        val skin = SettingsPreferences.getKeyboardSkin(this)
        val bgColor = SkinApplier.keyboardBgColor(this, skin)
        binding.root.setBackgroundColor(bgColor)
        binding.keyboardContainer.setBackgroundColor(bgColor)
        window.window?.apply {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                @Suppress("DEPRECATION")
                navigationBarColor = bgColor
            }
            val isLightBg = Color.luminance(bgColor) > 0.5f
            insetsController?.setSystemBarsAppearance(
                if (isLightBg) APPEARANCE_LIGHT_NAVIGATION_BARS else 0,
                APPEARANCE_LIGHT_NAVIGATION_BARS,
            )
        }
        val oneHandMode = SettingsPreferences.getOneHandMode(this)
        val displayWidth = resources.displayMetrics.widthPixels
        val keyboardWidth = if (oneHandMode.isReduced) {
            (displayWidth * 0.80f).toInt()
        } else {
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        }
        val keyboardHeight = calculateKeyboardHeight()
        val keyboardGravity = oneHandMode.gravity
        val params = android.widget.FrameLayout.LayoutParams(keyboardWidth, keyboardHeight).apply {
            gravity = keyboardGravity
        }
        binding.keyboardFrameLayout.layoutParams = params
        binding.clipboardPanel.layoutParams = android.widget.FrameLayout.LayoutParams(keyboardWidth, keyboardHeight).apply {
            gravity = keyboardGravity
        }
        if (this::binding.isInitialized) {
            val fg = SkinApplier.fgColor(this, skin)
            val bg = SkinApplier.keyboardBgColor(this, skin)
            binding.wordSuggestionBar.applyColors(fg, bg)
            binding.clipboardPanel.applyColors(fg, bg)
        }
    }

    private fun calculateKeyboardHeight(): Int {
        val displayHeight = resources.displayMetrics.heightPixels
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val baseScale = if (isLandscape) 0.50f else 0.35f
        val heightScale = SettingsPreferences.getKeypadHeight(this).heightScale
        return (displayHeight * baseScale * heightScale).toInt()
    }

    private fun getHeight(): Int {
        return toDp(32)
    }

    private fun toDp(pixel: Int): Int {
        return (pixel * resources.displayMetrics.density).roundToInt()
    }

    override fun onInlineSuggestionsResponse(response: InlineSuggestionsResponse): Boolean {
        if (!this::binding.isInitialized) {
            return false
        }
        binding.suggestionStripStartChipGroup.removeAllViews()
        binding.suggestionStripScrollableChipGroup.removeAllViews()
        binding.suggestionStripEndChipGroup.removeAllViews()
        binding.suggestionStripLayout.visibility =
            if (response.inlineSuggestions.isEmpty()) View.GONE else View.VISIBLE
        response.inlineSuggestions.forEach { inlineSuggestion ->
            val size = Size(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            inlineSuggestion.inflate(this, size, mainExecutor) { view ->
                if (inlineSuggestion.info.isPinned) {
                    if (binding.suggestionStripStartChipGroup.isEmpty()) {
                        binding.suggestionStripStartChipGroup.addView(view)
                    } else {
                        binding.suggestionStripEndChipGroup.addView(view)
                    }
                } else {
                    binding.suggestionStripScrollableChipGroup.addView(view)
                }
            }
        }
        return true
    }

    companion object {
        const val INTENT_ACTION = "keyInput"
        const val EXTRA_NAME = "key"
    }

}