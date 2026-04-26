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
    fun `한 글자 한국어는 null 반환`() {
        assertNull(WordTokenizer.extractKorean("가"))
    }

    @Test
    fun `조사 제거 후 한 글자 남으면 null 반환`() {
        assertNull(WordTokenizer.extractKorean("은"))
        assertNull(WordTokenizer.extractKorean("나는"))
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
    fun `extractEnglish 3글자 이하는 null 반환`() {
        assertNull(WordTokenizer.extractEnglish("a"))
        assertNull(WordTokenizer.extractEnglish("ab"))
        assertNull(WordTokenizer.extractEnglish("abc"))
    }

    @Test
    fun `extractEnglish 4글자 이상 반환`() {
        assertEquals("word", WordTokenizer.extractEnglish("word"))
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
        assertNull(WordTokenizer.extractEnglish("안녕"))
        assertNull(WordTokenizer.extractEnglish("hello!"))
    }

    @Test
    fun `extractEnglish 숫자 포함 단어 반환`() {
        assertEquals("hello123", WordTokenizer.extractEnglish("hello123"))
        assertEquals("11st.co.kr", WordTokenizer.extractEnglish("11st.co.kr"))
    }

    @Test
    fun `extractEnglish 도메인 형태 반환`() {
        assertEquals("gmail.com", WordTokenizer.extractEnglish("gmail.com"))
        assertEquals("naver.com", WordTokenizer.extractEnglish("naver.com"))
        assertEquals("github.com", WordTokenizer.extractEnglish("github.com"))
    }

    @Test
    fun `extractEnglish 이메일 형태 반환`() {
        assertEquals("user@gmail.com", WordTokenizer.extractEnglish("user@gmail.com"))
    }

    @Test
    fun `extractEnglish 잘못된 도메인 형태는 null 반환`() {
        assertNull(WordTokenizer.extractEnglish(".gmail.com"))
        assertNull(WordTokenizer.extractEnglish("gmail."))
        assertNull(WordTokenizer.extractEnglish("gmail..com"))
        assertNull(WordTokenizer.extractEnglish("user@@gmail.com"))
        assertNull(WordTokenizer.extractEnglish("user@gmail@com"))
    }

    @Test
    fun `extractEnglish 앞뒤 공백 제거 후 처리`() {
        assertEquals("hello", WordTokenizer.extractEnglish("  hello  "))
    }

    @Test
    fun `extractKorean 조사만으로 구성된 경우 null 반환`() {
        assertNull(WordTokenizer.extractKorean("에서"))
    }

    @Test
    fun `extractKorean 최소 글자수 경계값 - 정확히 2글자는 반환`() {
        assertEquals("사랑", WordTokenizer.extractKorean("사랑"))
    }

    @Test
    fun `extractEnglish 최소 글자수 경계값 - 정확히 4글자는 반환`() {
        assertEquals("test", WordTokenizer.extractEnglish("test"))
    }
}
