package pe.aioo.openmoa.suggestion

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KoreanSuggestionEngine(
    private val dictionary: Dictionary,
    private val userWordStore: UserWordStore,
    private val maxCount: Int = 5,
) {
    suspend fun suggest(composingText: String, unresolved: String?): List<String> =
        withContext(Dispatchers.Default) {
            val (primary, fallback) = KoreanPrefixExtractor.extract(composingText, unresolved)
            if (primary.isEmpty()) return@withContext emptyList()

            val results = searchWithPrefix(primary)
            if (results.isNotEmpty()) return@withContext results

            if (fallback != null) searchWithPrefix(fallback) else emptyList()
        }

    private suspend fun searchWithPrefix(prefix: String): List<String> {
        val learned = userWordStore.topN(prefix, maxCount)
        val learnedSet = learned.toSet()
        val fromDict = dictionary.prefix(prefix, maxCount + learnedSet.size)
            .filter { it !in learnedSet }
        return (learned + fromDict).take(maxCount)
    }
}
