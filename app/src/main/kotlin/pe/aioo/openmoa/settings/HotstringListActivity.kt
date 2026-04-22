package pe.aioo.openmoa.settings

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import pe.aioo.openmoa.R
import pe.aioo.openmoa.databinding.ActivityHotstringListBinding
import pe.aioo.openmoa.hotstring.HotstringRepository
import pe.aioo.openmoa.hotstring.HotstringRule
import pe.aioo.openmoa.settings.SettingsPreferences

class HotstringListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHotstringListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHotstringListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        HotstringRepository.ensureDefaults(this)
        binding.enabledSwitch.isChecked = SettingsPreferences.getHotstringEnabled(this)
        binding.enabledItem.setOnClickListener {
            val newValue = !SettingsPreferences.getHotstringEnabled(this)
            SettingsPreferences.setHotstringEnabled(this, newValue)
            binding.enabledSwitch.isChecked = newValue
        }
        binding.addButton.setOnClickListener { showEditDialog(null) }
        refreshList()
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        val container = binding.ruleListContainer
        val rules = HotstringRepository.getAll(this)

        // emptyText를 제외한 rule view 제거
        val viewsToRemove = mutableListOf<View>()
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child.id != R.id.emptyText) viewsToRemove.add(child)
        }
        viewsToRemove.forEach { container.removeView(it) }

        binding.emptyText.visibility = if (rules.isEmpty()) View.VISIBLE else View.GONE

        rules.forEach { rule ->
            container.addView(createRuleView(rule))
            container.addView(createDivider())
        }
    }

    private fun createRuleView(rule: HotstringRule): View {
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
            setOnClickListener { showEditDialog(rule) }
        }

        val textLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val mainText = TextView(this).apply {
            text = "${rule.trigger}  →  ${rule.expansion}"
            textSize = 15f
        }
        textLayout.addView(mainText)
        row.addView(textLayout)

        val switch = SwitchCompat(this).apply {
            isChecked = rule.enabled
            setPadding(dp8, 0, dp8, 0)
            setOnCheckedChangeListener { _, checked ->
                HotstringRepository.upsert(this@HotstringListActivity, rule.copy(enabled = checked))
            }
        }
        row.addView(switch)

        val deleteBtn = TextView(this).apply {
            text = getString(R.string.settings_hotstring_delete)
            textSize = 14f
            setTextColor(context.getColor(android.R.color.holo_red_dark))
            setPadding(dp8, 0, 0, 0)
            setOnClickListener { showDeleteConfirm(rule) }
        }
        row.addView(deleteBtn)

        return row
    }

    private fun createDivider(): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            )
            setBackgroundColor(context.getColor(R.color.keyboard_background))
        }
    }

    private fun showEditDialog(existing: HotstringRule?) {
        val dp16 = (16 * resources.displayMetrics.density).toInt()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp16, dp16, dp16, 0)
        }

        val triggerEdit = EditText(this).apply {
            hint = getString(R.string.settings_hotstring_trigger_hint)
            setText(existing?.trigger ?: "")
            setSingleLine(true)
        }
        val expansionEdit = EditText(this).apply {
            hint = getString(R.string.settings_hotstring_expansion_hint)
            setText(existing?.expansion ?: "")
            setSingleLine(true)
        }

        container.addView(triggerEdit)
        container.addView(expansionEdit)

        AlertDialog.Builder(this)
            .setTitle(existing?.trigger ?: getString(R.string.settings_hotstring_add))
            .setView(container)
            .setPositiveButton(R.string.settings_qwerty_long_key_save) { _, _ ->
                val trigger = triggerEdit.text.toString().trim()
                val expansion = expansionEdit.text.toString().trim()
                when {
                    trigger.isEmpty() -> showToast(R.string.settings_hotstring_trigger_empty)
                    expansion.isEmpty() -> showToast(R.string.settings_hotstring_expansion_empty)
                    HotstringRepository.hasTrigger(this, trigger, existing?.id) ->
                        showDuplicateConfirm(trigger, expansion, existing?.id)
                    else -> saveRule(trigger, expansion, existing?.id)
                }
            }
            .setNegativeButton(R.string.settings_qwerty_long_key_cancel, null)
            .show()
    }

    private fun showDuplicateConfirm(trigger: String, expansion: String, existingId: String?) {
        AlertDialog.Builder(this)
            .setMessage(R.string.settings_hotstring_duplicate_confirm)
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                val duplicateId = HotstringRepository.getAll(this)
                    .firstOrNull { it.trigger == trigger && it.id != existingId }?.id
                if (duplicateId != null) HotstringRepository.delete(this, duplicateId)
                saveRule(trigger, expansion, existingId)
            }
            .setNegativeButton(R.string.settings_qwerty_long_key_cancel, null)
            .show()
    }

    private fun showDeleteConfirm(rule: HotstringRule) {
        AlertDialog.Builder(this)
            .setMessage(R.string.settings_hotstring_delete_confirm)
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                HotstringRepository.delete(this, rule.id)
                refreshList()
            }
            .setNegativeButton(R.string.settings_qwerty_long_key_cancel, null)
            .show()
    }

    private fun saveRule(trigger: String, expansion: String, existingId: String?) {
        val rule = HotstringRule(
            id = existingId ?: HotstringRepository.newId(),
            trigger = trigger,
            expansion = expansion
        )
        HotstringRepository.upsert(this, rule)
        refreshList()
    }

    private fun showToast(messageResId: Int) {
        android.widget.Toast.makeText(this, messageResId, android.widget.Toast.LENGTH_SHORT).show()
    }
}
