package pe.aioo.openmoa.settings

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.koin.android.ext.android.get
import org.koin.core.qualifier.named
import pe.aioo.openmoa.R
import pe.aioo.openmoa.databinding.ActivityLearnedWordsBinding
import pe.aioo.openmoa.suggestion.UserWordStore
import pe.aioo.openmoa.suggestion.WordTokenizer
import pe.aioo.openmoa.settings.SettingsPreferences

class LearnedWordsActivity : AppCompatActivity() {

    private enum class Tab { KO, EN, BLACKLIST }

    private lateinit var binding: ActivityLearnedWordsBinding
    private lateinit var koStore: UserWordStore
    private lateinit var enStore: UserWordStore
    private var currentTab = Tab.KO

    private val dp16 by lazy { (16 * resources.displayMetrics.density).toInt() }
    private val dp8 by lazy { (8 * resources.displayMetrics.density).toInt() }
    private val selectableBackground: Drawable? by lazy {
        val ta = obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
        try { ta.getDrawable(0) } finally { ta.recycle() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearnedWordsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        koStore = get(named("ko"))
        enStore = get(named("en"))
        binding.tabKoButton.setOnClickListener { switchTab(Tab.KO) }
        binding.tabEnButton.setOnClickListener { switchTab(Tab.EN) }
        binding.tabBlacklistButton.setOnClickListener { switchTab(Tab.BLACKLIST) }
        binding.addButton.setOnClickListener { showAddWordDialog() }
        refreshList()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_learned_words, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val isWordTab = currentTab != Tab.BLACKLIST
        menu.findItem(R.id.menu_delete_all)?.isVisible = isWordTab
        menu.findItem(R.id.menu_prune_30)?.isVisible = isWordTab
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_delete_all -> { showDeleteAllConfirm(); return true }
            R.id.menu_prune_30 -> { showPruneConfirm(); return true }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun switchTab(tab: Tab) {
        currentTab = tab
        binding.addButton.visibility = if (tab == Tab.BLACKLIST) View.GONE else View.VISIBLE
        invalidateOptionsMenu()
        refreshList()
    }

    private fun refreshList() {
        when (currentTab) {
            Tab.KO -> refreshWordList(koStore)
            Tab.EN -> refreshWordList(enStore)
            Tab.BLACKLIST -> refreshBlacklist()
        }
    }

    private fun refreshWordList(store: UserWordStore) {
        val entries = store.entries().sortedByDescending { it.second }
        clearWordViews()
        binding.emptyText.text = getString(R.string.settings_learned_words_empty)
        binding.emptyText.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        entries.forEach { (word, count) -> binding.wordListContainer.addView(createWordView(word, count)) }
    }

    private fun refreshBlacklist() {
        val koBlacklist = koStore.blacklist().sorted().map { it to false }
        val enBlacklist = enStore.blacklist().sorted().map { it to true }
        val combined = koBlacklist + enBlacklist
        clearWordViews()
        binding.emptyText.text = getString(R.string.settings_learned_words_blacklist_empty)
        binding.emptyText.visibility = if (combined.isEmpty()) View.VISIBLE else View.GONE
        combined.forEach { (word, isEn) -> binding.wordListContainer.addView(createBlacklistWordView(word, isEn)) }
    }

    private fun clearWordViews() {
        val container = binding.wordListContainer
        for (i in container.childCount - 1 downTo 0) {
            val child = container.getChildAt(i)
            if (child.id != R.id.emptyText) container.removeViewAt(i)
        }
    }

    private fun buildRowLayout(onClick: () -> Unit): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp16, 0, dp16)
            background = selectableBackground
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
        }

    private fun createWordView(word: String, count: Int): View {
        val row = buildRowLayout { showActionDialog(word) }
        row.addView(TextView(this).apply {
            text = word
            textSize = 15f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        row.addView(TextView(this).apply {
            text = count.toString()
            textSize = 14f
            setTextColor(context.getColor(android.R.color.darker_gray))
            setPadding(dp8, 0, dp8, 0)
        })
        row.addView(TextView(this).apply {
            text = getString(R.string.settings_hotstring_delete)
            textSize = 14f
            setPadding(dp8, 0, 0, 0)
            setOnClickListener { showDeleteConfirm(word) }
        })
        return row
    }

    private fun createBlacklistWordView(word: String, isEn: Boolean): View {
        val langLabel = getString(if (isEn) R.string.settings_lang_label_en else R.string.settings_lang_label_ko)
        val row = buildRowLayout { showBlacklistReleaseConfirm(word, isEn) }
        row.addView(TextView(this).apply {
            text = word
            textSize = 15f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        row.addView(TextView(this).apply {
            text = langLabel
            textSize = 12f
            setTextColor(context.getColor(android.R.color.darker_gray))
            setPadding(dp8, 0, dp8, 0)
        })
        row.addView(TextView(this).apply {
            text = getString(R.string.settings_learned_words_blacklist_release)
            textSize = 14f
            setPadding(dp8, 0, 0, 0)
            setOnClickListener { showBlacklistReleaseConfirm(word, isEn) }
        })
        return row
    }

    private fun showActionDialog(word: String) {
        val store = when (currentTab) {
            Tab.KO -> koStore
            Tab.EN -> enStore
            Tab.BLACKLIST -> return
        }
        val items = arrayOf(
            getString(R.string.suggestion_long_click_remove),
            getString(R.string.suggestion_long_click_blacklist),
        )
        AlertDialog.Builder(this)
            .setTitle(word)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> { store.remove(word); refreshList() }
                    1 -> { store.addToBlacklist(word); refreshList() }
                }
            }
            .show()
    }

    private fun showDeleteConfirm(word: String) {
        val store = when (currentTab) {
            Tab.KO -> koStore
            Tab.EN -> enStore
            Tab.BLACKLIST -> return
        }
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.settings_learned_words_delete_confirm, word))
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                store.remove(word)
                refreshList()
            }
            .setNegativeButton(R.string.settings_qwerty_long_key_cancel, null)
            .show()
    }

    private fun showDeleteAllConfirm() {
        val store = when (currentTab) {
            Tab.KO -> koStore
            Tab.EN -> enStore
            Tab.BLACKLIST -> return
        }
        val count = store.entries().size
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.settings_learned_words_delete_all_confirm) + " ($count)")
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                store.clear()
                refreshList()
            }
            .setNegativeButton(R.string.settings_qwerty_long_key_cancel, null)
            .show()
    }

    private fun showAddWordDialog() {
        val isKo = currentTab == Tab.KO
        val store = if (isKo) koStore else enStore
        val hint = getString(
            if (isKo) R.string.settings_learned_words_add_hint_ko
            else R.string.settings_learned_words_add_hint_en
        )
        val input = EditText(this).apply {
            this.hint = hint
            setSingleLine()
            setPadding(dp16, dp16, dp16, dp16)
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.settings_learned_words_add))
            .setView(input)
            .setPositiveButton(R.string.dialog_confirm, null)
            .setNegativeButton(R.string.settings_qwerty_long_key_cancel, null)
            .create()
        dialog.setOnShowListener {
            input.requestFocus()
            getSystemService(InputMethodManager::class.java)
                .showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val raw = input.text.toString()
                val word = if (isKo) WordTokenizer.extractKorean(raw) else WordTokenizer.extractEnglish(raw)
                when {
                    word == null -> input.error = getString(
                        if (isKo) R.string.settings_learned_words_add_invalid_ko
                        else R.string.settings_learned_words_add_invalid_en
                    )
                    store.contains(word) -> input.error = getString(R.string.settings_learned_words_add_duplicate)
                    else -> {
                        val minCount = SettingsPreferences.getMinLearnCount(this@LearnedWordsActivity)
                        store.importWords(mapOf(word to minCount))
                        dialog.dismiss()
                        refreshList()
                    }
                }
            }
        }
        dialog.show()
    }

    private fun showPruneConfirm() {
        val store = when (currentTab) {
            Tab.KO -> koStore
            Tab.EN -> enStore
            Tab.BLACKLIST -> return
        }
        AlertDialog.Builder(this)
            .setMessage(R.string.settings_learned_words_prune_confirm)
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                val removed = store.pruneOlderThan(30)
                val msg = if (removed > 0) {
                    getString(R.string.settings_learned_words_prune_result, removed)
                } else {
                    getString(R.string.settings_learned_words_prune_none)
                }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                if (removed > 0) refreshList()
            }
            .setNegativeButton(R.string.settings_qwerty_long_key_cancel, null)
            .show()
    }

    private fun showBlacklistReleaseConfirm(word: String, isEn: Boolean) {
        val store = if (isEn) enStore else koStore
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.settings_learned_words_blacklist_release_confirm, word))
            .setPositiveButton(R.string.settings_learned_words_blacklist_release) { _, _ ->
                store.removeFromBlacklist(word)
                refreshBlacklist()
            }
            .setNegativeButton(R.string.settings_qwerty_long_key_cancel, null)
            .show()
    }
}
