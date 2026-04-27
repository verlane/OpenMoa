package pe.aioo.openmoa.suggestion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KoreanSuggestionEngineTest {

    private fun fakeKoDictionary(vararg words: String): Dictionary = object : Dictionary {
        override suspend fun prefix(prefix: String, limit: Int): List<String> =
            words.filter { it.startsWith(prefix) }.take(limit)

        override suspend fun chosung(pattern: String, limit: Int): List<String> =
            words.filter { HangulSyllable.matchesChosungPattern(it, pattern) }.take(limit)
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
            override fun decrement(word: String) = Unit
            override fun remove(word: String) = Unit
            override fun contains(word: String): Boolean = false
            override fun entries(): List<Pair<String, Int>> = emptyList()
            override fun importWords(words: Map<String, Int>) = Unit
            override fun addToBlacklist(word: String) = Unit
            override fun removeFromBlacklist(word: String) = Unit
            override fun blacklist(): Set<String> = emptySet()
            override suspend fun topN(prefix: String, limit: Int, minCount: Int) =
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
    fun `chosung input returns matching words from dictionary`() = runTest {
        val engine = KoreanSuggestionEngine(
            fakeKoDictionary("고구마", "고기", "사과"),
            NoOpUserWordStore()
        )
        val result = engine.suggest("ㄱㄱㅁ", null)
        assertEquals(listOf("고구마"), result)
    }

    @Test
    fun `chosung input returns learned words before dictionary words`() = runTest {
        val store = InMemoryUserWordStore().apply {
            repeat(3) { increment("고구마") }
        }
        val engine = KoreanSuggestionEngine(
            fakeKoDictionary("고기마", "고구마"),
            store
        )
        val result = engine.suggest("ㄱㄱㅁ", null)
        assertEquals("고구마", result.first())
    }

    @Test
    fun `chosung returns empty when no match`() = runTest {
        val engine = KoreanSuggestionEngine(
            fakeKoDictionary("사과", "바나나"),
            NoOpUserWordStore()
        )
        val result = engine.suggest("ㄱㄱㅁ", null)
        assertTrue(result.isEmpty())
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
