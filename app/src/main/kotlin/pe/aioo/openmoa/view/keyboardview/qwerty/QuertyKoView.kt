package pe.aioo.openmoa.view.keyboardview.qwerty

import android.content.Context
import android.util.AttributeSet
import android.view.View

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

    /**
     * 단모음 모드: 자모가 없는 키 자리(o, p, l, m)를 GONE 처리.
     * ConstraintLayout horizontal chain에서 GONE된 view는 weight 합에서 제외되어
     * 나머지 키들이 균등하게 화면을 채움.
     */
    private fun applySimpleVisibility() {
        // setter 가 init() 완료 전에 호출될 가능성을 가드 (XML attribute 바인딩 등)
        if (!isBindingInitialized()) return
        val visibility = if (simple) View.GONE else View.VISIBLE
        binding.oKey.visibility = visibility
        binding.pKey.visibility = visibility
        binding.lKey.visibility = visibility
        binding.mKey.visibility = visibility
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
