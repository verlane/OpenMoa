package pe.aioo.openmoa.suggestion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KoreanSuggestionEngineTest {

    private fun fakeKoDictionary(vararg words: String): Dictionary = object : Dictionary {
        override suspend fun prefix(prefix: String, limit: Int): List<String> =
            words.filter { it.startsWith(prefix) }.take(limit)
    }

    @Test
    fun `returns empty list when composingText is empty`() = runTest {
        val engine = KoreanSuggestionEngine(fakeKoDictionary("사랑"), NoOpUserWordStore())
        assertTrue(engine.suggest("", null).isEmpty())
    }

    @Test
    fun `returns matches for complete syllable prefix`() = runTest {
        val engine = KoreanSuggestionEngine(
            fakeKoDictionary("사랑", "사람", "사과"),
            NoOpUserWordStore()
        )
        val result = engine.suggest("사", null)
        assertEquals(listOf("사랑", "사람", "사과"), result)
    }

    @Test
    fun `jamo unresolved is stripped and fallback prefix used`() = runTest {
        // "사ㄹ" -> primary="사", fallback=null
        val engine = KoreanSuggestionEngine(
            fakeKoDictionary("사랑", "사람"),
            NoOpUserWordStore()
        )
        val result = engine.suggest("사ㄹ", "ㄹ")
        assertEquals(listOf("사랑", "사람"), result)
    }

    @Test
    fun `learned words appear before dictionary words`() = runTest {
        val learned = object : UserWordStore {
            override fun increment(word: String) = Unit
            override suspend fun topN(prefix: String, limit: Int) =
                if ("사랑".startsWith(prefix)) listOf("사랑").take(limit) else emptyList()
            override fun clear() = Unit
        }
        val engine = KoreanSuggestionEngine(
            fakeKoDictionary("사람", "사과"),
            learned,
            maxCount = 3
        )
        val result = engine.suggest("사", null)
        assertEquals("사랑", result.first())
    }

    @Test
    fun `respects maxCount`() = runTest {
        val engine = KoreanSuggestionEngine(
            fakeKoDictionary("가나", "가다", "가라", "가마", "가바"),
            NoOpUserWordStore(),
            maxCount = 3
        )
        val result = engine.suggest("가", null)
        assertEquals(3, result.size)
    }
}
