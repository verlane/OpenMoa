package pe.aioo.openmoa.clipboard

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import pe.aioo.openmoa.R

class ClipboardEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val entryId = intent.getStringExtra(EXTRA_ENTRY_ID) ?: run { finish(); return }
        val entryText = intent.getStringExtra(EXTRA_ENTRY_TEXT) ?: ""

        val dp = resources.displayMetrics.density
        val dp16 = (16 * dp).toInt()
        val dp8 = (8 * dp).toInt()

        val editText = EditText(this).apply {
            setText(entryText)
            setSelection(entryText.length)
            minLines = 3
            maxLines = 6
        }

        val saveBtn = Button(this).apply {
            text = getString(R.string.clipboard_edit_save)
            setOnClickListener {
                val newText = editText.text.toString()
                if (newText.isNotBlank()) {
                    ClipboardRepository.update(this@ClipboardEditActivity, entryId, newText)
                }
                finish()
            }
        }

        val cancelBtn = Button(this).apply {
            text = getString(R.string.clipboard_edit_cancel)
            setOnClickListener { finish() }
        }

        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(cancelBtn, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(saveBtn, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp16, dp16, dp16, dp8)
            addView(editText, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            addView(btnRow, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        setContentView(root)
        title = getString(R.string.clipboard_edit_title)
    }

    companion object {
        const val EXTRA_ENTRY_ID = "extra_entry_id"
        const val EXTRA_ENTRY_TEXT = "extra_entry_text"
    }
}
