package pe.aioo.openmoa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IMEModeExtTest {

    @Test
    fun `한국어 계열 모드는 isKoreanFamily true 반환`() {
        assertTrue(IMEMode.IME_KO.isKoreanFamily())
        assertTrue(IMEMode.IME_KO_ARROW.isKoreanFamily())
        assertTrue(IMEMode.IME_KO_PHONE.isKoreanFamily())
        assertTrue(IMEMode.IME_KO_PUNCTUATION.isKoreanFamily())
        assertTrue(IMEMode.IME_KO_NUMBER.isKoreanFamily())
        assertTrue(IMEMode.IME_EMOJI.isKoreanFamily())
    }

    @Test
    fun `영어 계열 모드는 isKoreanFamily false 반환`() {
        assertFalse(IMEMode.IME_EN.isKoreanFamily())
        assertFalse(IMEMode.IME_EN_ARROW.isKoreanFamily())
        assertFalse(IMEMode.IME_EN_PHONE.isKoreanFamily())
        assertFalse(IMEMode.IME_EN_PUNCTUATION.isKoreanFamily())
        assertFalse(IMEMode.IME_EN_NUMBER.isKoreanFamily())
    }

    @Test
    fun `토글 - 기본 모드는 반대 언어로 전환`() {
        assertEquals(IMEMode.IME_EN, IMEMode.IME_KO.resolveLanguageSwitchTarget())
        assertEquals(IMEMode.IME_KO, IMEMode.IME_EN.resolveLanguageSwitchTarget())
    }

    @Test
    fun `토글 - 한국어 계열 보조 모드는 IME_KO로 복귀`() {
        assertEquals(IMEMode.IME_KO, IMEMode.IME_KO_PUNCTUATION.resolveLanguageSwitchTarget())
        assertEquals(IMEMode.IME_KO, IMEMode.IME_KO_NUMBER.resolveLanguageSwitchTarget())
        assertEquals(IMEMode.IME_KO, IMEMode.IME_KO_ARROW.resolveLanguageSwitchTarget())
        assertEquals(IMEMode.IME_KO, IMEMode.IME_KO_PHONE.resolveLanguageSwitchTarget())
        assertEquals(IMEMode.IME_KO, IMEMode.IME_EMOJI.resolveLanguageSwitchTarget())
    }

    @Test
    fun `토글 - 영어 계열 보조 모드는 IME_EN으로 복귀`() {
        assertEquals(IMEMode.IME_EN, IMEMode.IME_EN_PUNCTUATION.resolveLanguageSwitchTarget())
        assertEquals(IMEMode.IME_EN, IMEMode.IME_EN_NUMBER.resolveLanguageSwitchTarget())
        assertEquals(IMEMode.IME_EN, IMEMode.IME_EN_ARROW.resolveLanguageSwitchTarget())
        assertEquals(IMEMode.IME_EN, IMEMode.IME_EN_PHONE.resolveLanguageSwitchTarget())
    }
}
