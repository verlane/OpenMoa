package pe.aioo.openmoa.view.keyboardview.qwerty

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

class QuertyKoView : QuertyView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    var simple: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            applySimpleVisibility()
            refreshKeyLabels()
        }

    override fun keyList(): List<List<String>> = if (simple) SIMPLE_KEY_LIST else KO_KEY_LIST

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        applySimpleVisibility()
    }

    /**
     * 단모음 모드: 자모가 없는 키 자리(o, p, l, m)를 GONE 처리.
     * beforeASpace/afterLSpace는 수직 체인 앵커 역할을 하므로 GONE 불가.
     * 대신 horizontal weight를 0으로 조정해 ㅁ행/ㅋ행 weight를 8.0으로 맞춤 (ㅂ행과 동일).
     */
    private fun applySimpleVisibility() {
        if (!isBindingInitialized()) return
        val visibility = if (simple) View.GONE else View.VISIBLE
        binding.oKey.visibility = visibility
        binding.pKey.visibility = visibility
        binding.lKey.visibility = visibility
        binding.mKey.visibility = visibility

        val spaceWeight = if (simple) 0f else 0.5f
        (binding.beforeASpace.layoutParams as? ConstraintLayout.LayoutParams)?.let {
            it.horizontalWeight = spaceWeight
            binding.beforeASpace.layoutParams = it
            binding.beforeASpace.requestLayout()
        }
        (binding.afterLSpace.layoutParams as? ConstraintLayout.LayoutParams)?.let {
            it.horizontalWeight = spaceWeight
            binding.afterLSpace.layoutParams = it
            binding.afterLSpace.requestLayout()
        }

        val shiftBackspaceWeight = if (simple) 1f else 1.5f
        (binding.shiftKey.layoutParams as? ConstraintLayout.LayoutParams)?.let {
            it.horizontalWeight = shiftBackspaceWeight
            binding.shiftKey.layoutParams = it
        }
        (binding.backspaceKey.layoutParams as? ConstraintLayout.LayoutParams)?.let {
            it.horizontalWeight = shiftBackspaceWeight
            binding.backspaceKey.layoutParams = it
        }
    }

    companion object {
        // 두벌식(KS X 5002) 표준 자판
        private val KO_KEY_LIST = listOf(
            listOf(
                "ㅂ", "ㅈ", "ㄷ", "ㄱ", "ㅅ", "ㅛ", "ㅕ", "ㅑ", "ㅐ", "ㅔ",
                "ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅗ", "ㅓ", "ㅏ", "ㅣ",
                "ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅠ", "ㅜ", "ㅡ",
            ),
            listOf(
                "ㅃ", "ㅉ", "ㄸ", "ㄲ", "ㅆ", "ㅛ", "ㅕ", "ㅑ", "ㅒ", "ㅖ",
                "ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅗ", "ㅓ", "ㅏ", "ㅣ",
                "ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅠ", "ㅜ", "ㅡ",
            ),
        )

        // 단모음 자판 (Gboard 단모음 배열):
        // 1행 q~p (10키): ㅂ ㅈ ㄷ ㄱ ㅅ ㅗ ㅐ ㅔ + 2 빈 (o, p GONE)
        // 2행 a~l (9키):  ㅁ ㄴ ㅇ ㄹ ㅎ ㅓ ㅏ ㅣ + 1 빈 (l GONE)
        // 3행 z~m (7키):  ㅋ ㅌ ㅊ ㅍ ㅜ ㅡ + 1 빈 (m GONE)
        // multi-tap 합성(ㅏㅏ→ㅑ 등)은 IME 측에서 처리됨.
        private val SIMPLE_KEY_LIST = listOf(
            listOf(
                "ㅂ", "ㅈ", "ㄷ", "ㄱ", "ㅅ", "ㅗ", "ㅐ", "ㅔ", "", "",
                "ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅓ", "ㅏ", "ㅣ", "",
                "ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅜ", "ㅡ", "",
            ),
            listOf(
                "ㅃ", "ㅉ", "ㄸ", "ㄲ", "ㅆ", "ㅗ", "ㅐ", "ㅔ", "", "",
                "ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅓ", "ㅏ", "ㅣ", "",
                "ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅜ", "ㅡ", "",
            ),
        )
    }
}
