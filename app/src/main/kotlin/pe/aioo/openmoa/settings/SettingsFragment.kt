package pe.aioo.openmoa.settings

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import pe.aioo.openmoa.R
import pe.aioo.openmoa.clipboard.ClipboardEntry
import pe.aioo.openmoa.clipboard.ClipboardRepository
import pe.aioo.openmoa.hotstring.HotstringRule
import pe.aioo.openmoa.hotstring.HotstringRepository
import pe.aioo.openmoa.config.EnterLongPressAction
import pe.aioo.openmoa.config.HangulInputMode
import pe.aioo.openmoa.config.HapticStrength
import pe.aioo.openmoa.config.KeyboardSkin
import pe.aioo.openmoa.config.KeypadHeight
import pe.aioo.openmoa.config.LongPressTime
import pe.aioo.openmoa.config.OneHandMode
import pe.aioo.openmoa.config.SoundType
import pe.aioo.openmoa.config.SoundVolume
import pe.aioo.openmoa.config.SpaceLongPressAction
import pe.aioo.openmoa.quickphrase.QuickPhraseKey
import pe.aioo.openmoa.quickphrase.QwertyLongKey

class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        private const val SETTINGS_FILE_NAME = "openmoa_settings.json"
        private const val DEV_TAP_REQUIRED = 7
    }

    private var devTapCount = 0
    private var devTapLastTime = 0L

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SettingsPreferences.PREFS_NAME
        setPreferencesFromResource(R.xml.preferences_main, rootKey)
        setupListPreferences()
        setupClickPreferences()
        setupVersionPreference()
    }

    override fun onResume() {
        super.onResume()
        updateGestureAngleSummary()
    }

    private fun setupListPreferences() {
        setupClipboardListPreferences()
        pref<ListPreference>(SettingsPreferences.KEY_HANGUL_INPUT_MODE)
            ?.setupEnum(HangulInputMode.values(), { it.labelResId }, HangulInputMode.TWO_HAND_MOAKEY)
        pref<ListPreference>(SettingsPreferences.KEY_KEYBOARD_SKIN)
            ?.setupEnum(KeyboardSkin.values(), { it.labelResId }, KeyboardSkin.WHITE)
        pref<ListPreference>(SettingsPreferences.KEY_KEYPAD_HEIGHT)
            ?.setupEnum(KeypadHeight.values(), { it.labelResId }, KeypadHeight.LOW)
        pref<ListPreference>(SettingsPreferences.KEY_ONE_HAND_MODE)
            ?.setupEnum(OneHandMode.values(), { it.labelResId }, OneHandMode.NONE)
        pref<ListPreference>(SettingsPreferences.KEY_LONG_PRESS_TIME)
            ?.setupEnum(LongPressTime.values(), { it.labelResId }, LongPressTime.MS_500)
        pref<ListPreference>(SettingsPreferences.KEY_SPACE_LONG_PRESS_ACTION)
            ?.setupEnum(SpaceLongPressAction.values(), { it.labelResId }, SpaceLongPressAction.IME_PICKER)
        pref<ListPreference>(SettingsPreferences.KEY_ENTER_LONG_PRESS_ACTION)
            ?.setupEnum(EnterLongPressAction.values(), { it.labelResId }, EnterLongPressAction.ARROW)
        pref<ListPreference>(SettingsPreferences.KEY_HAPTIC_STRENGTH)
            ?.setupEnum(HapticStrength.values(), { it.labelResId }, HapticStrength.MEDIUM)
        pref<ListPreference>(SettingsPreferences.KEY_SOUND_VOLUME)
            ?.setupEnum(SoundVolume.values(), { it.labelResId }, SoundVolume.OFF)
        pref<ListPreference>(SettingsPreferences.KEY_SOUND_TYPE)
            ?.setupEnum(SoundType.values(), { it.labelResId }, SoundType.STANDARD)
    }

    private fun setupClipboardListPreferences() {
        val ctx = requireContext()
        pref<ListPreference>(SettingsPreferences.KEY_CLIPBOARD_MAX_ITEMS)?.apply {
            entries = arrayOf("10", "20", "30", "50")
            entryValues = arrayOf("10", "20", "30", "50")
            if (value == null) value = "20"
        }
        pref<ListPreference>(SettingsPreferences.KEY_CLIPBOARD_EXPIRY_MINUTES)?.apply {
            entries = arrayOf(
                ctx.getString(R.string.settings_clipboard_expiry_30min),
                ctx.getString(R.string.settings_clipboard_expiry_1h),
                ctx.getString(R.string.settings_clipboard_expiry_3h),
                ctx.getString(R.string.settings_clipboard_expiry_1d),
                ctx.getString(R.string.settings_clipboard_expiry_unlimited),
            )
            entryValues = arrayOf("30", "60", "180", "1440", "0")
            if (value == null) value = "60"
        }
    }

    private fun setupClickPreferences() {
        pref<Preference>("pref_gesture_angle")?.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), GestureAngleActivity::class.java))
            true
        }
        pref<Preference>("pref_shortcut")?.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), ShortcutSettingsActivity::class.java))
            true
        }
        pref<Preference>("pref_hotstring")?.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), HotstringListActivity::class.java))
            true
        }
        pref<Preference>("pref_data_export")?.setOnPreferenceClickListener {
            exportSettings()
            true
        }
        pref<Preference>("pref_data_import")?.setOnPreferenceClickListener {
            importSettings()
            true
        }
        pref<Preference>("pref_data_reset")?.setOnPreferenceClickListener {
            showResetConfirmDialog()
            true
        }
    }

    private fun setupVersionPreference() {
        val ctx = requireContext()
        val versionName = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
        pref<Preference>("pref_version")?.apply {
            summary = versionName
            setOnPreferenceClickListener {
                val now = System.currentTimeMillis()
                if (now - devTapLastTime > 3000) devTapCount = 0
                devTapLastTime = now
                devTapCount++
                val remaining = DEV_TAP_REQUIRED - devTapCount
                when {
                    remaining <= 0 -> {
                        devTapCount = 0
                        startActivity(Intent(ctx, DevToolsActivity::class.java))
                    }
                    remaining <= 3 -> {
                        Toast.makeText(ctx, "${remaining}번 더 누르면 개발자 모드", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }
        }
    }

    private fun updateGestureAngleSummary() {
        val ctx = requireContext()
        val preset = getString(SettingsPreferences.getGestureAnglePreset(ctx).labelResId)
        val threshold = SettingsPreferences.getGestureThreshold(ctx)
        pref<Preference>("pref_gesture_angle")?.summary = "$preset · ${threshold}dp"
    }

    private fun <T : Enum<T>> ListPreference.setupEnum(
        values: Array<T>,
        labelOf: (T) -> Int,
        default: T
    ) {
        val ctx = requireContext()
        entries = values.map { ctx.getString(labelOf(it)) }.toTypedArray()
        entryValues = values.map { it.name }.toTypedArray()
        if (value == null) value = default.name
    }

    private inline fun <reified T : Preference> pref(key: String): T? = findPreference(key)

    private fun exportSettings() {
        val ctx = requireContext()
        lifecycleScope.launch(Dispatchers.IO) {
            val msgResId = try {
                val prefs = ctx.getSharedPreferences(SettingsPreferences.PREFS_NAME, Context.MODE_PRIVATE)
                val settingsObj = JSONObject()
                prefs.all.forEach { (key, value) ->
                    when (value) {
                        is Boolean -> settingsObj.put(key, value)
                        is String -> settingsObj.put(key, value)
                        is Int -> settingsObj.put(key, value)
                        is Long -> settingsObj.put(key, value)
                        is Float -> settingsObj.put(key, value.toDouble())
                    }
                }

                val clipboardArray = JSONArray()
                ClipboardRepository.getAll(ctx).forEach { entry ->
                    clipboardArray.put(JSONObject().apply {
                        put("id", entry.id)
                        put("text", entry.text)
                        put("pinned", entry.pinned)
                        put("createdAt", entry.createdAt)
                    })
                }

                val hotstringsArray = JSONArray()
                HotstringRepository.getAll(ctx).forEach { rule ->
                    hotstringsArray.put(JSONObject().apply {
                        put("id", rule.id)
                        put("trigger", rule.trigger)
                        put("expansion", rule.expansion)
                        put("enabled", rule.enabled)
                    })
                }

                val jsonText = JSONObject().apply {
                    put("version", 2)
                    put("exportedAt", System.currentTimeMillis())
                    put("settings", settingsObj)
                    put("clipboard", clipboardArray)
                    put("hotstrings", hotstringsArray)
                }.toString(2)

                val resolver = ctx.contentResolver
                resolver.query(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Downloads._ID),
                    "${MediaStore.Downloads.DISPLAY_NAME} = ?",
                    arrayOf(SETTINGS_FILE_NAME),
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(0)
                        resolver.delete(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI.buildUpon()
                                .appendPath(id.toString()).build(),
                            null, null
                        )
                    }
                }
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, SETTINGS_FILE_NAME)
                    put(MediaStore.Downloads.MIME_TYPE, "application/json")
                    put(MediaStore.Downloads.RELATIVE_PATH, "Download/")
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: throw Exception("MediaStore insert failed")
                (resolver.openOutputStream(uri) ?: throw Exception("openOutputStream failed"))
                    .use { it.write(jsonText.toByteArray()) }
                R.string.settings_data_export_success
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("SettingsFragment", "export failed", e)
                R.string.settings_data_export_fail
            }
            withContext(Dispatchers.Main) {
                if (isAdded) Toast.makeText(ctx, msgResId, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun importSettings() {
        val ctx = requireContext()
        val allowedKeys = buildSet {
            addAll(SettingsPreferences.ALL_KEYS)
            QuickPhraseKey.values().forEach { add(it.prefKey) }
            QwertyLongKey.values().forEach { add(it.prefKey) }
        }
        val booleanKeys = setOf(
            SettingsPreferences.KEY_KEY_PREVIEW,
            SettingsPreferences.KEY_AUTO_SPACE_PERIOD,
            SettingsPreferences.KEY_AUTO_CAPITALIZE_ENGLISH,
            SettingsPreferences.KEY_HOTSTRING_ENABLED,
            SettingsPreferences.KEY_WORD_SUGGESTION_ENABLED,
            SettingsPreferences.KEY_KOREAN_WORD_SUGGESTION_ENABLED,
            SettingsPreferences.KEY_CLIPBOARD_ENABLED,
        )
        lifecycleScope.launch(Dispatchers.IO) {
            val success = try {
                val resolver = ctx.contentResolver
                val uri = resolver.query(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Downloads._ID),
                    "${MediaStore.Downloads.DISPLAY_NAME} = ?",
                    arrayOf(SETTINGS_FILE_NAME),
                    null
                )?.use {
                    if (it.moveToFirst()) {
                        val id = it.getLong(0)
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI.buildUpon()
                            .appendPath(id.toString()).build()
                    } else null
                } ?: run {
                    withContext(Dispatchers.Main) {
                        if (isAdded) Toast.makeText(ctx, R.string.settings_data_import_fail, Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                val jsonText = resolver.openInputStream(uri)
                    ?.use { it.readBytes().toString(Charsets.UTF_8) }
                    ?: throw Exception("openInputStream failed")
                val root = JSONObject(jsonText)
                if (root.optInt("version") != 2) throw Exception("Unsupported version")
                val settingsObj = root.optJSONObject("settings")
                    ?: throw Exception("Invalid format: missing settings")

                // 1단계: 전체 파싱 (실패 시 적용 없이 예외)
                val settingsEdits = mutableListOf<Pair<String, Any>>()
                settingsObj.keys().forEach { key ->
                    if (key !in allowedKeys) return@forEach
                    if (key in booleanKeys) {
                        settingsEdits.add(key to settingsObj.optBoolean(key))
                    } else {
                        val value = settingsObj.optString(key)
                        if (value.isNotEmpty()) settingsEdits.add(key to value)
                    }
                }
                val newHotstrings = root.optJSONArray("hotstrings")?.let { array ->
                    List(array.length()) { i ->
                        val obj = array.getJSONObject(i)
                        HotstringRule(
                            id = obj.getString("id"),
                            trigger = obj.getString("trigger"),
                            expansion = obj.getString("expansion"),
                            enabled = obj.optBoolean("enabled", true)
                        )
                    }
                }
                val newClipboard = root.optJSONArray("clipboard")?.let { array ->
                    List(array.length()) { i ->
                        val obj = array.getJSONObject(i)
                        ClipboardEntry(
                            id = obj.getString("id"),
                            text = obj.getString("text"),
                            pinned = obj.optBoolean("pinned", false),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    }
                }

                // 2단계: 일괄 적용
                val editor = ctx.getSharedPreferences(SettingsPreferences.PREFS_NAME, Context.MODE_PRIVATE).edit()
                settingsEdits.forEach { (key, value) ->
                    when (value) {
                        is Boolean -> editor.putBoolean(key, value)
                        is String -> editor.putString(key, value)
                    }
                }
                editor.apply()
                newHotstrings?.let { HotstringRepository.replaceAll(ctx, it) }
                newClipboard?.let { ClipboardRepository.replaceAll(ctx, it.filter { e -> e.text.isNotBlank() }) }

                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("SettingsFragment", "import failed", e)
                false
            }
            withContext(Dispatchers.Main) {
                if (!isAdded) return@withContext
                val msgResId = if (success) R.string.settings_data_import_success else R.string.settings_data_import_fail
                Toast.makeText(ctx, msgResId, Toast.LENGTH_SHORT).show()
                if (success) {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.settingsContainer, SettingsFragment())
                        .commit()
                }
            }
        }
    }

    private fun showResetConfirmDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setMessage(R.string.settings_data_reset_confirm)
            .setPositiveButton(R.string.dialog_confirm) { _, _ -> resetAllSettings() }
            .setNegativeButton(R.string.settings_qwerty_long_key_cancel, null)
            .show()
    }

    private fun resetAllSettings() {
        val ctx = requireContext()
        lifecycleScope.launch(Dispatchers.IO) {
            ctx.getSharedPreferences(SettingsPreferences.PREFS_NAME, Context.MODE_PRIVATE)
                .edit().clear().apply()
            ClipboardRepository.clearAll(ctx)
            withContext(Dispatchers.Main) {
                if (!isAdded) return@withContext
                Toast.makeText(ctx, R.string.settings_data_reset_success, Toast.LENGTH_SHORT).show()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.settingsContainer, SettingsFragment())
                    .commit()
            }
        }
    }
}
