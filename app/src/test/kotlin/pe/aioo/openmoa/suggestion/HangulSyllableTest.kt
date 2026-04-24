package pe.aioo.openmoa.suggestion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HangulSyllableTest {

    @Test
    fun `isHangulSyllable returns true for 가`() {
        assertTrue(HangulSyllable.isHangulSyllable('가'))
    }

    @Test
    fun `isHangulSyllable returns true for 힣`() {
        assertTrue(HangulSyllable.isHangulSyllable('힣'))
    }

    @Test
    fun `isHangulSyllable returns false for standalone jamo`() {
        assertFalse(HangulSyllable.isHangulSyllable('ㄱ'))
        assertFalse(HangulSyllable.isHangulSyllable('ㅏ'))
    }

    @Test
    fun `isHangulSyllable returns false for ascii`() {
        assertFalse(HangulSyllable.isHangulSyllable('A'))
    }

    @Test
    fun `hasJongseong returns false for 바`() {
        assertFalse(HangulSyllable.hasJongseong('바'))
    }

    @Test
    fun `hasJongseong returns false for 가`() {
        assertFalse(HangulSyllable.hasJongseong('가'))
    }

    @Test
    fun `hasJongseong returns true for 방`() {
        assertTrue(HangulSyllable.hasJongseong('방'))
    }

    @Test
    fun `hasJongseong returns true for 닭 (complex jongseong)`() {
        assertTrue(HangulSyllable.hasJongseong('닭'))
    }

    @Test
    fun `syllableBase removes jongseong from 방`() {
        assertEquals('바', HangulSyllable.syllableBase('방'))
    }

    @Test
    fun `syllableBase keeps 바 unchanged`() {
        assertEquals('바', HangulSyllable.syllableBase('바'))
    }

    @Test
    fun `syllableBase removes complex jongseong from 밝`() {
        assertEquals('바', HangulSyllable.syllableBase('밝'))
    }

    @Test
    fun `jongseongRange covers all 28 variants from 바`() {
        val range = HangulSyllable.jongseongRange('바')
        assertEquals('바', range.first)
        assertEquals('바' + 27, range.last)
        assertTrue('방' in range)
        assertTrue('발' in range)
        assertTrue('박' in range)
    }

    @Test
    fun `jongseongRange from syllable with jongseong uses its base`() {
        val range = HangulSyllable.jongseongRange('방')
        assertEquals('바', range.first)
        assertTrue('방' in range)
    }
}
