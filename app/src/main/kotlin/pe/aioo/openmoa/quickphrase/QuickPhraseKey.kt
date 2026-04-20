package pe.aioo.openmoa.quickphrase

enum class QuickPhraseKey(
    val jaum: String,
    val defaultPhrase: String,
    val prefKey: String,
) {
    KIEUK("ㅋ", "안녕하세요", "quick_phrase_kieuk"),
    TIEUT("ㅌ", "감사합니다", "quick_phrase_tieut"),
    CHIEUT("ㅊ", "어디세요?", "quick_phrase_chieut"),
    PIEUP("ㅍ", "연락바랍니다", "quick_phrase_pieup"),
}
