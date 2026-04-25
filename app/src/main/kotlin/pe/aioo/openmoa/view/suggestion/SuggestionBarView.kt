package pe.aioo.openmoa.view.suggestion

import android.content.res.ColorStateList
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import pe.aioo.openmoa.R

class SuggestionBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : HorizontalScrollView(context, attrs) {

    var onPick: ((String, Boolean) -> Unit)? = null

    private val density = context.resources.displayMetrics.density
    private var currentTextColor: Int = Color.BLACK
    private val container = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    init {
        isHorizontalScrollBarEnabled = false
        addView(container, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT))
    }

    fun setSuggestions(words: List<String>, hotstringExpansions: Set<String> = emptySet()) {
        container.removeAllViews()
        scrollTo(0, 0)
        words.forEach { word -> container.addView(buildWordView(word, word in hotstringExpansions)) }
    }

    fun showClipboard(text: String, onPaste: (String) -> Unit) {
        val preview = if (text.length > MAX_CLIPBOARD_LEN) text.take(MAX_CLIPBOARD_LEN) + "…" else text
        container.removeAllViews()
        scrollTo(0, 0)
        container.addView(buildClipboardView(preview, text, onPaste))
    }

    fun showSelectionActions(onCut: () -> Unit, onCopy: () -> Unit) {
        container.removeAllViews()
        scrollTo(0, 0)
        container.addView(buildActionView(context.getString(R.string.key_cut), onCut))
        container.addView(buildActionView(context.getString(R.string.key_copy), onCopy))
    }

    fun showEmpty() {
        container.removeAllViews()
        scrollTo(0, 0)
    }

    private fun buildWordView(word: String, isHotstring: Boolean): TextView {
        return TextView(context).apply {
            text = word
            textSize = TEXT_SIZE_SP
            setTextColor(currentTextColor)
            setPadding(
                (PADDING_H_DP * density).toInt(), (PADDING_V_DP * density).toInt(),
                (PADDING_H_DP * density).toInt(), (PADDING_V_DP * density).toInt(),
            )
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            background = RippleDrawable(ColorStateList.valueOf(0x22000000), null, null)
            setOnClickListener { onPick?.invoke(word, isHotstring) }
        }
    }

    private fun buildClipboardView(displayText: String, fullText: String, onPaste: (String) -> Unit): TextView {
        return TextView(context).apply {
            text = displayText
            textSize = TEXT_SIZE_SP
            setTextColor(currentTextColor)
            alpha = CLIPBOARD_ALPHA
            setTypeface(null, Typeface.ITALIC)
            setPadding(
                (PADDING_H_DP * density).toInt(), (PADDING_V_DP * density).toInt(),
                (PADDING_H_DP * density).toInt(), (PADDING_V_DP * density).toInt(),
            )
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            background = RippleDrawable(ColorStateList.valueOf(0x22000000), null, null)
            setOnClickListener { onPaste(fullText) }
        }
    }

    private fun buildActionView(label: String, action: () -> Unit): TextView {
        return TextView(context).apply {
            text = label
            textSize = TEXT_SIZE_SP
            setTextColor(currentTextColor)
            setPadding(
                (PADDING_H_DP * density).toInt(), (PADDING_V_DP * density).toInt(),
                (PADDING_H_DP * density).toInt(), (PADDING_V_DP * density).toInt(),
            )
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            background = RippleDrawable(ColorStateList.valueOf(0x22000000), null, null)
            setOnClickListener { action() }
        }
    }

    fun applyColors(textColor: Int, bgColor: Int) {
        currentTextColor = textColor
        setBackgroundColor(bgColor)
        for (i in 0 until container.childCount) {
            (container.getChildAt(i) as? TextView)?.setTextColor(textColor)
        }
    }

    companion object {
        private const val TEXT_SIZE_SP = 17f
        private const val PADDING_H_DP = 10
        private const val PADDING_V_DP = 4
        private const val MAX_CLIPBOARD_LEN = 20
        private const val CLIPBOARD_ALPHA = 0.65f
    }
}
