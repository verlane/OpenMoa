package pe.aioo.openmoa.suggestion

object KoreanPrefixExtractor {

    // 조합 중인 한글 텍스트에서 Trie 검색에 사용할 prefix를 추출한다.
    // unresolved: HangulAssembler.getUnresolved() 반환값 (null 또는 조합 중인 자모)
    // 반환: (primary, fallback) - primary가 비어있으면 제안 없음
    fun extract(composingText: String, unresolved: String?): Pair<String, String?> {
        if (composingText.isEmpty()) return Pair("", null)

        // 조합 중인 unresolved가 완성 음절이 아닌 자모만인 경우 잘라냄
        // 예: "사ㄹ" -> primary="사", fallback=null
        // 예: "사랑" (ㅇ 종성 포함 완성) -> primary="사랑", fallback="사"
        // 예: "사라" (모음 연결 중) -> primary="사라", fallback="사"
        if (unresolved != null && !unresolved.all { it in '가'..'힣' }) {
            // unresolved에 자모가 포함됨 -> composingText에서 제거
            val primary = composingText.removeSuffix(unresolved)
            return if (primary.isEmpty() || !primary.all { it in '가'..'힣' }) {
                Pair("", null)
            } else {
                Pair(primary, if (primary.length > 1) primary.dropLast(1) else null)
            }
        }

        // 완성 음절 상태 (unresolved null이거나 완성 음절)
        val primary = composingText.filter { it in '가'..'힣' }
        val fallback = if (primary.length > 1) primary.dropLast(1) else null
        return Pair(primary, fallback)
    }
}
