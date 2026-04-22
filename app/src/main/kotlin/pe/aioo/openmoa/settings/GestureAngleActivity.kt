package pe.aioo.openmoa.settings

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pe.aioo.openmoa.R
import pe.aioo.openmoa.config.GestureAnglePreset
import pe.aioo.openmoa.config.GestureAngles
import pe.aioo.openmoa.databinding.ActivityGestureAngleBinding

class GestureAngleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestureAngleBinding
    private val currentAngles = IntArray(8)
    private var suppressSpinnerCallback = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestureAngleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SettingsPreferences.getGestureAngles(this).values.copyInto(currentAngles)

        setupSpinner()

        binding.gestureAngleView.setAngles(currentAngles)
        binding.gestureAngleView.onAnglesChanged = { newAngles ->
            newAngles.copyInto(currentAngles)
            if (binding.presetSpinner.selectedItemPosition != GestureAnglePreset.CUSTOM.ordinal) {
                suppressSpinnerCallback = true
                binding.presetSpinner.setSelection(GestureAnglePreset.CUSTOM.ordinal)
                binding.presetSpinner.post { suppressSpinnerCallback = false }
            }
        }

        binding.applyButton.setOnClickListener {
            val preset = GestureAnglePreset.values()[binding.presetSpinner.selectedItemPosition]
            SettingsPreferences.setGestureAngles(this, GestureAngles(currentAngles.copyOf()))
            SettingsPreferences.setGestureAnglePreset(this, preset)
            Toast.makeText(this, R.string.gesture_angle_applied, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupSpinner() {
        val presets = GestureAnglePreset.values()
        val labels = presets.map { getString(it.labelResId) }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        suppressSpinnerCallback = true
        binding.presetSpinner.adapter = adapter

        val savedPreset = SettingsPreferences.getGestureAnglePreset(this)
        binding.presetSpinner.setSelection(savedPreset.ordinal)

        binding.presetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (suppressSpinnerCallback) return
                val presetAngles = presets[position].angles ?: return
                presetAngles.copyInto(currentAngles)
                binding.gestureAngleView.setAngles(currentAngles)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.presetSpinner.post { suppressSpinnerCallback = false }
    }
}
