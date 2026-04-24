package pe.aioo.openmoa.suggestion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KoreanTrieDictionaryTest {

    private fun dictionaryOf(vararg words: String): KoreanTrieDictionary =
        KoreanTrieDictionary(words.asSequence())

    // 기존 정확 매칭 동작

    @Test
    fun `exact prefix match returns matching words`() = runTest {
        val dict = dictionaryOf("사랑", "사람", "사과")
        val result = dict.prefix("사랑", 5)
        assertTrue("사랑" in result)
        assertTrue("사람" !in result)
    }

    @Test
    fun `non-existent prefix returns empty`() = runTest {
        val dict = dictionaryOf("사랑", "사람")
        assertTrue(dict.prefix("나", 5).isEmpty())
    }

    @Test
    fun `empty prefix returns empty`() = runTest {
        val dict = dictionaryOf("사랑")
        assertTrue(dict.prefix("", 5).isEmpty())
    }

    @Test
    fun `non-hangul prefix returns empty`() = runTest {
        val dict = dictionaryOf("hello")
        assertTrue(dict.prefix("he", 5).isEmpty())
    }

    @Test
    fun `limit is respected`() = runTest {
        val dict = dictionaryOf("가나", "가다", "가라", "가마", "가바")
        assertEquals(3, dict.prefix("가", 3).size)
    }

    // 받침 없는 음절 확장 검색

    @Test
    fun `bare last syllable expands to include jongseong variants`() = runTest {
        val dict = dictionaryOf("가바나", "가방끈", "가발")
        val result = dict.prefix("가바", 10)
        assertTrue("가바나" in result)
        assertTrue("가방끈" in result)
        assertTrue("가발" in result)
    }

    @Test
    fun `exact match listed before expanded variants`() = runTest {
        val dict = dictionaryOf("가바나", "가방")
        val result = dict.prefix("가바", 10)
        val idxExact = result.indexOf("가바나")
        val idxExpand = result.indexOf("가방")
        assertTrue(idxExact < idxExpand)
    }

    @Test
    fun `syllable with jongseong does not expand`() = runTest {
        val dict = dictionaryOf("가방", "가바나")
        val result = dict.prefix("가방", 10)
        assertTrue("가방" in result)
        assertTrue("가바나" !in result)
    }

    @Test
    fun `single bare syllable expands`() = runTest {
        val dict = dictionaryOf("바나나", "방법", "발전")
        val result = dict.prefix("바", 10)
        assertTrue("바나나" in result)
        assertTrue("방법" in result)
        assertTrue("발전" in result)
    }

    @Test
    fun `limit respected in expanded search`() = runTest {
        val suffixes = listOf("가", "나", "다", "라", "마", "바", "사", "아", "자", "차")
        val words = suffixes.map { "가바$it" } + suffixes.map { "가방$it" }
        val dict = dictionaryOf(*words.toTypedArray())
        assertEquals(5, dict.prefix("가바", 5).size)
    }

    @Test
    fun `mid-prefix syllables must match exactly`() = runTest {
        val dict = dictionaryOf("가방", "나방")
        val result = dict.prefix("가바", 10)
        assertTrue("가방" in result)
        assertTrue("나방" !in result)
    }
}
