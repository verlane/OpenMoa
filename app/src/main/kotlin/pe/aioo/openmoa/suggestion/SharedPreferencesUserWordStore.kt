package pe.aioo.openmoa.suggestion

import android.content.Context
import org.json.JSONObject

class SharedPreferencesUserWordStore(private val context: Context) : UserWordStore {

    private val prefs by lazy {
        context.getSharedPreferences("word_store", Context.MODE_PRIVATE)
    }
    private val words: MutableMap<String, Int> by lazy { loadWords() }

    private fun loadWords(): MutableMap<String, Int> {
        val json = prefs.getString(KEY_WORDS, null) ?: return mutableMapOf()
        return runCatching {
            val obj = JSONObject(json)
            val map = mutableMapOf<String, Int>()
            obj.keys().forEach { key -> map[key] = obj.getInt(key) }
            map
        }.getOrDefault(mutableMapOf())
    }

    private fun persist() {
        val obj = JSONObject()
        words.forEach { (k, v) -> obj.put(k, v) }
        prefs.edit().putString(KEY_WORDS, obj.toString()).apply()
    }

    @Synchronized
    override fun increment(word: String) {
        if (word.isBlank()) return
        val normalized = word.lowercase().trim()
        words[normalized] = (words[normalized] ?: 0) + 1
        if (words.size > MAX_WORDS) evict()
        persist()
    }

    @Synchronized
    override fun topN(prefix: String, limit: Int): List<String> {
        if (prefix.isEmpty()) return emptyList()
        val normalized = prefix.lowercase()
        return words.entries
            .filter { it.key.startsWith(normalized) }
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
    }

    @Synchronized
    override fun clear() {
        words.clear()
        prefs.edit().remove(KEY_WORDS).apply()
    }

    private fun evict() {
        val toRemove = words.entries
            .sortedBy { it.value }
            .take(words.size - MAX_WORDS + EVICT_MARGIN)
            .map { it.key }
        toRemove.forEach { words.remove(it) }
    }

    companion object {
        private const val KEY_WORDS = "learned_words"
        private const val MAX_WORDS = 5000
        private const val EVICT_MARGIN = 100
    }
}
