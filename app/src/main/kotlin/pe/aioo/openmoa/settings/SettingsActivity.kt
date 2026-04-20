package pe.aioo.openmoa.settings

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import pe.aioo.openmoa.R
import pe.aioo.openmoa.config.HangulInputMode
import pe.aioo.openmoa.config.KeypadHeight
import pe.aioo.openmoa.config.OneHandMode
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
        updateKeypadHeightDisplay()
        updateOneHandModeDisplay()
        updateKeyPreviewDisplay()
        updateQuickPhraseDisplays()

        binding.hangulInputModeItem.setOnClickListener { showInputModeDialog() }
        binding.keypadHeightItem.setOnClickListener { showKeypadHeightDialog() }
        binding.oneHandModeItem.setOnClickListener { showOneHandModeDialog() }
        binding.keyPreviewItem.setOnClickListener { toggleKeyPreview() }
        binding.quickPhraseKieukItem.setOnClickListener { showQuickPhraseEditDialog(QuickPhraseKey.KIEUK) }
        binding.quickPhraseTieutItem.setOnClickListener { showQuickPhraseEditDialog(QuickPhraseKey.TIEUT) }
        binding.quickPhraseChieutItem.setOnClickListener { showQuickPhraseEditDialog(QuickPhraseKey.CHIEUT) }
        binding.quickPhrasePieupItem.setOnClickListener { showQuickPhraseEditDialog(QuickPhraseKey.PIEUP) }
    }

    private fun updateQuickPhraseDisplays() {
        binding.quickPhraseKieukValue.text = QuickPhraseRepository.getPhrase(this, QuickPhraseKey.KIEUK)
        binding.quickPhraseTieutValue.text = QuickPhraseRepository.getPhrase(this, QuickPhraseKey.TIEUT)
        binding.quickPhraseChieutValue.text = QuickPhraseRepository.getPhrase(this, QuickPhraseKey.CHIEUT)
        binding.quickPhrasePieupValue.text = QuickPhraseRepository.getPhrase(this, QuickPhraseKey.PIEUP)
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

    private fun updateKeyPreviewDisplay() {
        binding.keyPreviewSwitch.isChecked = SettingsPreferences.getKeyPreviewEnabled(this)
    }

    private fun toggleKeyPreview() {
        val newValue = !SettingsPreferences.getKeyPreviewEnabled(this)
        SettingsPreferences.setKeyPreviewEnabled(this, newValue)
        binding.keyPreviewSwitch.isChecked = newValue
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
