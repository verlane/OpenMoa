package pe.aioo.openmoa.suggestion

object HangulSyllable {

    private const val BASE = '가'
    private const val JONGSEONG_COUNT = 28

    fun isHangulSyllable(c: Char): Boolean = c in '가'..'힣'

    fun hasJongseong(c: Char): Boolean = (c - BASE) % JONGSEONG_COUNT != 0

    fun syllableBase(c: Char): Char {
        val offset = (c - BASE) % JONGSEONG_COUNT
        return c - offset
    }

    fun jongseongRange(c: Char): CharRange {
        val base = syllableBase(c)
        return base..(base + JONGSEONG_COUNT - 1)
    }
}
