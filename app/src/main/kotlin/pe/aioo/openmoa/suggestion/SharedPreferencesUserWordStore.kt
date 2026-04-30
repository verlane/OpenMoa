package pe.aioo.openmoa.suggestion

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject

class SharedPreferencesUserWordStore(
    private val context: Context,
    private val language: Language,
    private val clock: Clock = SystemClock,
) : UserWordStore {

    interface Clock {
        fun todayEpochDay(): Long
    }

    private object SystemClock : Clock {
        override fun todayEpochDay(): Long = System.currentTimeMillis() / MS_PER_DAY
    }

    private data class WordMeta(val count: Int, val lastUsedDay: Long)

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
    private val words: MutableMap<String, WordMeta> by lazy { loadWords() }
    private val blacklistSet: MutableSet<String> by lazy { loadBlacklist() }

    private val persistHandler = Handler(Looper.getMainLooper())
    private val persistRunnable = Runnable {
        synchronized(this) { doPersistWords() }
    }

    private fun loadWords(): MutableMap<String, WordMeta> {
        val json = prefs.getString(wordsKey, null) ?: return mutableMapOf()
        return runCatching {
            val obj = JSONObject(json)
            val today = clock.todayEpochDay()
            val map = mutableMapOf<String, WordMeta>()
            obj.keys().forEach { key ->
                map[key] = when (val value = obj.opt(key)) {
                    is Int -> WordMeta(value, today)
                    is JSONObject -> WordMeta(value.optInt("c", 1), value.optLong("d", today))
                    else -> return@forEach
                }
            }
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

    private fun schedulePersist() {
        persistHandler.removeCallbacks(persistRunnable)
        persistHandler.postDelayed(persistRunnable, PERSIST_DEBOUNCE_MS)
    }

    override fun flush() {
        persistHandler.removeCallbacks(persistRunnable)
        synchronized(this) { doPersistWords() }
    }

    private fun doPersistWords() {
        val obj = JSONObject()
        words.forEach { (k, v) ->
            obj.put(k, JSONObject().apply {
                put("c", v.count)
                put("d", v.lastUsedDay)
            })
        }
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
            val existing = words[normalized]
            words[normalized] = WordMeta(
                count = (existing?.count ?: 0) + 1,
                lastUsedDay = clock.todayEpochDay()
            )
            if (words.size > MAX_WORDS) evict()
        }
        schedulePersist()
    }

    override fun ensureMinCount(word: String, minCount: Int) {
        if (word.isBlank()) return
        val normalized = normalize(word)
        synchronized(this) {
            val current = words[normalized]?.count ?: 0
            if (current < minCount) {
                words[normalized] = WordMeta(count = minCount, lastUsedDay = clock.todayEpochDay())
                if (words.size > MAX_WORDS) evict()
            }
        }
        schedulePersist()
    }

    override fun decrement(word: String) {
        val normalized = normalize(word)
        val changed = synchronized(this) {
            val meta = words[normalized] ?: return
            if (meta.count <= 1) words.remove(normalized) else words[normalized] = meta.copy(count = meta.count - 1)
            true
        }
        if (changed) schedulePersist()
    }

    override fun remove(word: String) {
        val normalized = normalize(word)
        synchronized(this) { words.remove(normalized) }
        schedulePersist()
    }

    override fun contains(word: String): Boolean = synchronized(this) {
        words.containsKey(normalize(word))
    }

    override fun entries(): List<Pair<String, Int>> = synchronized(this) {
        words.entries.map { it.key to it.value.count }
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
        return synchronized(this) {
            words.entries
                .filter {
                    it.key.startsWith(normalized) &&
                    it.value.count >= minCount &&
                    it.key !in blacklistSet
                }
                .sortedByDescending { it.value.count }
                .take(limit)
                .map { it.key }
        }
    }

    override suspend fun topNChosung(pattern: String, limit: Int, minCount: Int): List<String> {
        if (pattern.isEmpty()) return emptyList()
        return synchronized(this) {
            words.entries
                .filter { (word, meta) ->
                    meta.count >= minCount &&
                    word !in blacklistSet &&
                    HangulSyllable.matchesChosungPattern(word, pattern)
                }
                .sortedByDescending { it.value.count }
                .take(limit)
                .map { it.key }
        }
    }

    override fun importWords(words: Map<String, Int>) {
        val today = clock.todayEpochDay()
        synchronized(this) {
            words.forEach { (word, count) ->
                if (word.isNotBlank()) {
                    val key = normalize(word)
                    val existing = this.words[key]
                    this.words[key] = WordMeta(count, existing?.lastUsedDay ?: today)
                }
            }
            if (this.words.size > MAX_WORDS) evict()
            doPersistWords()
        }
    }

    override fun pruneOlderThan(days: Int): Int = synchronized(this) {
        val cutoffDay = clock.todayEpochDay() - days
        val toRemove = words.entries
            .filter { it.value.lastUsedDay < cutoffDay }
            .map { it.key }
        toRemove.forEach { words.remove(it) }
        if (toRemove.isNotEmpty()) doPersistWords()
        toRemove.size
    }

    override fun clear() {
        persistHandler.removeCallbacks(persistRunnable)
        synchronized(this) {
            words.clear()
            blacklistSet.clear()
            prefs.edit().remove(wordsKey).remove(blacklistKey).apply()
        }
    }

    private fun normalize(word: String): String =
        if (language == Language.EN) word.lowercase().trim() else word.trim()

    private fun evict() {
        val today = clock.todayEpochDay()
        val toRemove = words.entries
            .sortedBy { (_, meta) ->
                val ageDays = today - meta.lastUsedDay
                meta.count - maxOf(0L, ageDays - GRACE_DAYS) * AGE_PENALTY
            }
            .take(words.size - MAX_WORDS + EVICT_MARGIN)
            .map { it.key }
        toRemove.forEach { words.remove(it) }
    }

    fun seedIfNeeded(seedWords: List<String>, seedCount: Int) {
        val versionKey = "seed_version_${language.name.lowercase()}"
        val today = clock.todayEpochDay()
        synchronized(this) {
            if (prefs.getInt(versionKey, 0) >= SEED_VERSION) return
            seedWords
                .mapNotNull { raw -> normalize(raw).takeIf { raw.isNotBlank() } }
                .filter { !words.containsKey(it) && it !in blacklistSet }
                .forEach { words[it] = WordMeta(seedCount, today) }
            if (words.size > MAX_WORDS) evict()
            doPersistWords()
            prefs.edit().putInt(versionKey, SEED_VERSION).apply()
        }
    }

    companion object {
        private const val MAX_WORDS = 5000
        private const val EVICT_MARGIN = 100
        private const val SEED_VERSION = 1
        private const val PERSIST_DEBOUNCE_MS = 500L
        private const val GRACE_DAYS = 30L
        private const val AGE_PENALTY: Double = 0.5
        private const val MS_PER_DAY = 86_400_000L
    }
}
