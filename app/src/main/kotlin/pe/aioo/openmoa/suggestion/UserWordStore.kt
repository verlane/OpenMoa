package pe.aioo.openmoa.suggestion

interface UserWordStore {
    suspend fun topN(prefix: String, limit: Int, minCount: Int = 1): List<String>
    suspend fun topNChosung(pattern: String, limit: Int, minCount: Int = 1): List<String> = emptyList()
    fun increment(word: String)
    fun ensureMinCount(word: String, minCount: Int) = Unit
    fun decrement(word: String)
    fun remove(word: String)
    fun contains(word: String): Boolean
    fun entries(): List<Pair<String, Int>>
    fun importWords(words: Map<String, Int>)
    fun addToBlacklist(word: String)
    fun removeFromBlacklist(word: String)
    fun blacklist(): Set<String>
    fun clear()
    fun flush() = Unit
    fun pruneOlderThan(days: Int): Int = 0
}

class NoOpUserWordStore : UserWordStore {
    override suspend fun topN(prefix: String, limit: Int, minCount: Int): List<String> = emptyList()
    override fun increment(word: String) = Unit
    override fun decrement(word: String) = Unit
    override fun remove(word: String) = Unit
    override fun contains(word: String): Boolean = false
    override fun entries(): List<Pair<String, Int>> = emptyList()
    override fun importWords(words: Map<String, Int>) = Unit
    override fun addToBlacklist(word: String) = Unit
    override fun removeFromBlacklist(word: String) = Unit
    override fun blacklist(): Set<String> = emptySet()
    override fun clear() = Unit
}

class InMemoryUserWordStore : UserWordStore {
    private val words = mutableMapOf<String, Int>()
    private val blacklistSet = mutableSetOf<String>()

    fun countOf(word: String): Int = words[word] ?: 0

    override fun increment(word: String) {
        if (word.isBlank()) return
        words[word] = (words[word] ?: 0) + 1
    }

    override fun ensureMinCount(word: String, minCount: Int) {
        if (word.isBlank()) return
        val current = words[word] ?: 0
        if (current < minCount) words[word] = minCount
    }

    override fun decrement(word: String) {
        val count = words[word] ?: return
        if (count <= 1) words.remove(word) else words[word] = count - 1
    }

    override fun remove(word: String) {
        words.remove(word)
    }

    override fun contains(word: String): Boolean = words.containsKey(word)

    override fun entries(): List<Pair<String, Int>> = words.entries.map { it.key to it.value }

    override fun importWords(words: Map<String, Int>) {
        words.forEach { (word, count) -> if (word.isNotBlank()) this.words[word] = count }
    }

    override fun addToBlacklist(word: String) {
        blacklistSet.add(word)
    }

    override fun removeFromBlacklist(word: String) {
        blacklistSet.remove(word)
    }

    override fun blacklist(): Set<String> = blacklistSet.toSet()

    override suspend fun topN(prefix: String, limit: Int, minCount: Int): List<String> {
        if (prefix.isEmpty()) return emptyList()
        return words.entries
            .filter { it.key.startsWith(prefix) && it.value >= minCount && it.key !in blacklistSet }
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
    }

    override suspend fun topNChosung(pattern: String, limit: Int, minCount: Int): List<String> {
        if (pattern.isEmpty()) return emptyList()
        return words.entries
            .filter { (word, count) ->
                count >= minCount &&
                word !in blacklistSet &&
                HangulSyllable.matchesChosungPattern(word, pattern)
            }
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
    }

    override fun clear() {
        words.clear()
        blacklistSet.clear()
    }
}
