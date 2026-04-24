package pe.aioo.openmoa.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.koin.android.ext.android.inject
import pe.aioo.openmoa.R
import pe.aioo.openmoa.config.HapticStrength
import pe.aioo.openmoa.databinding.ActivityDevToolsBinding
import pe.aioo.openmoa.view.feedback.KeyFeedbackPlayer

class DevToolsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDevToolsBinding
    private val feedbackPlayer: KeyFeedbackPlayer by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevToolsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.amplitudeStatusText.setText(
            if (feedbackPlayer.hasAmplitudeControl()) R.string.dev_tools_amplitude_supported
            else R.string.dev_tools_amplitude_not_supported
        )

        binding.hapticLightButton.setOnClickListener {
            feedbackPlayer.playHaptic(HapticStrength.LIGHT.durationMs, HapticStrength.LIGHT.amplitude)
        }
        binding.hapticMediumButton.setOnClickListener {
            feedbackPlayer.playHaptic(HapticStrength.MEDIUM.durationMs, HapticStrength.MEDIUM.amplitude)
        }
        binding.hapticStrongButton.setOnClickListener {
            feedbackPlayer.playHaptic(HapticStrength.STRONG.durationMs, HapticStrength.STRONG.amplitude)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
