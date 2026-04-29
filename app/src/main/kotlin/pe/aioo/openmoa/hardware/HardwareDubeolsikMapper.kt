package pe.aioo.openmoa.hardware

object HardwareDubeolsikMapper {

    fun toJamo(char: Char, shift: Boolean): String? {
        val isUpper = char.isUpperCase()
        val lower = char.lowercaseChar()
        val effectiveShift = shift || isUpper
        return if (effectiveShift) SHIFT[lower] ?: NORMAL[lower] else NORMAL[lower]
    }

    private val NORMAL = mapOf(
        'q' to "ㅂ", 'w' to "ㅈ", 'e' to "ㄷ", 'r' to "ㄱ", 't' to "ㅅ",
        'y' to "ㅛ", 'u' to "ㅕ", 'i' to "ㅑ", 'o' to "ㅐ", 'p' to "ㅔ",
        'a' to "ㅁ", 's' to "ㄴ", 'd' to "ㅇ", 'f' to "ㄹ", 'g' to "ㅎ",
        'h' to "ㅗ", 'j' to "ㅓ", 'k' to "ㅏ", 'l' to "ㅣ",
        'z' to "ㅋ", 'x' to "ㅌ", 'c' to "ㅊ", 'v' to "ㅍ",
        'b' to "ㅠ", 'n' to "ㅜ", 'm' to "ㅡ",
    )

    private val SHIFT = mapOf(
        'q' to "ㅃ", 'w' to "ㅉ", 'e' to "ㄸ", 'r' to "ㄲ", 't' to "ㅆ",
        'o' to "ㅒ", 'p' to "ㅖ",
    )
}
