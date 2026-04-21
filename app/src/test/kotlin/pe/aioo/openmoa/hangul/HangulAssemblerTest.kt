package pe.aioo.openmoa.hangul

import org.junit.Assert
import org.junit.Test

// FIXME: Add more tests
class HangulAssemblerTest {

    @Test
    fun testAssembleSimpleHangul() {
        val assembler = HangulAssembler()
        Assert.assertNull(assembler.appendJamo("ㄴ"))
        Assert.assertEquals("ㄴ", assembler.getUnresolved())
        Assert.assertNull(assembler.appendJamo("ㅡ"))
        Assert.assertEquals("느", assembler.getUnresolved())
        Assert.assertNull(assembler.appendJamo("ㆍ"))
        Assert.assertEquals("누", assembler.getUnresolved())
        Assert.assertNull(assembler.appendJamo("ㆍ"))
        Assert.assertEquals("뉴", assembler.getUnresolved())
        Assert.assertNull(assembler.appendJamo("ㅇ"))
        Assert.assertEquals("늉", assembler.getUnresolved())
        Assert.assertEquals("늉", assembler.appendJamo("ㄱ"))
        Assert.assertEquals("ㄱ", assembler.getUnresolved())
    }

    @Test
    fun testAssembleSimpleHangulWithAraea() {
        val assembler = HangulAssembler()
        Assert.assertNull(assembler.appendJamo("ㄴ"))
        Assert.assertEquals("ㄴ", assembler.getUnresolved())
        Assert.assertNull(assembler.appendJamo("ㆍ"))
        Assert.assertEquals("ㄴㆍ", assembler.getUnresolved())
        Assert.assertNull(assembler.appendJamo("ㆍ"))
        Assert.assertEquals("ㄴᆢ", assembler.getUnresolved())
        Assert.assertNull(assembler.appendJamo("ㅣ"))
        Assert.assertEquals("녀", assembler.getUnresolved())
    }

    @Test
    fun testAssembleComplexHangul() {
        val assembler = HangulAssembler()
        Assert.assertNull(assembler.appendJamo("ㅂ"))
        Assert.assertEquals("ㅂ", assembler.getUnresolved())
        Assert.assertNull(assembler.appendJamo("ㅜ"))
        Assert.assertEquals("부", assembler.getUnresolved())
        Assert.assertNull(assembler.appendJamo("ㆍ"))
        Assert.assertEquals("뷰", assembler.getUnresolved())
        Assert.assertNull(assembler.appendJamo("ㅣ"))
        Assert.assertEquals("붜", assembler.getUnresolved())
        Assert.assertNull(assembler.appendJamo("ㅣ"))
        Assert.assertEquals("붸", assembler.getUnresolved())
        Assert.assertNull(assembler.appendJamo("ㄹ"))
        Assert.assertEquals("뷀", assembler.getUnresolved())
        Assert.assertNull(assembler.appendJamo("ㄱ"))
        Assert.assertEquals("뷁", assembler.getUnresolved())
        Assert.assertEquals("뷁", assembler.appendJamo("ㄱ"))
        Assert.assertEquals("ㄱ", assembler.getUnresolved())
    }

    @Test
    fun testDisassembleJongSeong() {
        val assembler = HangulAssembler()
        Assert.assertNull(assembler.appendJamo("ㅂ"))
        Assert.assertNull(assembler.appendJamo("ㅜ"))
        Assert.assertNull(assembler.appendJamo("ㆍ"))
        Assert.assertNull(assembler.appendJamo("ㅣ"))
        Assert.assertNull(assembler.appendJamo("ㅣ"))
        Assert.assertNull(assembler.appendJamo("ㄹ"))
        Assert.assertNull(assembler.appendJamo("ㄱ"))
        Assert.assertEquals("뷀", assembler.appendJamo("ㅣ"))
        Assert.assertEquals("기", assembler.getUnresolved())
    }

    @Test
    fun testDisassembleJongSeongWithAraea() {
        val assembler = HangulAssembler()
        Assert.assertNull(assembler.appendJamo("ㅂ"))
        Assert.assertNull(assembler.appendJamo("ㅜ"))
        Assert.assertNull(assembler.appendJamo("ㆍ"))
        Assert.assertNull(assembler.appendJamo("ㅣ"))
        Assert.assertNull(assembler.appendJamo("ㅣ"))
        Assert.assertNull(assembler.appendJamo("ㄹ"))
        Assert.assertNull(assembler.appendJamo("ㄱ"))
        Assert.assertEquals("뷀", assembler.appendJamo("ㆍ"))
        Assert.assertEquals("ㄱㆍ", assembler.getUnresolved())
    }

    @Test
    fun `previewWithAppended - 빈 상태에서 자음 누르면 자음 단독 반환`() {
        val assembler = HangulAssembler()
        Assert.assertEquals("ㄴ", assembler.previewWithAppended("ㄴ"))
        Assert.assertNull(assembler.getUnresolved())
    }

    @Test
    fun `previewWithAppended - 고 조합 중 ㅁ 누르면 곰 반환`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅗ")
        Assert.assertEquals("곰", assembler.previewWithAppended("ㅁ"))
        Assert.assertEquals("고", assembler.getUnresolved())
    }

    @Test
    fun `previewWithAppended - 각 조합 중 ㅅ 누르면 복합 종성 갃 반환`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄱ")
        Assert.assertEquals("갃", assembler.previewWithAppended("ㅅ"))
        Assert.assertEquals("각", assembler.getUnresolved())
    }

    @Test
    fun `previewWithAppended - 호출 후 조합 상태가 변경되지 않음`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅗ")
        assembler.previewWithAppended("ㅁ")
        Assert.assertEquals("고", assembler.getUnresolved())
        assembler.previewWithAppended("ㄴ")
        Assert.assertEquals("고", assembler.getUnresolved())
    }

    @Test
    fun `previewWithAppended - 연속 호출해도 매번 동일한 결과 반환`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅗ")
        Assert.assertEquals("곰", assembler.previewWithAppended("ㅁ"))
        Assert.assertEquals("곰", assembler.previewWithAppended("ㅁ"))
    }

}