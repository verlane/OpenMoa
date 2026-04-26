package pe.aioo.openmoa.suggestion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DefaultSeedWordsTest {

    @Test
    fun `한국어 시드 단어는 30개`() {
        assertEquals(30, DefaultSeedWords.KO.size)
    }

    @Test
    fun `영어 시드 단어는 30개`() {
        assertEquals(30, DefaultSeedWords.EN.size)
    }

    @Test
    fun `한국어 시드 단어에 중복 없음`() {
        assertEquals(DefaultSeedWords.KO.size, DefaultSeedWords.KO.toSet().size)
    }

    @Test
    fun `영어 시드 단어에 중복 없음`() {
        assertEquals(DefaultSeedWords.EN.size, DefaultSeedWords.EN.toSet().size)
    }

    @Test
    fun `한국어 시드 단어는 모두 extractKorean을 통과`() {
        DefaultSeedWords.KO.forEach { word ->
            assertNotNull("'$word' 는 extractKorean에서 null 반환", WordTokenizer.extractKorean(word))
        }
    }

    @Test
    fun `영어 시드 단어는 모두 extractEnglish를 통과`() {
        DefaultSeedWords.EN.forEach { word ->
            assertNotNull("'$word' 는 extractEnglish에서 null 반환", WordTokenizer.extractEnglish(word))
        }
    }
}
