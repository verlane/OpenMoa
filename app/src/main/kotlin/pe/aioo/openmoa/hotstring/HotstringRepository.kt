package pe.aioo.openmoa.hotstring

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import pe.aioo.openmoa.settings.SettingsPreferences
import java.util.UUID

object HotstringRepository {

    private const val KEY_RULES = "hotstring_rules"
    private const val KEY_VERSION = "hotstring_rules_version"

    private var cache: List<HotstringRule> = emptyList()
    private var cacheVersion: Long = -1L

    fun getAll(context: Context): List<HotstringRule> {
        return try {
            val json = prefs(context).getString(KEY_RULES, null) ?: return emptyList()
            val array = JSONArray(json)
            List(array.length()) { i ->
                val obj = array.getJSONObject(i)
                HotstringRule(
                    id = obj.getString("id"),
                    trigger = obj.getString("trigger"),
                    expansion = obj.getString("expansion"),
                    enabled = obj.optBoolean("enabled", true)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun upsert(context: Context, rule: HotstringRule) {
        val rules = getAll(context).toMutableList()
        val index = rules.indexOfFirst { it.id == rule.id }
        if (index >= 0) rules[index] = rule else rules.add(rule)
        persist(context, rules)
    }

    fun delete(context: Context, id: String) {
        persist(context, getAll(context).filter { it.id != id })
    }

    fun newId(): String = UUID.randomUUID().toString()

    fun ensureDefaults(context: Context) {
        if (getAll(context).isNotEmpty()) return
        val defaults = listOf(
            HotstringRule(newId(), "ㅇㄴ", "안녕하세요"),
            HotstringRule(newId(), "ㄱㅅ", "감사합니다"),
            HotstringRule(newId(), "ㅈㅅ", "죄송합니다"),
        )
        persist(context, defaults)
    }

    fun getVersion(context: Context): Long =
        prefs(context).getLong(KEY_VERSION, 0L)

    fun getCached(context: Context): List<HotstringRule> {
        val version = getVersion(context)
        if (version != cacheVersion) {
            cache = getAll(context)
            cacheVersion = version
        }
        return cache
    }

    fun hasTrigger(context: Context, trigger: String, excludeId: String? = null): Boolean =
        getAll(context).any { it.trigger == trigger && it.id != excludeId }

    private fun persist(context: Context, rules: List<HotstringRule>) {
        val array = JSONArray()
        rules.forEach { rule ->
            array.put(JSONObject().apply {
                put("id", rule.id)
                put("trigger", rule.trigger)
                put("expansion", rule.expansion)
                put("enabled", rule.enabled)
            })
        }
        prefs(context).edit()
            .putString(KEY_RULES, array.toString())
            .putLong(KEY_VERSION, System.currentTimeMillis())
            .apply()
        cacheVersion = -1L
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(SettingsPreferences.PREFS_NAME, Context.MODE_PRIVATE)
}
