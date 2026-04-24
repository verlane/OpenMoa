package pe.aioo.openmoa.hangul

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoeumGestureProcessorTest {

    private fun resolve(vararg moeums: String): String? {
        val processor = MoeumGestureProcessor()
        moeums.forEach { processor.appendMoeum(it) }
        return processor.resolveMoeumList()
    }

    // --- 빈 상태 ---

    @Test
    fun `빈 상태에서 resolveMoeumList는 null 반환`() {
        assertNull(MoeumGestureProcessor().resolveMoeumList())
    }

    // --- 단일 모음 ---

    @Test
    fun `단일 ㅏ`() = assertEquals("ㅏ", resolve("ㅏ"))

    @Test
    fun `단일 ㅓ`() = assertEquals("ㅓ", resolve("ㅓ"))

    @Test
    fun `단일 ㅗ`() = assertEquals("ㅗ", resolve("ㅗ"))

    @Test
    fun `단일 ㅜ`() = assertEquals("ㅜ", resolve("ㅜ"))

    @Test
    fun `단일 ㅡL은 ㅡ 반환`() = assertEquals("ㅡ", resolve("ㅡL"))

    @Test
    fun `단일 ㅡR은 ㅡ 반환`() = assertEquals("ㅡ", resolve("ㅡR"))

    @Test
    fun `단일 ㅣL은 ㅣ 반환`() = assertEquals("ㅣ", resolve("ㅣL"))

    @Test
    fun `단일 ㅣR은 ㅣ 반환`() = assertEquals("ㅣ", resolve("ㅣR"))

    // --- 2단계 strict 전이 ---

    @Test
    fun `ㅏ+ㅓ = ㅐ`() = assertEquals("ㅐ", resolve("ㅏ", "ㅓ"))

    @Test
    fun `ㅓ+ㅏ = ㅔ`() = assertEquals("ㅔ", resolve("ㅓ", "ㅏ"))

    @Test
    fun `ㅗ+ㅏ = ㅘ`() = assertEquals("ㅘ", resolve("ㅗ", "ㅏ"))

    @Test
    fun `ㅗ+ㅜ = ㅚ`() = assertEquals("ㅚ", resolve("ㅗ", "ㅜ"))

    @Test
    fun `ㅘ+ㅓ = ㅙ`() = assertEquals("ㅙ", resolve("ㅗ", "ㅏ", "ㅓ"))

    @Test
    fun `ㅜ+ㅓ = ㅝ`() = assertEquals("ㅝ", resolve("ㅜ", "ㅓ"))

    @Test
    fun `ㅜ+ㅗ = ㅟ`() = assertEquals("ㅟ", resolve("ㅜ", "ㅗ"))

    @Test
    fun `ㅝ+ㅏ = ㅞ`() = assertEquals("ㅞ", resolve("ㅜ", "ㅓ", "ㅏ"))

    @Test
    fun `ㅡL+ㅣL = ㅢ`() = assertEquals("ㅢ", resolve("ㅡL", "ㅣL"))

    @Test
    fun `ㅡR+ㅣR = ㅢ`() = assertEquals("ㅢ", resolve("ㅡR", "ㅣR"))

    // --- 3단계 strict 전이 ---

    @Test
    fun `ㅏ+ㅓ+ㅏ = ㅑ`() = assertEquals("ㅑ", resolve("ㅏ", "ㅓ", "ㅏ"))

    @Test
    fun `ㅓ+ㅏ+ㅓ = ㅕ`() = assertEquals("ㅕ", resolve("ㅓ", "ㅏ", "ㅓ"))

    @Test
    fun `ㅑ+ㅓ = ㅒ`() = assertEquals("ㅒ", resolve("ㅏ", "ㅓ", "ㅏ", "ㅓ"))

    @Test
    fun `ㅕ+ㅏ = ㅖ`() = assertEquals("ㅖ", resolve("ㅓ", "ㅏ", "ㅓ", "ㅏ"))

    @Test
    fun `ㅚ+ㅏ = ㅘ`() = assertEquals("ㅘ", resolve("ㅗ", "ㅜ", "ㅏ"))

    @Test
    fun `ㅚ+ㅗ = ㅛ`() = assertEquals("ㅛ", resolve("ㅗ", "ㅜ", "ㅗ"))

    // --- non-strict 전이 ---

    @Test
    fun `ㅏ+ㅗ = ㅐ (non-strict)`() = assertEquals("ㅐ", resolve("ㅏ", "ㅗ"))

    @Test
    fun `ㅏ+ㅜ = ㅐ (non-strict)`() = assertEquals("ㅐ", resolve("ㅏ", "ㅜ"))

    @Test
    fun `ㅓ+ㅗ = ㅔ (non-strict)`() = assertEquals("ㅔ", resolve("ㅓ", "ㅗ"))

    @Test
    fun `ㅗ+ㅡL = ㅚ (non-strict)`() = assertEquals("ㅚ", resolve("ㅗ", "ㅡL"))

    @Test
    fun `ㅗ+ㅡR = ㅚ (non-strict)`() = assertEquals("ㅚ", resolve("ㅗ", "ㅡR"))

    @Test
    fun `ㅜ+ㅣL = ㅟ (non-strict)`() = assertEquals("ㅟ", resolve("ㅜ", "ㅣL"))

    @Test
    fun `ㅡL+ㅓ+ㅓ = ㅓ (ㅡ축 left경로)`() = assertEquals("ㅓ", resolve("ㅡL", "ㅓ", "ㅓ"))

    @Test
    fun `ㅡR+ㅏ+ㅏ = ㅏ (ㅡ축 right경로)`() = assertEquals("ㅏ", resolve("ㅡR", "ㅏ", "ㅏ"))

    // --- clear ---

    @Test
    fun `clear 후 resolveMoeumList는 null 반환`() {
        val processor = MoeumGestureProcessor()
        processor.appendMoeum("ㅏ")
        processor.appendMoeum("ㅓ")
        processor.clear()
        assertNull(processor.resolveMoeumList())
    }

}
