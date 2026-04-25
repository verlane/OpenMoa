package pe.aioo.openmoa.clipboard

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import pe.aioo.openmoa.settings.SettingsPreferences
import java.util.UUID

object ClipboardRepository {

    private const val KEY_ENTRIES = "clipboard_entries"
    private const val PREFS_NAME = "openmoa_clipboard"

    fun getAll(context: Context): List<ClipboardEntry> {
        return try {
            val json = prefs(context).getString(KEY_ENTRIES, null) ?: return emptyList()
            val array = JSONArray(json)
            List(array.length()) { i ->
                val obj = array.getJSONObject(i)
                ClipboardEntry(
                    id = obj.getString("id"),
                    text = obj.getString("text"),
                    pinned = obj.optBoolean("pinned", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun add(context: Context, text: String) {
        if (text.isBlank()) return
        val maxItems = SettingsPreferences.getClipboardMaxItems(context)
        val entries = getAll(context).toMutableList()

        val existingIndex = entries.indexOfFirst { it.text == text }
        if (existingIndex >= 0) {
            val existing = entries.removeAt(existingIndex)
            entries.add(0, existing.copy(createdAt = System.currentTimeMillis()))
        } else {
            entries.add(0, ClipboardEntry(id = UUID.randomUUID().toString(), text = text))
        }

        val pinned = entries.filter { it.pinned }
        val unpinned = entries.filter { !it.pinned }.take(maxItems)
        persist(context, pinned + unpinned)
    }

    @Synchronized
    fun use(context: Context, id: String) {
        persist(context, getAll(context).map {
            if (it.id == id) it.copy(createdAt = System.currentTimeMillis()) else it
        })
    }

    @Synchronized
    fun pin(context: Context, id: String) {
        persist(context, getAll(context).map { if (it.id == id) it.copy(pinned = true) else it })
    }

    @Synchronized
    fun unpin(context: Context, id: String) {
        persist(context, getAll(context).map { if (it.id == id) it.copy(pinned = false) else it })
    }

    @Synchronized
    fun update(context: Context, id: String, newText: String) {
        if (newText.isBlank()) return
        persist(context, getAll(context).map { if (it.id == id) it.copy(text = newText) else it })
    }

    @Synchronized
    fun delete(context: Context, id: String) {
        persist(context, getAll(context).filter { it.id != id })
    }

    @Synchronized
    fun clearUnpinned(context: Context) {
        persist(context, getAll(context).filter { it.pinned })
    }

    @Synchronized
    fun replaceAll(context: Context, entries: List<ClipboardEntry>) {
        persist(context, entries)
    }

    @Synchronized
    fun clearAll(context: Context) {
        persist(context, emptyList())
    }

    @Synchronized
    fun purgeExpired(context: Context) {
        val expiryMinutes = SettingsPreferences.getClipboardExpiryMinutes(context)
        if (expiryMinutes <= 0) return
        val cutoff = System.currentTimeMillis() - expiryMinutes * 60_000L
        persist(context, getAll(context).filter { it.pinned || it.createdAt > cutoff })
    }

    @Synchronized
    private fun persist(context: Context, entries: List<ClipboardEntry>) {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(JSONObject().apply {
                put("id", entry.id)
                put("text", entry.text)
                put("pinned", entry.pinned)
                put("createdAt", entry.createdAt)
            })
        }
        prefs(context).edit().putString(KEY_ENTRIES, array.toString()).apply()
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
