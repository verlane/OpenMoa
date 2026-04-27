package pe.aioo.openmoa.view.keyboardview

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pe.aioo.openmoa.R
import pe.aioo.openmoa.config.Config
import pe.aioo.openmoa.databinding.PunctuationViewBinding
import pe.aioo.openmoa.view.message.SpecialKey
import pe.aioo.openmoa.quickphrase.NumberLongKey
import pe.aioo.openmoa.view.keytouchlistener.EnterKeyTouchListener
import pe.aioo.openmoa.view.keytouchlistener.FunctionalKeyTouchListener
import pe.aioo.openmoa.view.keytouchlistener.LanguageKeyTouchListener
import pe.aioo.openmoa.view.keytouchlistener.QwertyKeyTouchListener
import pe.aioo.openmoa.view.keytouchlistener.RepeatKeyTouchListener
import pe.aioo.openmoa.view.keytouchlistener.SimpleKeyTouchListener
import pe.aioo.openmoa.settings.SettingsPreferences
import pe.aioo.openmoa.view.keytouchlistener.SpaceKeyTouchListener
import pe.aioo.openmoa.view.message.SpecialKeyMessage
import pe.aioo.openmoa.view.message.StringKeyMessage
import pe.aioo.openmoa.view.preview.KeyPreviewController
import pe.aioo.openmoa.view.preview.QuickPhraseMenuPopup
import pe.aioo.openmoa.view.skin.SkinApplier

class PunctuationView : ConstraintLayout, KoinComponent {

    private val config: Config by inject()

    constructor(context: Context) : super(context) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    var onEditNumberLongKeyRequest: ((NumberLongKey) -> Unit)? = null

    private lateinit var binding: PunctuationViewBinding
    private var previewController: KeyPreviewController? = null
    private val topRowNumberListeners = mutableListOf<QwertyKeyTouchListener>()
    private var enterKeyListener: EnterKeyTouchListener? = null
    private var languageKeyListener: LanguageKeyTouchListener? = null
    private var page = 0
    private val prefs by lazy {
        context.getSharedPreferences(SettingsPreferences.PREFS_NAME, Context.MODE_PRIVATE)
    }
    private val numberPrefKeys = NumberLongKey.values().map { it.prefKey }.toSet()
    private val prefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key in numberPrefKeys && ::binding.isInitialized && page == 0) {
            updateTopRowHints()
        }
    }

    private fun init() {
        inflate(context, R.layout.punctuation_view, this)
        binding = PunctuationViewBinding.bind(this)
        previewController = KeyPreviewController({ config.keyPreviewEnabled }, SettingsPreferences.getKeyboardSkin(context))
        setPageOrNextPage(0, true)
        setOnTouchListeners()
        SkinApplier.apply(this, SettingsPreferences.getKeyboardSkin(context))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        prefs.registerOnSharedPreferenceChangeListener(prefChangeListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        prefs.unregisterOnSharedPreferenceChangeListener(prefChangeListener)
        previewController?.cancel()
        topRowNumberListeners.forEach { it.cancel() }
        enterKeyListener?.cancel()
        languageKeyListener?.cancel()
    }

    private fun updateTopRowHints() {
        val topRowViews = listOf(
            binding.qKey, binding.wKey, binding.eKey, binding.rKey, binding.tKey,
            binding.yKey, binding.uKey, binding.iKey, binding.oKey, binding.pKey,
        )
        val longKeys = listOf(
            NumberLongKey.NUM_1, NumberLongKey.NUM_2, NumberLongKey.NUM_3,
            NumberLongKey.NUM_4, NumberLongKey.NUM_5, NumberLongKey.NUM_6,
            NumberLongKey.NUM_7, NumberLongKey.NUM_8, NumberLongKey.NUM_9,
            NumberLongKey.NUM_0,
        )
        topRowViews.zip(longKeys).forEach { (view, longKey) ->
            (view as? pe.aioo.openmoa.view.keyboardview.qwerty.HintKeyView)?.keyHint =
                longKey.getPhrase(context).take(1)
        }
    }

    fun setPageOrNextPage(newPage: Int? = null, isInitialize: Boolean = false) {
        if (page == newPage && !isInitialize) {
            return
        }
        page = newPage ?: ((page + 1) % PUNCTUATION_LIST.size)
        listOf(
            binding.qKey, binding.wKey, binding.eKey, binding.rKey, binding.tKey, binding.yKey,
            binding.uKey, binding.iKey, binding.oKey, binding.pKey, binding.aKey, binding.sKey,
            binding.dKey, binding.fKey, binding.gKey, binding.hKey, binding.jKey, binding.kKey,
            binding.lKey, binding.zKey, binding.xKey, binding.cKey, binding.vKey, binding.bKey,
            binding.nKey, binding.mKey,
        ).mapIndexed { index, view ->
            view.text = PUNCTUATION_LIST[page][index]
        }
        binding.nextKey.text = resources.getString(
            R.string.key_next_format, page + 1, PUNCTUATION_LIST.size
        )
        applyTopRowListeners()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun applyTopRowListeners() {
        topRowNumberListeners.forEach { it.cancel() }
        topRowNumberListeners.clear()
        val topRowViews = listOf(
            binding.qKey, binding.wKey, binding.eKey, binding.rKey, binding.tKey,
            binding.yKey, binding.uKey, binding.iKey, binding.oKey, binding.pKey,
        )
        if (page == 0) {
            val longKeys = listOf(
                NumberLongKey.NUM_1, NumberLongKey.NUM_2, NumberLongKey.NUM_3,
                NumberLongKey.NUM_4, NumberLongKey.NUM_5, NumberLongKey.NUM_6,
                NumberLongKey.NUM_7, NumberLongKey.NUM_8, NumberLongKey.NUM_9,
                NumberLongKey.NUM_0,
            )
            topRowViews.zip(longKeys).forEach { (view, longKey) ->
                (view as? pe.aioo.openmoa.view.keyboardview.qwerty.HintKeyView)?.keyHint =
                    longKey.getPhrase(context).take(1)
                val popup = QuickPhraseMenuPopup(context)
                val listener = QwertyKeyTouchListener(
                    context,
                    previewController,
                    longKeyProvider = { longKey.getPhrase(context) },
                    onTap = { StringKeyMessage(view.text.toString()) },
                    quickPhraseMenuPopup = popup,
                    onEdit = { onEditNumberLongKeyRequest?.invoke(longKey) },
                )
                topRowNumberListeners.add(listener)
                view.setOnTouchListener(listener)
            }
        } else {
            topRowViews.forEach { view ->
                (view as? pe.aioo.openmoa.view.keyboardview.qwerty.HintKeyView)?.keyHint = ""
                view.setOnTouchListener(
                    FunctionalKeyTouchListener(context, previewController = previewController) {
                        StringKeyMessage(view.text.toString())
                    }
                )
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setOnTouchListeners() {
        // 상단 행(qKey~pKey)은 applyTopRowListeners()에서 페이지에 따라 별도 처리
        listOf(
            binding.aKey, binding.sKey, binding.dKey, binding.fKey, binding.gKey,
            binding.hKey, binding.jKey, binding.kKey, binding.lKey, binding.zKey,
            binding.xKey, binding.cKey, binding.vKey, binding.bKey, binding.nKey,
            binding.mKey,
        ).map {
            it.apply {
                setOnTouchListener(FunctionalKeyTouchListener(context, previewController = previewController) {
                    StringKeyMessage(text.toString())
                })
            }
        }
        binding.apply {
            nextKey.setOnTouchListener(
                FunctionalKeyTouchListener(context) {
                    setPageOrNextPage()
                    null
                }
            )
            backspaceKey.setOnTouchListener(
                RepeatKeyTouchListener(context, SpecialKeyMessage(SpecialKey.BACKSPACE))
            )
            languageKeyListener?.cancel()
            languageKeyListener = LanguageKeyTouchListener(context)
            languageKey.setOnTouchListener(languageKeyListener)
            hanjaNumberPunctuationKey.setOnTouchListener(
                SimpleKeyTouchListener(
                    context, SpecialKeyMessage(SpecialKey.HANJA_NUMBER_PUNCTUATION)
                )
            )
            spaceKey.setOnTouchListener(SpaceKeyTouchListener(context))
            arrowKey.setOnTouchListener(
                SimpleKeyTouchListener(context, SpecialKeyMessage(SpecialKey.ARROW))
            )
            enterKeyListener?.cancel()
            enterKeyListener = EnterKeyTouchListener(context)
            enterKey.setOnTouchListener(enterKeyListener)
        }
    }

    companion object {
        private val PUNCTUATION_LIST = listOf(
            listOf(
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                "-", "@", "*", "^", ":", ";", "(", ")", "~",
                "/", "'", "\"", ".", ",", "?", "!",
            ),
            listOf(
                "#", "&", "%", "+", "=", "_", "\\", "|", "<", ">",
                "{", "}", "[", "]", "$", "￡", "¥", "€", "₩",
                "¢", "`", "˚", "•", "®", "©", "¿",
            ),
            listOf(
                "♥", "♡", "◎", "♩", "♬", "♨", "♀", "♂", "☞", "☜",
                "≠", "※", "≒", "♠", "♤", "★", "☆", "♣", "♧",
                "◐", "◆", "◇", "■", "□", "×", "÷",
            ),
            listOf(
                "Ψ", "Ω", "α", "β", "γ", "δ", "ε", "ζ", "η", "θ",
                "∀", "∂", "∃", "∇", "∈", "∋", "∏", "∑", "∝",
                "∞", "∧", "∨", "∩", "∪", "∫", "∬",
            ),
            listOf(
                "←", "↑", "→", "↓", "↔", "↕", "↖", "↗", "↘", "↙",
                "∮", "∴", "∵", "≡", "≤", "≥", "≪", "≫", "⌒",
                "⊂", "⊃", "⊆", "⊇", "℃", "℉", "™",
            ),
        )
    }

}