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

    @Test
    fun `isChosung returns true for basic consonants`() {
        assertTrue(HangulSyllable.isChosung('ㄱ'))
        assertTrue(HangulSyllable.isChosung('ㄴ'))
        assertTrue(HangulSyllable.isChosung('ㅎ'))
    }

    @Test
    fun `isChosung returns false for vowels and non-jamo`() {
        assertFalse(HangulSyllable.isChosung('ㅏ'))
        assertFalse(HangulSyllable.isChosung('가'))
        assertFalse(HangulSyllable.isChosung('a'))
    }

    @Test
    fun `isAllChosung returns true for all consonants`() {
        assertTrue(HangulSyllable.isAllChosung("ㄱㄱㅁ"))
    }

    @Test
    fun `isAllChosung returns false for empty string`() {
        assertFalse(HangulSyllable.isAllChosung(""))
    }

    @Test
    fun `isAllChosung returns false when mixed with syllable`() {
        assertFalse(HangulSyllable.isAllChosung("ㄱ가"))
    }

    @Test
    fun `getChosung extracts correct initial consonant`() {
        assertEquals('ㄱ', HangulSyllable.getChosung('고'))
        assertEquals('ㄱ', HangulSyllable.getChosung('구'))
        assertEquals('ㅁ', HangulSyllable.getChosung('마'))
        assertEquals('ㅎ', HangulSyllable.getChosung('힣'))
    }

    @Test
    fun `getChosung returns null for non-syllable`() {
        assertEquals(null, HangulSyllable.getChosung('ㄱ'))
        assertEquals(null, HangulSyllable.getChosung('a'))
    }

    @Test
    fun `syllableRangeForChosung covers all variants for ㄱ`() {
        val range = HangulSyllable.syllableRangeForChosung('ㄱ')
        assertTrue('가' in range)
        assertTrue('기' in range)
        assertTrue('깊' in range)
        assertFalse('나' in range)
    }

    @Test
    fun `syllableRangeForChosung returns empty for non-chosung`() {
        assertTrue(HangulSyllable.syllableRangeForChosung('ㅏ').isEmpty())
    }

    @Test
    fun `matchesChosungPattern matches 고구마 with ㄱㄱㅁ`() {
        assertTrue(HangulSyllable.matchesChosungPattern("고구마", "ㄱㄱㅁ"))
    }

    @Test
    fun `matchesChosungPattern rejects word shorter than pattern`() {
        assertFalse(HangulSyllable.matchesChosungPattern("고구", "ㄱㄱㅁ"))
    }

    @Test
    fun `matchesChosungPattern allows word longer than pattern`() {
        assertTrue(HangulSyllable.matchesChosungPattern("고구마밭", "ㄱㄱㅁ"))
    }
}
