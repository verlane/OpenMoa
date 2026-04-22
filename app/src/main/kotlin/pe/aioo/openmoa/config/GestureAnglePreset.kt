package pe.aioo.openmoa.config

import pe.aioo.openmoa.R

enum class GestureAnglePreset(val labelResId: Int, val angles: IntArray?) {
    RIGHT_HAND(R.string.gesture_angle_preset_right_hand, GestureAngles.RIGHT_HAND),
    LEFT_HAND(R.string.gesture_angle_preset_left_hand, GestureAngles.LEFT_HAND),
    BOTH_HANDS(R.string.gesture_angle_preset_both_hands, GestureAngles.BOTH_HANDS),
    SIX_DIR(R.string.gesture_angle_preset_six_dir, GestureAngles.SIX_DIR),
    CUSTOM(R.string.gesture_angle_preset_custom, null);

    companion object {
        fun fromString(s: String?): GestureAnglePreset =
            values().find { it.name == s } ?: RIGHT_HAND
    }
}
