package pe.aioo.openmoa.suggestion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KoreanPrefixExtractorTest {

    @Test
    fun `empty composingText returns empty primary`() {
        val (primary, fallback) = KoreanPrefixExtractor.extract("", null)
        assertEquals("", primary)
        assertNull(fallback)
    }

    @Test
    fun `complete syllable without unresolved returns text as primary`() {
        val (primary, fallback) = KoreanPrefixExtractor.extract("사랑", null)
        assertEquals("사랑", primary)
        assertEquals("사", fallback)
    }

    @Test
    fun `single complete syllable without unresolved has no fallback`() {
        val (primary, fallback) = KoreanPrefixExtractor.extract("사", null)
        assertEquals("사", primary)
        assertNull(fallback)
    }

    @Test
    fun `unresolved jamo stripped from composingText`() {
        // "사ㄹ" 상태 - unresolved = "ㄹ" (자모)
        val (primary, fallback) = KoreanPrefixExtractor.extract("사ㄹ", "ㄹ")
        assertEquals("사", primary)
        assertNull(fallback)
    }

    @Test
    fun `unresolved complete syllable moeum phase returns composingText`() {
        // "사라" 상태 - unresolved = "라" (완성 음절)
        val (primary, fallback) = KoreanPrefixExtractor.extract("사라", "라")
        assertEquals("사라", primary)
        assertEquals("사", fallback)
    }

    @Test
    fun `multi syllable with jamo unresolved`() {
        // "안녕ㅎ" 상태 - unresolved = "ㅎ"
        val (primary, fallback) = KoreanPrefixExtractor.extract("안녕ㅎ", "ㅎ")
        assertEquals("안녕", primary)
        assertEquals("안", fallback)
    }

    @Test
    fun `only jamo in composingText returns empty`() {
        val (primary, fallback) = KoreanPrefixExtractor.extract("ㄱ", "ㄱ")
        assertEquals("", primary)
        assertNull(fallback)
    }
}
