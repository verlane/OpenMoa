package pe.aioo.openmoa.suggestion

import android.content.Context
import org.json.JSONObject

class SharedPreferencesUserWordStore(
    private val context: Context,
    private val language: Language,
) : UserWordStore {

    enum class Language { EN, KO }

    private val prefsKey = when (language) {
        Language.EN -> "learned_words_en"
        Language.KO -> "learned_words_ko"
    }

    private val prefs by lazy {
        context.getSharedPreferences("word_store", Context.MODE_PRIVATE)
    }
    private val words: MutableMap<String, Int> by lazy { loadWords() }

    private fun loadWords(): MutableMap<String, Int> {
        val json = prefs.getString(prefsKey, null) ?: return mutableMapOf()
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
        prefs.edit().putString(prefsKey, obj.toString()).apply()
    }

    override fun increment(word: String) {
        if (word.isBlank()) return
        val normalized = if (language == Language.EN) word.lowercase().trim() else word.trim()
        synchronized(this) {
            words[normalized] = (words[normalized] ?: 0) + 1
            if (words.size > MAX_WORDS) evict()
            persist()
        }
    }

    // topN은 코루틴에서 호출되나, 연산이 메모리 내 Map 조회(수 마이크로초)라
    // synchronized 블록으로 짧게 보호해도 스레드 풀 고갈 위험이 없다.
    override suspend fun topN(prefix: String, limit: Int): List<String> {
        if (prefix.isEmpty()) return emptyList()
        val normalized = if (language == Language.EN) prefix.lowercase() else prefix
        return synchronized(this) {
            words.entries
                .filter { it.key.startsWith(normalized) }
                .sortedByDescending { it.value }
                .take(limit)
                .map { it.key }
        }
    }

    override fun clear() {
        synchronized(this) {
            words.clear()
            prefs.edit().remove(prefsKey).apply()
        }
    }

    private fun evict() {
        val toRemove = words.entries
            .sortedBy { it.value }
            .take(words.size - MAX_WORDS + EVICT_MARGIN)
            .map { it.key }
        toRemove.forEach { words.remove(it) }
    }

    companion object {
        private const val MAX_WORDS = 5000
        private const val EVICT_MARGIN = 100
    }
}
