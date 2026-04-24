package pe.aioo.openmoa.settings

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import pe.aioo.openmoa.R
import pe.aioo.openmoa.databinding.ActivityShortcutSettingsBinding
import pe.aioo.openmoa.quickphrase.PhraseKey
import pe.aioo.openmoa.quickphrase.QuickPhraseKey
import pe.aioo.openmoa.quickphrase.QwertyLongKey

class ShortcutSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShortcutSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShortcutSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupViews()
        refreshAllDisplays()
    }

    override fun onResume() {
        super.onResume()
        refreshAllDisplays()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupViews() {
        QuickPhraseKey.values().forEach { key ->
            quickPhraseItemView(key).setOnClickListener { showEditDialog(key) }
        }
        QwertyLongKey.values().forEach { key ->
            qwertyLongKeyItemView(key).setOnClickListener { showEditDialog(key) }
        }
    }

    private fun refreshAllDisplays() {
        QuickPhraseKey.values().forEach { key ->
            quickPhraseValueView(key).text = key.getPhrase(this)
        }
        QwertyLongKey.values().forEach { key ->
            qwertyLongKeyValueView(key).text = key.getPhrase(this)
        }
    }

    private fun showEditDialog(key: PhraseKey) {
        val hintResId = R.string.settings_quick_phrase_edit_hint
        val editText = EditText(this).apply {
            setText(key.getPhrase(this@ShortcutSettingsActivity))
            hint = getString(hintResId)
            setSingleLine(true)
        }
        AlertDialog.Builder(this)
            .setTitle("${key.displayName} ${getString(hintResId)}")
            .setView(editText)
            .setPositiveButton(R.string.settings_qwerty_long_key_save) { _, _ ->
                val input = editText.text.toString()
                key.setPhrase(this, input.ifEmpty { key.defaultPhrase })
                refreshAllDisplays()
            }
            .setNeutralButton(R.string.settings_qwerty_long_key_reset) { _, _ ->
                key.setPhrase(this, key.defaultPhrase)
                refreshAllDisplays()
            }
            .setNegativeButton(R.string.settings_qwerty_long_key_cancel, null)
            .show()
    }

    private fun quickPhraseItemView(key: QuickPhraseKey) = when (key) {
        QuickPhraseKey.KIEUK -> binding.quickPhraseKieukItem
        QuickPhraseKey.TIEUT -> binding.quickPhraseTieutItem
        QuickPhraseKey.CHIEUT -> binding.quickPhraseChieutItem
        QuickPhraseKey.PIEUP -> binding.quickPhrasePieupItem
        QuickPhraseKey.SSANGBIEUP -> binding.quickPhraseSsangbieupItem
        QuickPhraseKey.SSANGJIEUT -> binding.quickPhraseSsangjieutItem
        QuickPhraseKey.SSANGDIGEUT -> binding.quickPhraseSsangdigeutItem
        QuickPhraseKey.SSANGGIYEOK -> binding.quickPhraseSsanggiyeokItem
        QuickPhraseKey.SSANGSIOT -> binding.quickPhraseSsangsiotItem
    }

    private fun quickPhraseValueView(key: QuickPhraseKey) = when (key) {
        QuickPhraseKey.KIEUK -> binding.quickPhraseKieukValue
        QuickPhraseKey.TIEUT -> binding.quickPhraseTieutValue
        QuickPhraseKey.CHIEUT -> binding.quickPhraseChieutValue
        QuickPhraseKey.PIEUP -> binding.quickPhrasePieupValue
        QuickPhraseKey.SSANGBIEUP -> binding.quickPhraseSsangbieupValue
        QuickPhraseKey.SSANGJIEUT -> binding.quickPhraseSsangjieutValue
        QuickPhraseKey.SSANGDIGEUT -> binding.quickPhraseSsangdigeutValue
        QuickPhraseKey.SSANGGIYEOK -> binding.quickPhraseSsanggiyeokValue
        QuickPhraseKey.SSANGSIOT -> binding.quickPhraseSsangsiotValue
    }

    private fun qwertyLongKeyItemView(key: QwertyLongKey) = when (key) {
        QwertyLongKey.Z -> binding.qwertyLongKeyZItem
        QwertyLongKey.X -> binding.qwertyLongKeyXItem
        QwertyLongKey.C -> binding.qwertyLongKeyCItem
        QwertyLongKey.V -> binding.qwertyLongKeyVItem
        QwertyLongKey.B -> binding.qwertyLongKeyBItem
        QwertyLongKey.N -> binding.qwertyLongKeyNItem
        QwertyLongKey.M -> binding.qwertyLongKeyMItem
    }

    private fun qwertyLongKeyValueView(key: QwertyLongKey) = when (key) {
        QwertyLongKey.Z -> binding.qwertyLongKeyZValue
        QwertyLongKey.X -> binding.qwertyLongKeyXValue
        QwertyLongKey.C -> binding.qwertyLongKeyCValue
        QwertyLongKey.V -> binding.qwertyLongKeyVValue
        QwertyLongKey.B -> binding.qwertyLongKeyBValue
        QwertyLongKey.N -> binding.qwertyLongKeyNValue
        QwertyLongKey.M -> binding.qwertyLongKeyMValue
    }
}
