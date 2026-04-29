package pe.aioo.openmoa.hardware

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HardwareDubeolsikMapperTest {

    @Test
    fun `소문자 자음 매핑 검증`() {
        assertEquals("ㅂ", HardwareDubeolsikMapper.toJamo('q', shift = false))
        assertEquals("ㅈ", HardwareDubeolsikMapper.toJamo('w', shift = false))
        assertEquals("ㄷ", HardwareDubeolsikMapper.toJamo('e', shift = false))
        assertEquals("ㄱ", HardwareDubeolsikMapper.toJamo('r', shift = false))
        assertEquals("ㅅ", HardwareDubeolsikMapper.toJamo('t', shift = false))
        assertEquals("ㅁ", HardwareDubeolsikMapper.toJamo('a', shift = false))
        assertEquals("ㄴ", HardwareDubeolsikMapper.toJamo('s', shift = false))
        assertEquals("ㅎ", HardwareDubeolsikMapper.toJamo('g', shift = false))
        assertEquals("ㅋ", HardwareDubeolsikMapper.toJamo('z', shift = false))
    }

    @Test
    fun `소문자 모음 매핑 검증`() {
        assertEquals("ㅛ", HardwareDubeolsikMapper.toJamo('y', shift = false))
        assertEquals("ㅕ", HardwareDubeolsikMapper.toJamo('u', shift = false))
        assertEquals("ㅑ", HardwareDubeolsikMapper.toJamo('i', shift = false))
        assertEquals("ㅐ", HardwareDubeolsikMapper.toJamo('o', shift = false))
        assertEquals("ㅔ", HardwareDubeolsikMapper.toJamo('p', shift = false))
        assertEquals("ㅗ", HardwareDubeolsikMapper.toJamo('h', shift = false))
        assertEquals("ㅓ", HardwareDubeolsikMapper.toJamo('j', shift = false))
        assertEquals("ㅏ", HardwareDubeolsikMapper.toJamo('k', shift = false))
        assertEquals("ㅣ", HardwareDubeolsikMapper.toJamo('l', shift = false))
        assertEquals("ㅡ", HardwareDubeolsikMapper.toJamo('m', shift = false))
    }

    @Test
    fun `Shift - 쌍자음과 ㅒ ㅖ`() {
        assertEquals("ㅃ", HardwareDubeolsikMapper.toJamo('q', shift = true))
        assertEquals("ㅉ", HardwareDubeolsikMapper.toJamo('w', shift = true))
        assertEquals("ㄸ", HardwareDubeolsikMapper.toJamo('e', shift = true))
        assertEquals("ㄲ", HardwareDubeolsikMapper.toJamo('r', shift = true))
        assertEquals("ㅆ", HardwareDubeolsikMapper.toJamo('t', shift = true))
        assertEquals("ㅒ", HardwareDubeolsikMapper.toJamo('o', shift = true))
        assertEquals("ㅖ", HardwareDubeolsikMapper.toJamo('p', shift = true))
    }

    @Test
    fun `Shift - 쌍자음 외 키는 normal과 동일`() {
        assertEquals("ㅁ", HardwareDubeolsikMapper.toJamo('a', shift = true))
        assertEquals("ㅎ", HardwareDubeolsikMapper.toJamo('g', shift = true))
        assertEquals("ㅑ", HardwareDubeolsikMapper.toJamo('i', shift = true))
    }

    @Test
    fun `대문자 입력은 자동으로 shift로 처리`() {
        assertEquals("ㅃ", HardwareDubeolsikMapper.toJamo('Q', shift = false))
        assertEquals("ㅁ", HardwareDubeolsikMapper.toJamo('A', shift = false))
    }

    @Test
    fun `매핑되지 않는 키는 null`() {
        assertNull(HardwareDubeolsikMapper.toJamo('1', shift = false))
        assertNull(HardwareDubeolsikMapper.toJamo(' ', shift = false))
        assertNull(HardwareDubeolsikMapper.toJamo(',', shift = false))
    }
}
