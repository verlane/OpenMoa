package pe.aioo.openmoa.config

import pe.aioo.openmoa.R

enum class HapticStrength(val labelResId: Int, val durationMs: Long, val amplitude: Int) {
    OFF(R.string.settings_haptic_strength_off, 0, 0),
    LIGHT(R.string.settings_haptic_strength_light, 20, 10),
    MEDIUM(R.string.settings_haptic_strength_medium, 50, 100),
    STRONG(R.string.settings_haptic_strength_strong, 100, 255);

    companion object {
        fun fromString(value: String?): HapticStrength =
            values().find { it.name == value } ?: MEDIUM
    }
}
