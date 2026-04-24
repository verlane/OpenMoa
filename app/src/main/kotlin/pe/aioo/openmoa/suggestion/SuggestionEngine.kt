package pe.aioo.openmoa.suggestion

class SuggestionEngine(
    private val dictionary: Dictionary,
    private val userWordStore: UserWordStore,
    private val maxCount: Int = 5,
) {
    suspend fun suggest(prefix: String): List<String> {
        if (prefix.isBlank()) return emptyList()

        val learned = userWordStore.topN(prefix, maxCount)
        val learnedSet = learned.toSet()

        val fromDict = dictionary.prefix(prefix, maxCount + learnedSet.size)
            .filter { it !in learnedSet }

        return (learned + fromDict).take(maxCount)
    }
}
