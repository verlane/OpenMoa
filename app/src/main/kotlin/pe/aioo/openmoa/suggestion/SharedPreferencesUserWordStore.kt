package pe.aioo.openmoa.suggestion

import android.content.Context
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject

class SharedPreferencesUserWordStore(
    private val context: Context,
    private val language: Language,
) : UserWordStore {

    enum class Language { EN, KO }

    private val wordsKey = when (language) {
        Language.EN -> "learned_words_en"
        Language.KO -> "learned_words_ko"
    }
    private val blacklistKey = when (language) {
        Language.EN -> "blacklist_en"
        Language.KO -> "blacklist_ko"
    }

    private val prefs by lazy {
        context.getSharedPreferences("word_store", Context.MODE_PRIVATE)
    }
    private val words: MutableMap<String, Int> by lazy { loadWords() }
    private val blacklistSet: MutableSet<String> by lazy { loadBlacklist() }
    private val mutex = Mutex()

    private fun loadWords(): MutableMap<String, Int> {
        val json = prefs.getString(wordsKey, null) ?: return mutableMapOf()
        return runCatching {
            val obj = JSONObject(json)
            val map = mutableMapOf<String, Int>()
            obj.keys().forEach { key -> map[key] = obj.getInt(key) }
            map
        }.getOrDefault(mutableMapOf())
    }

    private fun loadBlacklist(): MutableSet<String> {
        val json = prefs.getString(blacklistKey, null) ?: return mutableSetOf()
        return runCatching {
            val arr = JSONArray(json)
            (0 until arr.length()).mapTo(mutableSetOf()) { arr.getString(it) }
        }.getOrDefault(mutableSetOf())
    }

    private fun persistWords() {
        val obj = JSONObject()
        words.forEach { (k, v) -> obj.put(k, v) }
        prefs.edit().putString(wordsKey, obj.toString()).apply()
    }

    private fun persistBlacklist() {
        val arr = JSONArray()
        blacklistSet.forEach { arr.put(it) }
        prefs.edit().putString(blacklistKey, arr.toString()).apply()
    }

    override fun increment(word: String) {
        if (word.isBlank()) return
        val normalized = normalize(word)
        synchronized(this) {
            words[normalized] = (words[normalized] ?: 0) + 1
            if (words.size > MAX_WORDS) evict()
            persistWords()
        }
    }

    override fun decrement(word: String) {
        val normalized = normalize(word)
        synchronized(this) {
            val count = words[normalized] ?: return
            if (count <= 1) words.remove(normalized) else words[normalized] = count - 1
            persistWords()
        }
    }

    override fun remove(word: String) {
        val normalized = normalize(word)
        synchronized(this) {
            words.remove(normalized)
            persistWords()
        }
    }

    override fun contains(word: String): Boolean = synchronized(this) {
        words.containsKey(normalize(word))
    }

    override fun entries(): List<Pair<String, Int>> = synchronized(this) {
        words.entries.map { it.key to it.value }
    }

    override fun addToBlacklist(word: String) {
        val normalized = normalize(word)
        synchronized(this) {
            blacklistSet.add(normalized)
            persistBlacklist()
        }
    }

    override fun removeFromBlacklist(word: String) {
        val normalized = normalize(word)
        synchronized(this) {
            blacklistSet.remove(normalized)
            persistBlacklist()
        }
    }

    override fun blacklist(): Set<String> = synchronized(this) { blacklistSet.toSet() }

    override suspend fun topN(prefix: String, limit: Int, minCount: Int): List<String> {
        if (prefix.isEmpty()) return emptyList()
        val normalized = normalize(prefix)
        return mutex.withLock {
            words.entries
                .filter {
                    it.key.startsWith(normalized) &&
                    it.value >= minCount &&
                    it.key !in blacklistSet
                }
                .sortedByDescending { it.value }
                .take(limit)
                .map { it.key }
        }
    }

    override fun importWords(words: Map<String, Int>) {
        synchronized(this) {
            words.forEach { (word, count) ->
                if (word.isNotBlank()) this.words[normalize(word)] = count
            }
            if (this.words.size > MAX_WORDS) evict()
            persistWords()
        }
    }

    override fun clear() {
        synchronized(this) {
            words.clear()
            blacklistSet.clear()
            prefs.edit().remove(wordsKey).remove(blacklistKey).apply()
        }
    }

    private fun normalize(word: String): String =
        if (language == Language.EN) word.lowercase().trim() else word.trim()

    private fun evict() {
        val toRemove = words.entries
            .sortedBy { it.value }
            .take(words.size - MAX_WORDS + EVICT_MARGIN)
            .map { it.key }
        toRemove.forEach { words.remove(it) }
    }

    fun seedIfNeeded(seedWords: List<String>, seedCount: Int) {
        val versionKey = "seed_version_${language.name.lowercase()}"
        if (prefs.getInt(versionKey, 0) >= SEED_VERSION) return
        synchronized(this) {
            seedWords
                .filter { it.isNotBlank() && !words.containsKey(normalize(it)) && normalize(it) !in blacklistSet }
                .forEach { words[normalize(it)] = seedCount }
            if (words.size > MAX_WORDS) evict()
            persistWords()
        }
        prefs.edit().putInt(versionKey, SEED_VERSION).apply()
    }

    companion object {
        private const val MAX_WORDS = 5000
        private const val EVICT_MARGIN = 100
        private const val SEED_VERSION = 1
    }
}
