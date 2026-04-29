package pe.aioo.openmoa.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HangulInputModeTest {

    // --- fromString ---

    @Test
    fun `fromString null이면 TWO_HAND_MOAKEY`() {
        assertEquals(HangulInputMode.TWO_HAND_MOAKEY, HangulInputMode.fromString(null))
    }

    @Test
    fun `fromString 알 수 없는 값이면 TWO_HAND_MOAKEY`() {
        assertEquals(HangulInputMode.TWO_HAND_MOAKEY, HangulInputMode.fromString("FOO"))
    }

    @Test
    fun `fromString MOAKEY`() {
        assertEquals(HangulInputMode.MOAKEY, HangulInputMode.fromString("MOAKEY"))
    }

    @Test
    fun `fromString MOAKEY_PLUS`() {
        assertEquals(HangulInputMode.MOAKEY_PLUS, HangulInputMode.fromString("MOAKEY_PLUS"))
    }

    @Test
    fun `fromString QWERTY`() {
        assertEquals(HangulInputMode.QWERTY, HangulInputMode.fromString("QWERTY"))
    }

    @Test
    fun `fromString QWERTY_SIMPLE`() {
        assertEquals(HangulInputMode.QWERTY_SIMPLE, HangulInputMode.fromString("QWERTY_SIMPLE"))
    }

    // --- isMoakeyLayout ---

    @Test
    fun `TWO_HAND_MOAKEY는 모아키 레이아웃 아님`() {
        assertFalse(HangulInputMode.TWO_HAND_MOAKEY.isMoakeyLayout)
    }

    @Test
    fun `MOAKEY는 모아키 레이아웃`() {
        assertTrue(HangulInputMode.MOAKEY.isMoakeyLayout)
    }

    @Test
    fun `MOAKEY_PLUS는 모아키 레이아웃`() {
        assertTrue(HangulInputMode.MOAKEY_PLUS.isMoakeyLayout)
    }

    @Test
    fun `QWERTY는 모아키 레이아웃 아님`() {
        assertFalse(HangulInputMode.QWERTY.isMoakeyLayout)
    }

    // --- isQwertyLayout ---

    @Test
    fun `TWO_HAND_MOAKEY는 쿼티 아님`() {
        assertFalse(HangulInputMode.TWO_HAND_MOAKEY.isQwertyLayout)
    }

    @Test
    fun `MOAKEY는 쿼티 아님`() {
        assertFalse(HangulInputMode.MOAKEY.isQwertyLayout)
    }

    @Test
    fun `QWERTY는 쿼티`() {
        assertTrue(HangulInputMode.QWERTY.isQwertyLayout)
    }

    @Test
    fun `QWERTY_SIMPLE도 쿼티`() {
        assertTrue(HangulInputMode.QWERTY_SIMPLE.isQwertyLayout)
    }

    // --- isSimpleQwerty ---

    @Test
    fun `QWERTY는 단모음 아님`() {
        assertFalse(HangulInputMode.QWERTY.isSimpleQwerty)
    }

    @Test
    fun `QWERTY_SIMPLE은 단모음`() {
        assertTrue(HangulInputMode.QWERTY_SIMPLE.isSimpleQwerty)
    }

    @Test
    fun `MOAKEY는 단모음 아님`() {
        assertFalse(HangulInputMode.MOAKEY.isSimpleQwerty)
    }

    // --- showsMoeumKey ---

    @Test
    fun `TWO_HAND_MOAKEY는 모음키 표시`() {
        assertTrue(HangulInputMode.TWO_HAND_MOAKEY.showsMoeumKey)
    }

    @Test
    fun `MOAKEY는 모음키 표시`() {
        assertTrue(HangulInputMode.MOAKEY.showsMoeumKey)
    }

    @Test
    fun `MOAKEY_PLUS는 모음키 미표시`() {
        assertFalse(HangulInputMode.MOAKEY_PLUS.showsMoeumKey)
    }

    @Test
    fun `QWERTY는 모음키 미표시`() {
        assertFalse(HangulInputMode.QWERTY.showsMoeumKey)
    }

    @Test
    fun `QWERTY_SIMPLE은 모음키 미표시`() {
        assertFalse(HangulInputMode.QWERTY_SIMPLE.showsMoeumKey)
    }
}
