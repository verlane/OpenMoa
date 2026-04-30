package pe.aioo.openmoa.suggestion

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KoreanSuggestionEngine(
    private val dictionary: Dictionary,
    private val userWordStore: UserWordStore,
    private val maxCount: Int = 5,
) {
    suspend fun containsInDictionary(word: String): Boolean = dictionary.contains(word)

    suspend fun suggest(composingText: String, unresolved: String?, minCount: Int = 1): List<String> =
        withContext(Dispatchers.Default) {
            if (HangulSyllable.isAllChosung(composingText)) {
                return@withContext searchWithChosung(composingText, minCount)
            }

            val (primary, fallback) = KoreanPrefixExtractor.extract(composingText, unresolved)
            if (primary.isEmpty()) return@withContext emptyList()

            val results = searchWithPrefix(primary, minCount)
            if (results.isNotEmpty()) return@withContext results

            if (fallback != null) searchWithPrefix(fallback, minCount) else emptyList()
        }

    private suspend fun searchWithChosung(pattern: String, minCount: Int = 1): List<String> {
        val learned = userWordStore.topNChosung(pattern, maxCount, minCount)
        val learnedSet = learned.toSet()
        val fromDict = dictionary.chosung(pattern, maxCount + learnedSet.size)
            .filter { it !in learnedSet }
        return (learned + fromDict).take(maxCount)
    }

    private suspend fun searchWithPrefix(prefix: String, minCount: Int = 1): List<String> {
        val learned = userWordStore.topN(prefix, maxCount, minCount)
        val learnedSet = learned.toSet()
        val fromDict = dictionary.prefix(prefix, maxCount + learnedSet.size)
            .filter { it !in learnedSet }
        return (learned + fromDict).take(maxCount)
    }
}
