package pe.aioo.openmoa.view.suggestion

import android.content.res.ColorStateList
import android.content.Context
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView

class SuggestionBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : HorizontalScrollView(context, attrs) {

    var onPick: ((String) -> Unit)? = null

    private val density = context.resources.displayMetrics.density
    private val container = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    init {
        isHorizontalScrollBarEnabled = false
        addView(container, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT))
    }

    fun setSuggestions(words: List<String>) {
        container.removeAllViews()
        words.forEach { word -> container.addView(buildWordView(word)) }
    }

    private fun buildWordView(word: String): TextView {
        return TextView(context).apply {
            text = word
            textSize = TEXT_SIZE_SP
            setPadding(
                (PADDING_H_DP * density).toInt(), (PADDING_V_DP * density).toInt(),
                (PADDING_H_DP * density).toInt(), (PADDING_V_DP * density).toInt(),
            )
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            background = RippleDrawable(ColorStateList.valueOf(0x22000000), null, null)
            setOnClickListener { onPick?.invoke(word) }
        }
    }

    companion object {
        private const val TEXT_SIZE_SP = 19f
        private const val PADDING_H_DP = 14
        private const val PADDING_V_DP = 4
    }

    fun applyColors(textColor: Int, bgColor: Int) {
        setBackgroundColor(bgColor)
        for (i in 0 until container.childCount) {
            (container.getChildAt(i) as? TextView)?.setTextColor(textColor)
        }
    }
}
