package pe.aioo.openmoa.suggestion

object HangulSyllable {

    private const val BASE = '가'
    private const val JONGSEONG_COUNT = 28
    private const val JUNGSEONG_COUNT = 21
    private const val CHOSUNG = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ"

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

    fun isChosung(c: Char): Boolean = CHOSUNG.contains(c)

    fun isAllChosung(s: String): Boolean = s.isNotEmpty() && s.all { isChosung(it) }

    fun getChosung(syllable: Char): Char? {
        if (syllable !in '가'..'힣') return null
        return CHOSUNG[(syllable - BASE) / (JUNGSEONG_COUNT * JONGSEONG_COUNT)]
    }

    fun syllableRangeForChosung(c: Char): CharRange {
        val idx = CHOSUNG.indexOf(c)
        if (idx < 0) return CharRange.EMPTY
        val start = BASE + idx * JUNGSEONG_COUNT * JONGSEONG_COUNT
        return start..(start + JUNGSEONG_COUNT * JONGSEONG_COUNT - 1)
    }

    fun matchesChosungPattern(word: String, pattern: String): Boolean {
        if (word.length < pattern.length) return false
        return pattern.indices.all { i -> getChosung(word[i]) == pattern[i] }
    }
}
