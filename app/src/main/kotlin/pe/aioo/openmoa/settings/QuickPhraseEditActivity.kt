package pe.aioo.openmoa.settings

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import pe.aioo.openmoa.databinding.ActivityQuickPhraseEditBinding
import pe.aioo.openmoa.quickphrase.QuickPhraseKey
import pe.aioo.openmoa.quickphrase.QuickPhraseRepository

class QuickPhraseEditActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_KEY = "extra_key"
    }

    private lateinit var binding: ActivityQuickPhraseEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuickPhraseEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val initialKey = intent.getStringExtra(EXTRA_KEY)
            ?.let { runCatching { QuickPhraseKey.valueOf(it) }.getOrNull() }
            ?: QuickPhraseKey.KIEUK

        setupSpinner(initialKey)
        setupButtons()
    }

    private fun setupSpinner(initialKey: QuickPhraseKey) {
        val keys = QuickPhraseKey.values()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, keys.map { it.jaum })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.keySpinner.adapter = adapter
        binding.keySpinner.setSelection(keys.indexOf(initialKey))
        binding.keySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedKey = QuickPhraseKey.values()[position]
                binding.contentEditText.setText(QuickPhraseRepository.getPhrase(this@QuickPhraseEditActivity, selectedKey))
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupButtons() {
        binding.confirmButton.setOnClickListener {
            val selectedKey = QuickPhraseKey.values()[binding.keySpinner.selectedItemPosition]
            val content = binding.contentEditText.text.toString().trim()
            if (content.isNotEmpty()) {
                QuickPhraseRepository.setPhrase(this, selectedKey, content)
            }
            finish()
        }
        binding.cancelButton.setOnClickListener { finish() }
        binding.resetButton.setOnClickListener {
            val selectedKey = QuickPhraseKey.values()[binding.keySpinner.selectedItemPosition]
            QuickPhraseRepository.setPhrase(this, selectedKey, selectedKey.defaultPhrase)
            binding.contentEditText.setText(selectedKey.defaultPhrase)
        }
    }
}
