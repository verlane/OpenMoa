package pe.aioo.openmoa.suggestion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggestionEngineTest {

    private fun fakeDictionary(vararg words: String): Dictionary = object : Dictionary {
        override suspend fun prefix(prefix: String, limit: Int): List<String> =
            words.filter { it.startsWith(prefix) }.take(limit)
    }

    private fun fakeUserStore(vararg entries: Pair<String, List<String>>): UserWordStore {
        val map = entries.toMap()
        return object : UserWordStore {
            override fun increment(word: String) = Unit
            override suspend fun topN(prefix: String, limit: Int) =
                map.entries.firstOrNull { prefix.startsWith(it.key) || it.key == prefix }
                    ?.value?.take(limit) ?: emptyList()
            override fun clear() = Unit
        }
    }

    @Test
    fun `returns empty list for blank prefix`() = runTest {
        val engine = SuggestionEngine(fakeDictionary("hello"), NoOpUserWordStore())
        assertTrue(engine.suggest("").isEmpty())
        assertTrue(engine.suggest("   ").isEmpty())
    }

    @Test
    fun `returns dictionary matches for prefix`() = runTest {
        val engine = SuggestionEngine(
            fakeDictionary("hello", "help", "held", "world"),
            NoOpUserWordStore()
        )
        val result = engine.suggest("hel")
        assertEquals(listOf("hello", "help", "held"), result)
    }

    @Test
    fun `learned words appear before dictionary words`() = runTest {
        val engine = SuggestionEngine(
            fakeDictionary("hello", "help", "held"),
            fakeUserStore("hel" to listOf("help")),
            maxCount = 3
        )
        val result = engine.suggest("hel")
        assertEquals("help", result.first())
    }

    @Test
    fun `no duplicates between learned and dictionary`() = runTest {
        val engine = SuggestionEngine(
            fakeDictionary("hello", "help", "held"),
            fakeUserStore("hel" to listOf("hello")),
            maxCount = 5
        )
        val result = engine.suggest("hel")
        assertEquals(result.size, result.toSet().size)
    }

    @Test
    fun `respects maxCount limit`() = runTest {
        val engine = SuggestionEngine(
            fakeDictionary("hello", "help", "held", "helm", "her"),
            NoOpUserWordStore(),
            maxCount = 3
        )
        val result = engine.suggest("h")
        assertEquals(3, result.size)
    }

    @Test
    fun `NoOpUserWordStore returns empty list`() = runTest {
        val engine = SuggestionEngine(
            fakeDictionary("hello"),
            NoOpUserWordStore()
        )
        val result = engine.suggest("he")
        assertEquals(listOf("hello"), result)
    }
}
