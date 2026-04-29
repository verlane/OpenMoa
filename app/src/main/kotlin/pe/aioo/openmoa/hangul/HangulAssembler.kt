package pe.aioo.openmoa.hangul

import com.github.kimkevin.hangulparser.HangulParser
import com.github.kimkevin.hangulparser.HangulParserException

class HangulAssembler {

    private val jamoList = arrayListOf<String>()
    private var isDecomposedState = false

    private fun assembleLastJongseongIfCan(jamo: String): Boolean {
        if (!jamo.matches(JAEUM_REGEX) || jamoList.size != 3) {
            return false
        }
        val lastJamo = jamoList.removeAt(jamoList.lastIndex)
        jamoList.add(
            when (lastJamo) {
                "ㄱ" -> {
                    when (jamo) {
                        "ㅅ" ->  "ㄳ"
                        else -> lastJamo
                    }
                }
                "ㄴ" -> {
                    when (jamo) {
                        "ㅈ" -> "ㄵ"
                        "ㅎ" -> "ㄶ"
                        else -> lastJamo
                    }
                }
                "ㄹ" -> {
                    when (jamo) {
                        "ㄱ" -> "ㄺ"
                        "ㅁ" -> "ㄻ"
                        "ㅂ" -> "ㄼ"
                        "ㅅ" -> "ㄽ"
                        "ㅌ" -> "ㄾ"
                        "ㅍ" -> "ㄿ"
                        "ㅎ" -> "ㅀ"
                        else -> lastJamo
                    }
                }
                "ㅂ" -> {
                    when (jamo) {
                        "ㅅ" -> "ㅄ"
                        else -> lastJamo
                    }
                }
                else -> lastJamo
            }
        )
        return jamoList.last() != lastJamo
    }

    private fun disassembleLastJongseongIfCan() {
        val lastJamo = jamoList.removeAt(jamoList.lastIndex)
        jamoList.addAll(DOUBLE_JONGSEONG_DECOMPOSITION[lastJamo] ?: listOf(lastJamo))
    }

    private fun assembleLastMoeumIfCan(jamo: String): Boolean {
        if (!jamoList.last().matches(MOEUM_REGEX)) {
            return false
        }
        val lastJamo = jamoList.removeAt(jamoList.lastIndex)
        jamoList.add(
            when (lastJamo) {
                "ㅏ" -> when (jamo) {
                    "ㅣ" -> "ㅐ"
                    "ㆍ" -> "ㅑ"
                    else -> lastJamo
                }
                "ㅐ" -> when (jamo) {
                    "ㆍ" -> "ㅒ"
                    else -> lastJamo
                }
                "ㅑ" -> when (jamo) {
                    "ㅣ" -> "ㅒ"
                    else -> lastJamo
                }
                "ㅓ" -> when (jamo) {
                    "ㅣ" -> "ㅔ"
                    "ㆍ" -> "ㅕ"
                    else -> lastJamo
                }
                "ㅔ" -> when (jamo) {
                    "ㆍ" -> "ㅖ"
                    else -> lastJamo
                }
                "ㅕ" -> when (jamo) {
                    "ㅣ" -> "ㅖ"
                    else -> lastJamo
                }
                "ㅗ" -> when (jamo) {
                    "ㅣ" -> "ㅚ"
                    "ㆍ" -> "ㅛ"
                    else -> lastJamo
                }
                "ㅘ" -> when (jamo) {
                    "ㅣ" -> "ㅙ"
                    else -> lastJamo
                }
                "ㅚ" -> when (jamo) {
                    "ㆍ" -> "ㅘ"
                    else -> lastJamo
                }
                "ㅜ" -> when (jamo) {
                    "ㅣ" -> "ㅟ"
                    "ㆍ" -> "ㅠ"
                    else -> lastJamo
                }
                "ㅝ" -> when (jamo) {
                    "ㅣ" ->  "ㅞ"
                    else -> lastJamo
                }
                "ㅠ" -> when (jamo) {
                    "ㅣ" ->  "ㅝ"
                    else -> lastJamo
                }
                "ㅡ" -> when (jamo) {
                    "ㅣ" -> "ㅢ"
                    "ㆍ" -> "ㅜ"
                    else -> lastJamo
                }
                "ㅣ" -> when (jamo) {
                    "ㆍ" -> "ㅏ"
                    else -> lastJamo
                }
                "ㆍ" -> when (jamo) {
                    "ㅡ" -> "ㅗ"
                    "ㅣ" -> "ㅓ"
                    "ㆍ" -> "ᆢ"
                    else -> lastJamo
                }
                "ᆢ" -> when (jamo) {
                    "ㅡ" -> "ㅛ"
                    "ㅣ" -> "ㅕ"
                    else -> lastJamo
                }
                else -> lastJamo
            }
        )
        return jamoList.last() != lastJamo
    }

    private fun resolveJamoList(forceResolve: Boolean = false): String? {
        try {
            val assembled = HangulParser.assemble(jamoList)
            if (assembled.length > 1) {
                repeat(HangulParser.disassemble(assembled.substring(0, 1)).size) {
                    jamoList.removeAt(0)
                }
                return assembled.substring(0, 1)
            }
            if (forceResolve) {
                jamoList.clear()
                return assembled
            }
            return null
        } catch (e: HangulParserException) {
            val prevJamoList = jamoList.subList(0, jamoList.size - 1)
            val resolved = try {
                HangulParser.assemble(prevJamoList)
            } catch (_: HangulParserException) {
                prevJamoList.joinToString("")
            }
            repeat(prevJamoList.size) {
                jamoList.removeAt(0)
            }
            return resolved
        }
    }

    fun appendJamo(jamo: String): String? {
        isDecomposedState = false
        if (jamoList.isEmpty()) {
            jamoList.add(jamo)
            return null
        }
        if (jamo.matches(MOEUM_REGEX)) {
            disassembleLastJongseongIfCan()
        }
        if (assembleLastMoeumIfCan(jamo)) {
            return null
        }
        if (assembleLastJongseongIfCan(jamo)) {
            return null
        }
        if (jamo.matches(ARAEA_REGEX) && jamoList.last().matches(JAEUM_REGEX)) {
            val lastJamo = jamoList.removeAt(jamoList.lastIndex)
            val resolved = if (jamoList.isEmpty()) null else resolveJamoList(true)
            jamoList.addAll(listOf(lastJamo, jamo))
            return resolved
        }
        jamoList.add(jamo)
        return resolveJamoList()
    }

    fun previewWithAppended(jamo: String): String {
        val snapshot = ArrayList(jamoList)
        return try {
            appendJamo(jamo)
            getUnresolved() ?: jamo
        } finally {
            jamoList.clear()
            jamoList.addAll(snapshot)
        }
    }

    fun getUnresolved(): String? {
        return if (jamoList.isEmpty()) {
            null
        } else {
            try {
                HangulParser.assemble(jamoList)
            } catch (_: HangulParserException) {
                jamoList.joinToString("")
            }
        }
    }

    /**
     * 마지막 자모를 새 자모로 교체 (multi-tap 합성용).
     * removeLastJamo 와 달리 음절을 클리어하지 않고 jamoList의 마지막만 바꿈.
     * @return 교체 후 unresolved (조합된 음절 또는 자모 시퀀스), 또는 null
     */
    fun replaceLastJamo(newJamo: String): String? {
        if (jamoList.isEmpty()) return null
        jamoList[jamoList.lastIndex] = newJamo
        isDecomposedState = false
        return getUnresolved()
    }

    fun removeLastJamo() {
        val last = jamoList.lastOrNull() ?: return
        val firstOfDouble = DOUBLE_JONGSEONG_DECOMPOSITION[last]?.first()
        when {
            firstOfDouble != null -> {
                jamoList[jamoList.lastIndex] = firstOfDouble
            }
            last.matches(MOEUM_REGEX) -> {
                if (isDecomposedState) {
                    jamoList.removeAt(jamoList.lastIndex)
                    isDecomposedState = false
                } else {
                    jamoList.clear()
                }
            }
            else -> {
                jamoList.removeAt(jamoList.lastIndex)
                isDecomposedState = jamoList.lastOrNull()?.matches(MOEUM_REGEX) == true
            }
        }
    }

    fun clear() {
        jamoList.clear()
        isDecomposedState = false
    }

    companion object {
        val JAMO_REGEX = Regex("^[ㄱ-ㅎㅏ-ㅣㆍ]$")
        private val JAEUM_REGEX = Regex("^[ㄱ-ㅎ]$")
        private val MOEUM_REGEX = Regex("^[ㅏ-ㅣㆍᆢ]$")
        private val ARAEA_REGEX = Regex("^[ㆍᆢ]$")
        private val DOUBLE_JONGSEONG_DECOMPOSITION = mapOf(
            "ㄳ" to listOf("ㄱ", "ㅅ"),
            "ㄵ" to listOf("ㄴ", "ㅈ"),
            "ㄶ" to listOf("ㄴ", "ㅎ"),
            "ㄺ" to listOf("ㄹ", "ㄱ"),
            "ㄻ" to listOf("ㄹ", "ㅁ"),
            "ㄼ" to listOf("ㄹ", "ㅂ"),
            "ㄽ" to listOf("ㄹ", "ㅅ"),
            "ㄾ" to listOf("ㄹ", "ㅌ"),
            "ㄿ" to listOf("ㄹ", "ㅍ"),
            "ㅀ" to listOf("ㄹ", "ㅎ"),
            "ㅄ" to listOf("ㅂ", "ㅅ"),
        )
    }

}