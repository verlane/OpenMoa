package pe.aioo.openmoa.view.suggestion

import android.content.Context
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
        val density = context.resources.displayMetrics.density
        return TextView(context).apply {
            text = word
            textSize = 19f
            setPadding((14 * density).toInt(), (4 * density).toInt(), (14 * density).toInt(), (4 * density).toInt())
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            background = with(android.graphics.drawable.RippleDrawable(
                android.content.res.ColorStateList.valueOf(0x22000000),
                null, null
            )) { this }
            setOnClickListener { onPick?.invoke(word) }
        }
    }

    fun applyColors(textColor: Int, bgColor: Int) {
        setBackgroundColor(bgColor)
        for (i in 0 until container.childCount) {
            (container.getChildAt(i) as? TextView)?.setTextColor(textColor)
        }
    }
}
