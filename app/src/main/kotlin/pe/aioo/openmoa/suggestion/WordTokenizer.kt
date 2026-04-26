package pe.aioo.openmoa.suggestion

object WordTokenizer {

    private const val MAX_LENGTH = 30
    private const val MIN_KOREAN_LENGTH = 2
    private const val MIN_ENGLISH_LENGTH = 4

    // 긴 것부터 먼저 매칭해야 "에게서"가 "에게"보다, "에서"가 "에"보다 먼저 제거됨
    private val KO_SUFFIXES = listOf(
        "에게서", "한테서",
        "으로", "에서", "에게", "한테",
        "와", "과", "로", "은", "는", "이", "가", "을", "를",
        "에", "도", "만", "의",
    )

    fun extractKorean(text: String): String? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null
        if (trimmed.length > MAX_LENGTH) return null
        if (!trimmed.all { it in '가'..'힣' || it in 'ᄀ'..'ᇿ' || it in '㄰'..'㆏' }) return null

        val stem = KO_SUFFIXES.firstOrNull { trimmed.endsWith(it) }
            ?.let { trimmed.dropLast(it.length) }
            ?: trimmed

        return if (stem.length < MIN_KOREAN_LENGTH) null else stem
    }

    fun extractEnglish(text: String): String? {
        val trimmed = text.trim()
        if (trimmed.length < MIN_ENGLISH_LENGTH || trimmed.length > MAX_LENGTH) return null

        if (!trimmed.first().isAsciiLetterOrDigit() || !trimmed.last().isAsciiLetterOrDigit()) return null

        val specials = setOf('.', '@', '-')
        var prevWasSpecial = false
        var atCount = 0
        for (ch in trimmed) {
            when {
                ch.isLetter() && ch.code < 128 -> prevWasSpecial = false
                ch.isDigit() && ch.code < 128 -> prevWasSpecial = false
                ch in specials -> {
                    if (prevWasSpecial) return null
                    if (ch == '@' && ++atCount > 1) return null
                    prevWasSpecial = true
                }
                else -> return null
            }
        }

        return trimmed.lowercase()
    }

    private fun Char.isAsciiLetterOrDigit() = (isLetter() || isDigit()) && code < 128
}
