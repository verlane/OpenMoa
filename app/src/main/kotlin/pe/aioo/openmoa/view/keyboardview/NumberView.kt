package pe.aioo.openmoa.view.keyboardview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pe.aioo.openmoa.R
import pe.aioo.openmoa.config.Config
import pe.aioo.openmoa.databinding.NumberViewBinding
import pe.aioo.openmoa.view.message.SpecialKey
import pe.aioo.openmoa.view.keytouchlistener.EnterKeyTouchListener
import pe.aioo.openmoa.view.keytouchlistener.LanguageKeyTouchListener
import pe.aioo.openmoa.view.keytouchlistener.QwertyKeyTouchListener
import pe.aioo.openmoa.view.keytouchlistener.RepeatKeyTouchListener
import pe.aioo.openmoa.view.keytouchlistener.SimpleKeyTouchListener
import pe.aioo.openmoa.quickphrase.NumberLongKey
import pe.aioo.openmoa.settings.SettingsPreferences
import pe.aioo.openmoa.view.keytouchlistener.SpaceKeyTouchListener
import pe.aioo.openmoa.view.message.SpecialKeyMessage
import pe.aioo.openmoa.view.message.StringKeyMessage
import pe.aioo.openmoa.view.preview.KeyPreviewController
import pe.aioo.openmoa.view.preview.QuickPhraseMenuPopup
import pe.aioo.openmoa.view.skin.SkinApplier

class NumberView : ConstraintLayout, KoinComponent {

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

    private lateinit var binding: NumberViewBinding
    private var previewController: KeyPreviewController? = null
    private val numberKeyListeners = mutableListOf<QwertyKeyTouchListener>()
    private var enterKeyListener: EnterKeyTouchListener? = null
    private var languageKeyListener: LanguageKeyTouchListener? = null

    private fun init() {
        inflate(context, R.layout.number_view, this)
        binding = NumberViewBinding.bind(this)
        previewController = KeyPreviewController({ false }, SettingsPreferences.getKeyboardSkin(context))
        setOnTouchListeners()
        SkinApplier.apply(this, SettingsPreferences.getKeyboardSkin(context))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setOnTouchListeners() {
        numberKeyListeners.clear()
        binding.apply {
            plusKey.setOnTouchListener(SimpleKeyTouchListener(context, StringKeyMessage("+")))
            minusKey.setOnTouchListener(SimpleKeyTouchListener(context, StringKeyMessage("-")))
            asteriskKey.setOnTouchListener(SimpleKeyTouchListener(context, StringKeyMessage("*")))
            dotKey.setOnTouchListener(SimpleKeyTouchListener(context, StringKeyMessage(".")))
            slashKey.setOnTouchListener(SimpleKeyTouchListener(context, StringKeyMessage("/")))
            backspaceKey.setOnTouchListener(
                RepeatKeyTouchListener(context, SpecialKeyMessage(SpecialKey.BACKSPACE))
            )
            languageKeyListener?.cancel()
            languageKeyListener = LanguageKeyTouchListener(context)
            languageKey.setOnTouchListener(languageKeyListener)
            hanjaNumberPunctuationKey.setOnTouchListener(
                SimpleKeyTouchListener(context, SpecialKeyMessage(SpecialKey.HANJA_NUMBER_PUNCTUATION))
            )
            spaceKey.setOnTouchListener(SpaceKeyTouchListener(context))
            enterKeyListener?.cancel()
            enterKeyListener = EnterKeyTouchListener(context)
            enterKey.setOnTouchListener(enterKeyListener)

            listOf(
                oneKey to NumberLongKey.NUM_1, twoKey to NumberLongKey.NUM_2,
                threeKey to NumberLongKey.NUM_3, fourKey to NumberLongKey.NUM_4,
                fiveKey to NumberLongKey.NUM_5, sixKey to NumberLongKey.NUM_6,
                sevenKey to NumberLongKey.NUM_7, eightKey to NumberLongKey.NUM_8,
                nineKey to NumberLongKey.NUM_9, zeroKey to NumberLongKey.NUM_0,
            ).forEach { (view, longKey) ->
                view.keyHint = longKey.getPhrase(context).take(1)
                val popup = QuickPhraseMenuPopup(context)
                val listener = QwertyKeyTouchListener(
                    context,
                    previewController,
                    longKeyProvider = { longKey.getPhrase(context) },
                    onTap = { StringKeyMessage(longKey.digit) },
                    quickPhraseMenuPopup = popup,
                    onEdit = { onEditNumberLongKeyRequest?.invoke(longKey) },
                )
                numberKeyListeners.add(listener)
                view.setOnTouchListener(listener)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        previewController?.cancel()
        numberKeyListeners.forEach { it.cancel() }
        enterKeyListener?.cancel()
        languageKeyListener?.cancel()
    }

}