package pe.aioo.openmoa.config

import pe.aioo.openmoa.R

enum class SoundVolume(val labelResId: Int, val volume: Float) {
    OFF(R.string.settings_sound_volume_off, 0f),
    LOW(R.string.settings_sound_volume_low, 0.3f),
    MEDIUM(R.string.settings_sound_volume_medium, 0.6f),
    HIGH(R.string.settings_sound_volume_high, 1.0f);

    companion object {
        fun fromString(value: String?): SoundVolume =
            values().find { it.name == value } ?: OFF
    }
}
