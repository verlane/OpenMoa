package pe.aioo.openmoa.hangul

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HangulAssemblerTest {

    @Test
    fun testAssembleSimpleHangul() {
        val assembler = HangulAssembler()
        assertNull(assembler.appendJamo("ㄴ"))
        assertEquals("ㄴ", assembler.getUnresolved())
        assertNull(assembler.appendJamo("ㅡ"))
        assertEquals("느", assembler.getUnresolved())
        assertNull(assembler.appendJamo("ㆍ"))
        assertEquals("누", assembler.getUnresolved())
        assertNull(assembler.appendJamo("ㆍ"))
        assertEquals("뉴", assembler.getUnresolved())
        assertNull(assembler.appendJamo("ㅇ"))
        assertEquals("늉", assembler.getUnresolved())
        assertEquals("늉", assembler.appendJamo("ㄱ"))
        assertEquals("ㄱ", assembler.getUnresolved())
    }

    @Test
    fun testAssembleSimpleHangulWithAraea() {
        val assembler = HangulAssembler()
        assertNull(assembler.appendJamo("ㄴ"))
        assertEquals("ㄴ", assembler.getUnresolved())
        assertNull(assembler.appendJamo("ㆍ"))
        assertEquals("ㄴㆍ", assembler.getUnresolved())
        assertNull(assembler.appendJamo("ㆍ"))
        assertEquals("ㄴᆢ", assembler.getUnresolved())
        assertNull(assembler.appendJamo("ㅣ"))
        assertEquals("녀", assembler.getUnresolved())
    }

    @Test
    fun testAssembleComplexHangul() {
        val assembler = HangulAssembler()
        assertNull(assembler.appendJamo("ㅂ"))
        assertEquals("ㅂ", assembler.getUnresolved())
        assertNull(assembler.appendJamo("ㅜ"))
        assertEquals("부", assembler.getUnresolved())
        assertNull(assembler.appendJamo("ㆍ"))
        assertEquals("뷰", assembler.getUnresolved())
        assertNull(assembler.appendJamo("ㅣ"))
        assertEquals("붜", assembler.getUnresolved())
        assertNull(assembler.appendJamo("ㅣ"))
        assertEquals("붸", assembler.getUnresolved())
        assertNull(assembler.appendJamo("ㄹ"))
        assertEquals("뷀", assembler.getUnresolved())
        assertNull(assembler.appendJamo("ㄱ"))
        assertEquals("뷁", assembler.getUnresolved())
        assertEquals("뷁", assembler.appendJamo("ㄱ"))
        assertEquals("ㄱ", assembler.getUnresolved())
    }

    @Test
    fun testDisassembleJongSeong() {
        val assembler = HangulAssembler()
        assertNull(assembler.appendJamo("ㅂ"))
        assertNull(assembler.appendJamo("ㅜ"))
        assertNull(assembler.appendJamo("ㆍ"))
        assertNull(assembler.appendJamo("ㅣ"))
        assertNull(assembler.appendJamo("ㅣ"))
        assertNull(assembler.appendJamo("ㄹ"))
        assertNull(assembler.appendJamo("ㄱ"))
        assertEquals("뷀", assembler.appendJamo("ㅣ"))
        assertEquals("기", assembler.getUnresolved())
    }

    @Test
    fun testDisassembleJongSeongWithAraea() {
        val assembler = HangulAssembler()
        assertNull(assembler.appendJamo("ㅂ"))
        assertNull(assembler.appendJamo("ㅜ"))
        assertNull(assembler.appendJamo("ㆍ"))
        assertNull(assembler.appendJamo("ㅣ"))
        assertNull(assembler.appendJamo("ㅣ"))
        assertNull(assembler.appendJamo("ㄹ"))
        assertNull(assembler.appendJamo("ㄱ"))
        assertEquals("뷀", assembler.appendJamo("ㆍ"))
        assertEquals("ㄱㆍ", assembler.getUnresolved())
    }

    // --- 복합 종성 조합 ---

    @Test
    fun `assembleLastJongseong - ㄱ+ㅅ = ㄳ`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄱ")
        assertNull(assembler.appendJamo("ㅅ"))
        assertEquals("갃", assembler.getUnresolved())
    }

    @Test
    fun `assembleLastJongseong - ㄴ+ㅈ = ㄵ`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄴ")
        assertNull(assembler.appendJamo("ㅈ"))
        assertEquals("갅", assembler.getUnresolved())
    }

    @Test
    fun `assembleLastJongseong - ㄴ+ㅎ = ㄶ`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄴ")
        assertNull(assembler.appendJamo("ㅎ"))
        assertEquals("갆", assembler.getUnresolved())
    }

    @Test
    fun `assembleLastJongseong - ㄹ+ㄱ = ㄺ`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄹ")
        assertNull(assembler.appendJamo("ㄱ"))
        assertEquals("갉", assembler.getUnresolved())
    }

    @Test
    fun `assembleLastJongseong - ㄹ+ㅁ = ㄻ`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄹ")
        assertNull(assembler.appendJamo("ㅁ"))
        assertEquals("갊", assembler.getUnresolved())
    }

    @Test
    fun `assembleLastJongseong - ㄹ+ㅂ = ㄼ`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄹ")
        assertNull(assembler.appendJamo("ㅂ"))
        assertEquals("갋", assembler.getUnresolved())
    }

    @Test
    fun `assembleLastJongseong - ㄹ+ㅅ = ㄽ`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄹ")
        assertNull(assembler.appendJamo("ㅅ"))
        assertEquals("갌", assembler.getUnresolved())
    }

    @Test
    fun `assembleLastJongseong - ㄹ+ㅌ = ㄾ`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄹ")
        assertNull(assembler.appendJamo("ㅌ"))
        assertEquals("갍", assembler.getUnresolved())
    }

    @Test
    fun `assembleLastJongseong - ㄹ+ㅍ = ㄿ`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄹ")
        assertNull(assembler.appendJamo("ㅍ"))
        assertEquals("갎", assembler.getUnresolved())
    }

    @Test
    fun `assembleLastJongseong - ㄹ+ㅎ = ㅀ`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄹ")
        assertNull(assembler.appendJamo("ㅎ"))
        assertEquals("갏", assembler.getUnresolved())
    }

    @Test
    fun `assembleLastJongseong - ㅂ+ㅅ = ㅄ`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㅂ")
        assertNull(assembler.appendJamo("ㅅ"))
        assertEquals("값", assembler.getUnresolved())
    }

    // --- removeLastJamo ---

    @Test
    fun `removeLastJamo - 빈 상태에서 호출해도 아무 변화 없음`() {
        val assembler = HangulAssembler()
        assembler.removeLastJamo()
        assertNull(assembler.getUnresolved())
    }

    @Test
    fun `removeLastJamo - 자음만 있을 때 제거`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.removeLastJamo()
        assertNull(assembler.getUnresolved())
    }

    @Test
    fun `removeLastJamo - 직접 입력된 가에서 백스페이스 시 전체 초기화`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.removeLastJamo()
        assertNull(assembler.getUnresolved())
    }

    @Test
    fun `removeLastJamo - 직접 입력된 느에서 백스페이스 시 전체 초기화`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄴ")
        assembler.appendJamo("ㅡ")
        assertEquals("느", assembler.getUnresolved())
        assembler.removeLastJamo()
        assertNull(assembler.getUnresolved())
    }

    @Test
    fun `removeLastJamo - 종성 제거 후 가 상태에서 백스페이스 시 ㄱ 남음`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄴ")
        assembler.removeLastJamo()
        assertEquals("가", assembler.getUnresolved())
        assembler.removeLastJamo()
        assertEquals("ㄱ", assembler.getUnresolved())
    }

    @Test
    fun `removeLastJamo - 복합 종성에서 마지막 자음만 제거`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄹ")
        assembler.appendJamo("ㄱ")
        assertEquals("갉", assembler.getUnresolved())
        assembler.removeLastJamo()
        assertEquals("갈", assembler.getUnresolved())
    }

    @Test
    fun `removeLastJamo - 갉에서 연속 백스페이스 시 갈 가 ㄱ 순으로 분해`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄹ")
        assembler.appendJamo("ㄱ")
        assertEquals("갉", assembler.getUnresolved())
        assembler.removeLastJamo()
        assertEquals("갈", assembler.getUnresolved())
        assembler.removeLastJamo()
        assertEquals("가", assembler.getUnresolved())
        assembler.removeLastJamo()
        assertEquals("ㄱ", assembler.getUnresolved())
        assembler.removeLastJamo()
        assertNull(assembler.getUnresolved())
    }

    @Test
    fun `removeLastJamo - 겹받침 ㄳ 분해`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅅ")
        assertEquals("갃", assembler.getUnresolved())
        assembler.removeLastJamo()
        assertEquals("각", assembler.getUnresolved())
    }

    @Test
    fun `removeLastJamo - 겹받침 ㄵ 분해`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄴ")
        assembler.appendJamo("ㅈ")
        assertEquals("갅", assembler.getUnresolved())
        assembler.removeLastJamo()
        assertEquals("간", assembler.getUnresolved())
    }

    @Test
    fun `removeLastJamo - 겹받침 ㄶ 분해`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄴ")
        assembler.appendJamo("ㅎ")
        assertEquals("갆", assembler.getUnresolved())
        assembler.removeLastJamo()
        assertEquals("간", assembler.getUnresolved())
    }

    @Test
    fun `removeLastJamo - 겹받침 ㄻ 분해`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄹ")
        assembler.appendJamo("ㅁ")
        assertEquals("갊", assembler.getUnresolved())
        assembler.removeLastJamo()
        assertEquals("갈", assembler.getUnresolved())
    }

    @Test
    fun `removeLastJamo - 겹받침 ㅄ 분해`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㅂ")
        assembler.appendJamo("ㅅ")
        assertEquals("값", assembler.getUnresolved())
        assembler.removeLastJamo()
        assertEquals("갑", assembler.getUnresolved())
    }

    // --- clear ---

    @Test
    fun `clear - 조합 중 호출 시 상태 초기화`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.clear()
        assertNull(assembler.getUnresolved())
    }

    @Test
    fun `clear - 빈 상태에서 호출해도 문제없음`() {
        val assembler = HangulAssembler()
        assembler.clear()
        assertNull(assembler.getUnresolved())
    }

    // --- getUnresolved ---

    @Test
    fun `getUnresolved - 빈 상태에서 null 반환`() {
        assertNull(HangulAssembler().getUnresolved())
    }

    // --- previewWithAppended ---

    @Test
    fun `previewWithAppended - 빈 상태에서 자음 누르면 자음 단독 반환`() {
        val assembler = HangulAssembler()
        assertEquals("ㄴ", assembler.previewWithAppended("ㄴ"))
        assertNull(assembler.getUnresolved())
    }

    @Test
    fun `previewWithAppended - 고 조합 중 ㅁ 누르면 곰 반환`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅗ")
        assertEquals("곰", assembler.previewWithAppended("ㅁ"))
        assertEquals("고", assembler.getUnresolved())
    }

    @Test
    fun `previewWithAppended - 각 조합 중 ㅅ 누르면 복합 종성 갃 반환`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄱ")
        assertEquals("갃", assembler.previewWithAppended("ㅅ"))
        assertEquals("각", assembler.getUnresolved())
    }

    @Test
    fun `previewWithAppended - 각 상태에서 ㅏ 누르면 조합 완성되며 가 반환`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅏ")
        assembler.appendJamo("ㄱ")
        assertEquals("가", assembler.previewWithAppended("ㅏ"))
        assertEquals("각", assembler.getUnresolved())
    }

    @Test
    fun `previewWithAppended - 호출 후 조합 상태가 변경되지 않음`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅗ")
        assembler.previewWithAppended("ㅁ")
        assertEquals("고", assembler.getUnresolved())
        assembler.previewWithAppended("ㄴ")
        assertEquals("고", assembler.getUnresolved())
    }

    @Test
    fun `previewWithAppended - 연속 호출해도 매번 동일한 결과 반환`() {
        val assembler = HangulAssembler()
        assembler.appendJamo("ㄱ")
        assembler.appendJamo("ㅗ")
        assertEquals("곰", assembler.previewWithAppended("ㅁ"))
        assertEquals("곰", assembler.previewWithAppended("ㅁ"))
    }

}
