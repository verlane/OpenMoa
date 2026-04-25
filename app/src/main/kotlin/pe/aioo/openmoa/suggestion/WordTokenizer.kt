package pe.aioo.openmoa.suggestion

object WordTokenizer {

    private const val MAX_LENGTH = 30

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

        if (stem.length < 2) return trimmed
        return stem
    }

    fun extractEnglish(text: String): String? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null
        if (trimmed.length > MAX_LENGTH) return null
        if (!trimmed.all { it.isLetter() && it.code < 128 }) return null
        return trimmed.lowercase()
    }
}
