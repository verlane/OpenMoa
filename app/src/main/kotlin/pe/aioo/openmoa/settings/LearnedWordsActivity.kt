package pe.aioo.openmoa.settings

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.koin.android.ext.android.get
import org.koin.core.qualifier.named
import pe.aioo.openmoa.R
import pe.aioo.openmoa.databinding.ActivityLearnedWordsBinding
import pe.aioo.openmoa.suggestion.UserWordStore

class LearnedWordsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLearnedWordsBinding
    private lateinit var currentStore: UserWordStore
    private lateinit var koStore: UserWordStore
    private lateinit var enStore: UserWordStore
    private var isKoTab = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearnedWordsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        koStore = get(named("ko"))
        enStore = get(named("en"))
        currentStore = koStore
        binding.tabKoButton.setOnClickListener { switchTab(isKo = true) }
        binding.tabEnButton.setOnClickListener { switchTab(isKo = false) }
        refreshList()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun switchTab(isKo: Boolean) {
        isKoTab = isKo
        currentStore = if (isKo) koStore else enStore
        refreshList()
    }

    private fun refreshList() {
        val container = binding.wordListContainer
        val entries = currentStore.entries().sortedByDescending { it.second }
        val viewsToRemove = mutableListOf<View>()
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child.id != R.id.emptyText) viewsToRemove.add(child)
        }
        viewsToRemove.forEach { container.removeView(it) }
        binding.emptyText.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        entries.forEach { (word, count) -> container.addView(createWordView(word, count)) }
    }

    private fun createWordView(word: String, count: Int): View {
        val dp16 = (16 * resources.displayMetrics.density).toInt()
        val dp8 = (8 * resources.displayMetrics.density).toInt()
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp16, 0, dp16)
            background = obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
                .use { it.getDrawable(0) }
            isClickable = true
            isFocusable = true
            setOnClickListener { showActionDialog(word) }
        }
        val wordText = TextView(this).apply {
            text = word
            textSize = 15f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val countText = TextView(this).apply {
            text = count.toString()
            textSize = 14f
            setTextColor(context.getColor(android.R.color.darker_gray))
            setPadding(dp8, 0, dp8, 0)
        }
        val deleteBtn = TextView(this).apply {
            text = getString(R.string.settings_hotstring_delete)
            textSize = 14f
            setPadding(dp8, 0, 0, 0)
            setOnClickListener { showDeleteConfirm(word) }
        }
        row.addView(wordText)
        row.addView(countText)
        row.addView(deleteBtn)
        return row
    }

    private fun showActionDialog(word: String) {
        val items = arrayOf(
            getString(R.string.suggestion_long_click_remove),
            getString(R.string.suggestion_long_click_blacklist),
        )
        AlertDialog.Builder(this)
            .setTitle(word)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> { currentStore.remove(word); refreshList() }
                    1 -> { currentStore.addToBlacklist(word); refreshList() }
                }
            }
            .show()
    }

    private fun showDeleteConfirm(word: String) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.settings_learned_words_delete_confirm, word))
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                currentStore.remove(word)
                refreshList()
            }
            .setNegativeButton(R.string.settings_qwerty_long_key_cancel, null)
            .show()
    }
}
