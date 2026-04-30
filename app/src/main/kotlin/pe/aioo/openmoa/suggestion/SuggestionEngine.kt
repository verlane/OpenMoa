package pe.aioo.openmoa.suggestion

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SuggestionEngine(
    private val dictionary: Dictionary,
    private val userWordStore: UserWordStore,
    private val maxCount: Int = 5,
) {
    suspend fun containsInDictionary(word: String): Boolean = dictionary.contains(word)

    suspend fun suggest(prefix: String, minCount: Int = 1): List<String> = withContext(Dispatchers.Default) {
        if (prefix.isBlank()) return@withContext emptyList()

        val learned = userWordStore.topN(prefix, maxCount, minCount)
        val learnedSet = learned.toSet()

        val fromDict = dictionary.prefix(prefix, maxCount + learnedSet.size)
            .filter { it !in learnedSet }

        (learned + fromDict).take(maxCount)
    }
}
