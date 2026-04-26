package pe.aioo.openmoa.settings

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import pe.aioo.openmoa.R
import pe.aioo.openmoa.databinding.ActivityShortcutSettingsBinding
import pe.aioo.openmoa.quickphrase.NumberLongKey
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
        buildNumberLongKeyItems()
    }

    private fun refreshAllDisplays() {
        QuickPhraseKey.values().forEach { key ->
            quickPhraseValueView(key).text = key.getPhrase(this)
        }
        QwertyLongKey.values().forEach { key ->
            qwertyLongKeyValueView(key).text = key.getPhrase(this)
        }
        refreshNumberLongKeyDisplays()
    }

    private fun buildNumberLongKeyItems() {
        val container = binding.numberLongKeyContainer
        container.removeAllViews()
        NumberLongKey.values().forEach { key ->
            val dp12 = (12 * resources.displayMetrics.density).toInt()
            val dp16 = (16 * resources.displayMetrics.density).toInt()
            val labelView = android.widget.TextView(this).apply {
                text = getString(R.string.settings_number_long_key_label_format, key.digit)
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            val valueView = android.widget.TextView(this).apply {
                id = android.view.View.generateViewId()
                tag = key.name
                text = key.getPhrase(this@ShortcutSettingsActivity)
                textSize = 14f
                setTextColor(android.graphics.Color.parseColor("#888888"))
                setPadding(0, (2 * resources.displayMetrics.density).toInt(), 0, 0)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            val itemView = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp16, dp12, dp16, dp12)
                isClickable = true
                isFocusable = true
                setBackgroundResource(android.R.attr.selectableItemBackground.let {
                    val tv = android.util.TypedValue()
                    theme.resolveAttribute(it, tv, true)
                    tv.resourceId
                })
                addView(labelView)
                addView(valueView)
                setOnClickListener { showEditDialog(key) }
            }
            container.addView(itemView)
        }
    }

    private fun refreshNumberLongKeyDisplays() {
        val container = binding.numberLongKeyContainer
        for (i in 0 until container.childCount) {
            val item = container.getChildAt(i) as? LinearLayout ?: continue
            val valueView = item.getChildAt(1) as? android.widget.TextView ?: continue
            val keyName = valueView.tag as? String ?: continue
            val key = NumberLongKey.values().find { it.name == keyName } ?: continue
            valueView.text = key.getPhrase(this)
        }
    }

    private fun showEditDialog(key: PhraseKey) {
        val hintResId = R.string.settings_quick_phrase_edit_hint
        val dp16 = (16 * resources.displayMetrics.density).toInt()
        val editText = EditText(this).apply {
            setText(key.getPhrase(this@ShortcutSettingsActivity))
            hint = getString(hintResId)
            setSingleLine(true)
        }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp16, dp16, dp16, 0)
            addView(editText)
        }
        AlertDialog.Builder(this)
            .setTitle("${key.displayName} ${getString(hintResId)}")
            .setView(container)
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
