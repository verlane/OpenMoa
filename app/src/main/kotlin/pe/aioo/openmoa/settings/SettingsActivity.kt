package pe.aioo.openmoa.settings

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import pe.aioo.openmoa.R
import pe.aioo.openmoa.config.HangulInputMode
import pe.aioo.openmoa.config.KeyboardSkin
import pe.aioo.openmoa.config.KeypadHeight
import pe.aioo.openmoa.config.LongPressTime
import pe.aioo.openmoa.config.OneHandMode
import pe.aioo.openmoa.config.SpaceLongPressAction
import pe.aioo.openmoa.databinding.ActivitySettingsBinding
import pe.aioo.openmoa.quickphrase.QuickPhraseKey
import pe.aioo.openmoa.quickphrase.QuickPhraseRepository

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViews()
    }

    private fun setupViews() {
        updateInputModeDisplay()
        updateKeyboardSkinDisplay()
        updateKeypadHeightDisplay()
        updateOneHandModeDisplay()
        updateLongPressTimeDisplay()
        updateSpaceLongPressActionDisplay()
        updateKeyPreviewDisplay()
        updateAutoSpacePeriodDisplay()
        updateQuickPhraseDisplays()

        binding.hangulInputModeItem.setOnClickListener { showInputModeDialog() }
        binding.keyboardSkinItem.setOnClickListener { showKeyboardSkinDialog() }
        binding.keypadHeightItem.setOnClickListener { showKeypadHeightDialog() }
        binding.oneHandModeItem.setOnClickListener { showOneHandModeDialog() }
        binding.longPressTimeItem.setOnClickListener { showLongPressTimeDialog() }
        binding.spaceLongPressActionItem.setOnClickListener { showSpaceLongPressActionDialog() }
        binding.keyPreviewItem.setOnClickListener { toggleKeyPreview() }
        binding.autoSpacePeriodItem.setOnClickListener { toggleAutoSpacePeriod() }
        binding.quickPhraseKieukItem.setOnClickListener { showQuickPhraseEditDialog(QuickPhraseKey.KIEUK) }
        binding.quickPhraseTieutItem.setOnClickListener { showQuickPhraseEditDialog(QuickPhraseKey.TIEUT) }
        binding.quickPhraseChieutItem.setOnClickListener { showQuickPhraseEditDialog(QuickPhraseKey.CHIEUT) }
        binding.quickPhrasePieupItem.setOnClickListener { showQuickPhraseEditDialog(QuickPhraseKey.PIEUP) }
        binding.quickPhraseSsangbieupItem.setOnClickListener { showQuickPhraseEditDialog(QuickPhraseKey.SSANGBIEUP) }
        binding.quickPhraseSsangjieutItem.setOnClickListener { showQuickPhraseEditDialog(QuickPhraseKey.SSANGJIEUT) }
        binding.quickPhraseSsangdigeutItem.setOnClickListener { showQuickPhraseEditDialog(QuickPhraseKey.SSANGDIGEUT) }
        binding.quickPhraseSsanggiyeokItem.setOnClickListener { showQuickPhraseEditDialog(QuickPhraseKey.SSANGGIYEOK) }
        binding.quickPhraseSsangsiotItem.setOnClickListener { showQuickPhraseEditDialog(QuickPhraseKey.SSANGSIOT) }
    }

    private fun updateQuickPhraseDisplays() {
        binding.quickPhraseKieukValue.text = QuickPhraseRepository.getPhrase(this, QuickPhraseKey.KIEUK)
        binding.quickPhraseTieutValue.text = QuickPhraseRepository.getPhrase(this, QuickPhraseKey.TIEUT)
        binding.quickPhraseChieutValue.text = QuickPhraseRepository.getPhrase(this, QuickPhraseKey.CHIEUT)
        binding.quickPhrasePieupValue.text = QuickPhraseRepository.getPhrase(this, QuickPhraseKey.PIEUP)
        binding.quickPhraseSsangbieupValue.text = QuickPhraseRepository.getPhrase(this, QuickPhraseKey.SSANGBIEUP)
        binding.quickPhraseSsangjieutValue.text = QuickPhraseRepository.getPhrase(this, QuickPhraseKey.SSANGJIEUT)
        binding.quickPhraseSsangdigeutValue.text = QuickPhraseRepository.getPhrase(this, QuickPhraseKey.SSANGDIGEUT)
        binding.quickPhraseSsanggiyeokValue.text = QuickPhraseRepository.getPhrase(this, QuickPhraseKey.SSANGGIYEOK)
        binding.quickPhraseSsangsiotValue.text = QuickPhraseRepository.getPhrase(this, QuickPhraseKey.SSANGSIOT)
    }

    private fun showQuickPhraseEditDialog(key: QuickPhraseKey) {
        val editText = EditText(this).apply {
            setText(QuickPhraseRepository.getPhrase(this@SettingsActivity, key))
            hint = getString(R.string.settings_quick_phrase_edit_hint)
            setSingleLine(true)
        }
        AlertDialog.Builder(this)
            .setTitle("${key.jaum} ${getString(R.string.settings_quick_phrase_edit_hint)}")
            .setView(editText)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val input = editText.text.toString().trim()
                if (input.isNotEmpty()) {
                    QuickPhraseRepository.setPhrase(this, key, input)
                } else {
                    QuickPhraseRepository.setPhrase(this, key, key.defaultPhrase)
                }
                updateQuickPhraseDisplays()
            }
            .setNeutralButton(R.string.settings_quick_phrase_reset) { _, _ ->
                QuickPhraseRepository.setPhrase(this, key, key.defaultPhrase)
                updateQuickPhraseDisplays()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
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
            .setNegativeButton(android.R.string.cancel, null)
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
            .setNegativeButton(android.R.string.cancel, null)
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
            .setNegativeButton(android.R.string.cancel, null)
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
            .setNegativeButton(android.R.string.cancel, null)
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
            .setNegativeButton(android.R.string.cancel, null)
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
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
