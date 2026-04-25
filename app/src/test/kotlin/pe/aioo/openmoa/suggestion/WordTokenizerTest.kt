package pe.aioo.openmoa.suggestion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WordTokenizerTest {

    @Test
    fun `빈 문자열은 null 반환`() {
        assertNull(WordTokenizer.extractKorean(""))
    }

    @Test
    fun `공백만 있으면 null 반환`() {
        assertNull(WordTokenizer.extractKorean("   "))
    }

    @Test
    fun `조사 없는 순수 단어는 그대로 반환`() {
        assertEquals("사랑", WordTokenizer.extractKorean("사랑"))
    }

    @Test
    fun `은 조사 제거`() {
        assertEquals("사랑", WordTokenizer.extractKorean("사랑은"))
    }

    @Test
    fun `는 조사 제거`() {
        assertEquals("사랑", WordTokenizer.extractKorean("사랑는"))
    }

    @Test
    fun `이 조사 제거`() {
        assertEquals("맥북", WordTokenizer.extractKorean("맥북이"))
    }

    @Test
    fun `가 조사 제거`() {
        assertEquals("사랑", WordTokenizer.extractKorean("사랑가"))
    }

    @Test
    fun `을 조사 제거`() {
        assertEquals("사랑", WordTokenizer.extractKorean("사랑을"))
    }

    @Test
    fun `를 조사 제거`() {
        assertEquals("사랑", WordTokenizer.extractKorean("사랑를"))
    }

    @Test
    fun `에서 조사 제거`() {
        assertEquals("맥북", WordTokenizer.extractKorean("맥북에서"))
    }

    @Test
    fun `에 조사 제거`() {
        assertEquals("맥북", WordTokenizer.extractKorean("맥북에"))
    }

    @Test
    fun `도 조사 제거`() {
        assertEquals("사랑", WordTokenizer.extractKorean("사랑도"))
    }

    @Test
    fun `만 조사 제거`() {
        assertEquals("사랑", WordTokenizer.extractKorean("사랑만"))
    }

    @Test
    fun `와 조사 제거`() {
        assertEquals("사랑", WordTokenizer.extractKorean("사랑와"))
    }

    @Test
    fun `과 조사 제거`() {
        assertEquals("사랑", WordTokenizer.extractKorean("사랑과"))
    }

    @Test
    fun `의 조사 제거`() {
        assertEquals("사랑", WordTokenizer.extractKorean("사랑의"))
    }

    @Test
    fun `로 조사 제거`() {
        assertEquals("사랑", WordTokenizer.extractKorean("사랑로"))
    }

    @Test
    fun `으로 조사 제거`() {
        assertEquals("사랑", WordTokenizer.extractKorean("사랑으로"))
    }

    @Test
    fun `한 글자 단어는 조사 제거 안 함 (너무 짧아서)`() {
        // "가" 에서 "가" 는 "가(조사)" 인지 단어인지 모름 - 2글자 이상만 제거
        assertEquals("가", WordTokenizer.extractKorean("가"))
    }

    @Test
    fun `두 글자일 때 조사 제거하면 한 글자 남는 경우 원본 반환`() {
        // "은" 이 "은(동사)" + 없음 인지 모름 → 결과가 1글자면 원본 반환
        assertEquals("은", WordTokenizer.extractKorean("은"))
    }

    @Test
    fun `최대 길이 초과 단어는 null 반환`() {
        val longWord = "가".repeat(31)
        assertNull(WordTokenizer.extractKorean(longWord))
    }

    @Test
    fun `한국어가 아닌 문자 포함 시 null 반환`() {
        assertNull(WordTokenizer.extractKorean("hello"))
        assertNull(WordTokenizer.extractKorean("123"))
    }

    @Test
    fun `에게서 조사 제거 (에게보다 긴 것 우선)`() {
        assertEquals("친구", WordTokenizer.extractKorean("친구에게서"))
    }

    @Test
    fun `에게 조사 제거`() {
        assertEquals("친구", WordTokenizer.extractKorean("친구에게"))
    }

    @Test
    fun `한테서 조사 제거`() {
        assertEquals("친구", WordTokenizer.extractKorean("친구한테서"))
    }

    @Test
    fun `앞뒤 공백 제거 후 처리`() {
        assertEquals("사랑", WordTokenizer.extractKorean("  사랑  "))
    }

    @Test
    fun `extractEnglish 빈 문자열은 null 반환`() {
        assertNull(WordTokenizer.extractEnglish(""))
    }

    @Test
    fun `extractEnglish 순수 영문 소문자 그대로 반환`() {
        assertEquals("hello", WordTokenizer.extractEnglish("hello"))
    }

    @Test
    fun `extractEnglish 대문자 소문자로 변환`() {
        assertEquals("hello", WordTokenizer.extractEnglish("Hello"))
    }

    @Test
    fun `extractEnglish 최대 길이 초과 시 null 반환`() {
        val longWord = "a".repeat(31)
        assertNull(WordTokenizer.extractEnglish(longWord))
    }

    @Test
    fun `extractEnglish 영문 외 문자 포함 시 null 반환`() {
        assertNull(WordTokenizer.extractEnglish("hello123"))
        assertNull(WordTokenizer.extractEnglish("안녕"))
    }
}
