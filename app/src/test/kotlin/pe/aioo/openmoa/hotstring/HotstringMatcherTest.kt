package pe.aioo.openmoa.hotstring

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class HotstringMatcherTest {

    private val rules = listOf(
        HotstringRule(id = "1", trigger = ".y", expansion = "youtube"),
        HotstringRule(id = "2", trigger = ".g", expansion = "google"),
        HotstringRule(id = "3", trigger = "btw", expansion = "by the way"),
    )

    // --- findMatch: 앞 글자 없을 때 ---

    @Test
    fun `trigger at start of buffer matches`() {
        val match = HotstringMatcher.findMatch(".y", rules)
        assertNotNull(match)
        assertEquals("youtube", match!!.expansion)
    }

    @Test
    fun `trigger preceded by space matches`() {
        val match = HotstringMatcher.findMatch("aaa .y", rules)
        assertNotNull(match)
        assertEquals("youtube", match!!.expansion)
    }

    @Test
    fun `trigger preceded by tab matches`() {
        val match = HotstringMatcher.findMatch("\t.y", rules)
        assertNotNull(match)
        assertEquals("youtube", match!!.expansion)
    }

    // --- findMatch: 앞 글자 있을 때 ---

    @Test
    fun `trigger preceded by letter does not match`() {
        val match = HotstringMatcher.findMatch("aaa.y", rules)
        assertNull(match)
    }

    @Test
    fun `trigger preceded by dot does not match`() {
        val match = HotstringMatcher.findMatch("..y", rules)
        assertNull(match)
    }

    @Test
    fun `alphabetic trigger preceded by letter does not match`() {
        val match = HotstringMatcher.findMatch("abtw", rules)
        assertNull(match)
    }

    @Test
    fun `alphabetic trigger preceded by space matches`() {
        val match = HotstringMatcher.findMatch("a btw", rules)
        assertNotNull(match)
        assertEquals("by the way", match!!.expansion)
    }

    // --- findMatch: 비활성 규칙 ---

    @Test
    fun `disabled rule does not match`() {
        val disabledRules = listOf(HotstringRule(id = "1", trigger = ".y", expansion = "youtube", enabled = false))
        assertNull(HotstringMatcher.findMatch(".y", disabledRules))
    }

    // --- bufferLengthNeeded ---

    @Test
    fun `bufferLengthNeeded returns max trigger length plus one`() {
        assertEquals(4, HotstringMatcher.bufferLengthNeeded(rules)) // "btw".length + 1
    }

    @Test
    fun `bufferLengthNeeded returns one for empty rules`() {
        assertEquals(1, HotstringMatcher.bufferLengthNeeded(emptyList()))
    }
}
