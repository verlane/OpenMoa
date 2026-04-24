package pe.aioo.openmoa.settings

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import pe.aioo.openmoa.R
import pe.aioo.openmoa.config.EnterLongPressAction
import pe.aioo.openmoa.config.HangulInputMode
import pe.aioo.openmoa.config.HapticStrength
import pe.aioo.openmoa.config.KeyboardSkin
import pe.aioo.openmoa.config.SoundType
import pe.aioo.openmoa.config.KeypadHeight
import pe.aioo.openmoa.config.LongPressTime
import pe.aioo.openmoa.config.OneHandMode
import pe.aioo.openmoa.config.SoundVolume
import pe.aioo.openmoa.config.SpaceLongPressAction
import pe.aioo.openmoa.databinding.ActivitySettingsBinding
import pe.aioo.openmoa.quickphrase.QuickPhraseKey
import pe.aioo.openmoa.quickphrase.QwertyLongKey

class SettingsActivity : AppCompatActivity() {

    companion object {
        private const val SETTINGS_FILE_NAME = "openmoa_settings.json"
        private const val DEV_TAP_REQUIRED = 7
        private const val DEV_TAP_RESET_MS = 2000L
    }

    private lateinit var binding: ActivitySettingsBinding
    private var devTapCount = 0
    private var devTapLastTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViews()
    }

    override fun onResume() {
        super.onResume()
        updateGestureAngleDisplay()
    }

    private fun setupViews() {
        refreshAllDisplays()
        binding.gestureAngleItem.setOnClickListener {
            startActivity(Intent(this, GestureAngleActivity::class.java))
        }
        binding.shortcutItem.setOnClickListener {
            startActivity(Intent(this, ShortcutSettingsActivity::class.java))
        }
        binding.hotstringManageItem.setOnClickListener {
            startActivity(Intent(this, HotstringListActivity::class.java))
        }
        binding.hangulInputModeItem.setOnClickListener { showInputModeDialog() }
        binding.keyboardSkinItem.setOnClickListener { showKeyboardSkinDialog() }
        binding.keypadHeightItem.setOnClickListener { showKeypadHeightDialog() }
        binding.oneHandModeItem.setOnClickListener { showOneHandModeDialog() }
        binding.longPressTimeItem.setOnClickListener { showLongPressTimeDialog() }
        binding.spaceLongPressActionItem.setOnClickListener { showSpaceLongPressActionDialog() }
        binding.keyPreviewItem.setOnClickListener { toggleKeyPreview() }
        binding.autoSpacePeriodItem.setOnClickListener { toggleAutoSpacePeriod() }
        binding.autoCapitalizeEnglishItem.setOnClickListener { toggleAutoCapitalizeEnglish() }
        binding.wordSuggestionItem.setOnClickListener { toggleWordSuggestion() }
        binding.enterLongPressActionItem.setOnClickListener { showEnterLongPressActionDialog() }
        binding.hapticStrengthItem.setOnClickListener { showHapticStrengthDialog() }
        binding.soundVolumeItem.setOnClickListener { showSoundVolumeDialog() }
        binding.soundTypeItem.setOnClickListener { showSoundTypeDialog() }
        binding.settingsDataExportItem.setOnClickListener { exportSettings() }
        binding.settingsDataImportItem.setOnClickListener { importSettings() }
        binding.settingsDataResetItem.setOnClickListener { showResetConfirmDialog() }
        binding.hangulSectionHeader.setOnClickListener { onDevTap() }
    }

    private fun onDevTap() {
        val now = System.currentTimeMillis()
        if (now - devTapLastTime > DEV_TAP_RESET_MS) devTapCount = 0
        devTapLastTime = now
        devTapCount++
        if (devTapCount >= DEV_TAP_REQUIRED) {
            devTapCount = 0
            startActivity(Intent(this, DevToolsActivity::class.java))
        }
    }

    private fun updateKeyboardSkinDisplay() {
        binding.keyboardSkinValue.text =
            getString(SettingsPreferences.getKeyboardSkin(this).labelResId)
    }

    private fun showKeyboardSkinDialog() {
        val options = KeyboardSkin.values()
        val labels = options.map { getString(it.labelResId) }.toTypedArray()
        val currentIndex = options.indexOf(SettingsPreferences.getKeyboardSkin(this))

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_keyboard_skin_title)
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                SettingsPreferences.save(this, SettingsPreferences.KEY_KEYBOARD_SKIN, options[which].name)
                updateKeyboardSkinDisplay()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    private fun updateInputModeDisplay() {
        binding.hangulInputModeValue.text =
            getString(SettingsPreferences.getHangulInputMode(this).labelResId)
    }

    private fun updateKeypadHeightDisplay() {
        binding.keypadHeightValue.text =
            getString(SettingsPreferences.getKeypadHeight(this).labelResId)
    }

    private fun updateOneHandModeDisplay() {
        binding.oneHandModeValue.text =
            getString(SettingsPreferences.getOneHandMode(this).labelResId)
    }

    private fun updateLongPressTimeDisplay() {
        binding.longPressTimeValue.text =
            getString(SettingsPreferences.getLongPressTime(this).labelResId)
    }

    private fun showLongPressTimeDialog() {
        val options = LongPressTime.values()
        val labels = options.map { getString(it.labelResId) }.toTypedArray()
        val currentIndex = options.indexOf(SettingsPreferences.getLongPressTime(this))

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_long_press_time_title)
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                SettingsPreferences.save(this, SettingsPreferences.KEY_LONG_PRESS_TIME, options[which].name)
                updateLongPressTimeDisplay()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    private fun updateKeyPreviewDisplay() {
        binding.keyPreviewSwitch.isChecked = SettingsPreferences.getKeyPreviewEnabled(this)
    }

    private fun toggleKeyPreview() {
        val newValue = !SettingsPreferences.getKeyPreviewEnabled(this)
        SettingsPreferences.setKeyPreviewEnabled(this, newValue)
        binding.keyPreviewSwitch.isChecked = newValue
    }

    private fun updateSpaceLongPressActionDisplay() {
        binding.spaceLongPressActionValue.text =
            getString(SettingsPreferences.getSpaceLongPressAction(this).labelResId)
    }

    private fun showSpaceLongPressActionDialog() {
        val options = SpaceLongPressAction.values()
        val labels = options.map { getString(it.labelResId) }.toTypedArray()
        val currentIndex = options.indexOf(SettingsPreferences.getSpaceLongPressAction(this))

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_space_long_press_title)
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                SettingsPreferences.save(this, SettingsPreferences.KEY_SPACE_LONG_PRESS_ACTION, options[which].name)
                updateSpaceLongPressActionDisplay()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    private fun updateAutoSpacePeriodDisplay() {
        binding.autoSpacePeriodSwitch.isChecked = SettingsPreferences.getAutoSpacePeriod(this)
    }

    private fun toggleAutoSpacePeriod() {
        val newValue = !SettingsPreferences.getAutoSpacePeriod(this)
        SettingsPreferences.setAutoSpacePeriod(this, newValue)
        binding.autoSpacePeriodSwitch.isChecked = newValue
    }

    private fun updateAutoCapitalizeEnglishDisplay() {
        binding.autoCapitalizeEnglishSwitch.isChecked = SettingsPreferences.getAutoCapitalizeEnglish(this)
    }

    private fun toggleAutoCapitalizeEnglish() {
        val newValue = !SettingsPreferences.getAutoCapitalizeEnglish(this)
        SettingsPreferences.setAutoCapitalizeEnglish(this, newValue)
        binding.autoCapitalizeEnglishSwitch.isChecked = newValue
    }

    private fun updateWordSuggestionDisplay() {
        binding.wordSuggestionSwitch.isChecked = SettingsPreferences.getWordSuggestionEnabled(this)
    }

    private fun toggleWordSuggestion() {
        val newValue = !SettingsPreferences.getWordSuggestionEnabled(this)
        SettingsPreferences.setWordSuggestionEnabled(this, newValue)
        binding.wordSuggestionSwitch.isChecked = newValue
    }

    private fun updateHapticStrengthDisplay() {
        binding.hapticStrengthValue.text =
            getString(SettingsPreferences.getHapticStrength(this).labelResId)
    }

    private fun showHapticStrengthDialog() {
        val options = HapticStrength.values()
        val labels = options.map { getString(it.labelResId) }.toTypedArray()
        val currentIndex = options.indexOf(SettingsPreferences.getHapticStrength(this))

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_haptic_strength_title)
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                SettingsPreferences.save(this, SettingsPreferences.KEY_HAPTIC_STRENGTH, options[which].name)
                updateHapticStrengthDisplay()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    private fun updateSoundVolumeDisplay() {
        binding.soundVolumeValue.text =
            getString(SettingsPreferences.getSoundVolume(this).labelResId)
    }

    private fun showSoundVolumeDialog() {
        val options = SoundVolume.values()
        val labels = options.map { getString(it.labelResId) }.toTypedArray()
        val currentIndex = options.indexOf(SettingsPreferences.getSoundVolume(this))

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_sound_volume_title)
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                SettingsPreferences.save(this, SettingsPreferences.KEY_SOUND_VOLUME, options[which].name)
                updateSoundVolumeDisplay()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    private fun updateSoundTypeDisplay() {
        binding.soundTypeValue.text =
            getString(SettingsPreferences.getSoundType(this).labelResId)
    }

    private fun showSoundTypeDialog() {
        val options = SoundType.values()
        val labels = options.map { getString(it.labelResId) }.toTypedArray()
        val currentIndex = options.indexOf(SettingsPreferences.getSoundType(this))

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_sound_type_title)
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                SettingsPreferences.save(this, SettingsPreferences.KEY_SOUND_TYPE, options[which].name)
                updateSoundTypeDisplay()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    private fun updateEnterLongPressActionDisplay() {
        binding.enterLongPressActionValue.text =
            getString(SettingsPreferences.getEnterLongPressAction(this).labelResId)
    }

    private fun showEnterLongPressActionDialog() {
        val options = EnterLongPressAction.values()
        val labels = options.map { getString(it.labelResId) }.toTypedArray()
        val currentIndex = options.indexOf(SettingsPreferences.getEnterLongPressAction(this))

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_enter_long_press_title)
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                SettingsPreferences.save(this, SettingsPreferences.KEY_ENTER_LONG_PRESS_ACTION, options[which].name)
                updateEnterLongPressActionDisplay()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    private fun showInputModeDialog() {
        val modes = HangulInputMode.values()
        val labels = modes.map { getString(it.labelResId) }.toTypedArray()
        val currentIndex = modes.indexOf(SettingsPreferences.getHangulInputMode(this))

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_hangul_input_mode_title)
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                SettingsPreferences.save(this, SettingsPreferences.KEY_HANGUL_INPUT_MODE, modes[which].name)
                updateInputModeDisplay()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    private fun showKeypadHeightDialog() {
        val options = KeypadHeight.values()
        val labels = options.map { getString(it.labelResId) }.toTypedArray()
        val currentIndex = options.indexOf(SettingsPreferences.getKeypadHeight(this))

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_keypad_height_title)
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                SettingsPreferences.save(this, SettingsPreferences.KEY_KEYPAD_HEIGHT, options[which].name)
                updateKeypadHeightDisplay()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    private fun showOneHandModeDialog() {
        val options = OneHandMode.values()
        val labels = options.map { getString(it.labelResId) }.toTypedArray()
        val currentIndex = options.indexOf(SettingsPreferences.getOneHandMode(this))

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_one_hand_mode_title)
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                SettingsPreferences.save(this, SettingsPreferences.KEY_ONE_HAND_MODE, options[which].name)
                updateOneHandModeDisplay()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    private fun exportSettings() {
        val prefs = getSharedPreferences(SettingsPreferences.PREFS_NAME, MODE_PRIVATE)
        val json = JSONObject()
        // prefs 파일 하나에 SettingsPreferences, QuickPhraseRepository, QwertyLongKeyRepository 가 함께 저장됨
        prefs.all.forEach { (key, value) ->
            when (value) {
                is Boolean -> json.put(key, value)
                is String -> json.put(key, value)
                is Int -> json.put(key, value)
                is Long -> json.put(key, value)
                is Float -> json.put(key, value.toDouble())
            }
        }
        val jsonText = json.toString(2)
        lifecycleScope.launch(Dispatchers.IO) {
            val msgResId = try {
                val resolver = contentResolver
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
            } catch (e: Exception) {
                Log.e("SettingsActivity", "export failed", e)
                R.string.settings_data_export_fail
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(this@SettingsActivity, msgResId, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun importSettings() {
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
        )
        lifecycleScope.launch(Dispatchers.IO) {
            val success = try {
                val resolver = contentResolver
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
                    return@launch withContext(Dispatchers.Main) {
                        Toast.makeText(this@SettingsActivity, R.string.settings_data_import_fail, Toast.LENGTH_SHORT).show()
                    }
                }
                val jsonText = resolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                    ?: throw Exception("openInputStream failed")
                val json = JSONObject(jsonText)
                val editor = getSharedPreferences(SettingsPreferences.PREFS_NAME, MODE_PRIVATE).edit()
                json.keys().forEach { key ->
                    if (key !in allowedKeys) return@forEach
                    if (key in booleanKeys) {
                        editor.putBoolean(key, json.optBoolean(key))
                    } else if (json.has(key)) {
                        val value = json.optString(key)
                        if (value.isNotEmpty()) editor.putString(key, value)
                    }
                }
                editor.apply()
                true
            } catch (e: Exception) {
                Log.e("SettingsActivity", "import failed", e)
                false
            }
            withContext(Dispatchers.Main) {
                if (success) {
                    refreshAllDisplays()
                    Toast.makeText(this@SettingsActivity, R.string.settings_data_import_success, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SettingsActivity, R.string.settings_data_import_fail, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showResetConfirmDialog() {
        AlertDialog.Builder(this)
            .setMessage(R.string.settings_data_reset_confirm)
            .setPositiveButton(R.string.dialog_confirm) { _, _ -> resetAllSettings() }
            .setNegativeButton(R.string.settings_qwerty_long_key_cancel, null)
            .show()
    }

    private fun resetAllSettings() {
        getSharedPreferences(SettingsPreferences.PREFS_NAME, MODE_PRIVATE).edit().clear().apply()
        refreshAllDisplays()
        Toast.makeText(this, R.string.settings_data_reset_success, Toast.LENGTH_SHORT).show()
    }

    private fun updateGestureAngleDisplay() {
        val preset = getString(SettingsPreferences.getGestureAnglePreset(this).labelResId)
        val threshold = SettingsPreferences.getGestureThreshold(this)
        binding.gestureAngleValue.text = "$preset · ${threshold}dp"
    }

    private fun refreshAllDisplays() {
        updateGestureAngleDisplay()
        updateInputModeDisplay()
        updateKeyboardSkinDisplay()
        updateKeypadHeightDisplay()
        updateOneHandModeDisplay()
        updateLongPressTimeDisplay()
        updateSpaceLongPressActionDisplay()
        updateKeyPreviewDisplay()
        updateAutoSpacePeriodDisplay()
        updateAutoCapitalizeEnglishDisplay()
        updateWordSuggestionDisplay()
        updateEnterLongPressActionDisplay()
        updateHapticStrengthDisplay()
        updateSoundVolumeDisplay()
        updateSoundTypeDisplay()
    }
}
