package pe.aioo.openmoa.config

import pe.aioo.openmoa.R

enum class HangulInputMode(val labelResId: Int) {
    TWO_HAND_MOAKEY(R.string.settings_input_mode_two_hand_moakey),
    MOAKEY(R.string.settings_input_mode_moakey),
    MOAKEY_PLUS(R.string.settings_input_mode_moakey_plus),
    QWERTY(R.string.settings_input_mode_qwerty),
    QWERTY_SIMPLE(R.string.settings_input_mode_qwerty_simple);

    val isMoakeyLayout: Boolean get() = this == MOAKEY || this == MOAKEY_PLUS
    val isQwertyLayout: Boolean get() = this == QWERTY || this == QWERTY_SIMPLE
    val isSimpleQwerty: Boolean get() = this == QWERTY_SIMPLE
    val showsMoeumKey: Boolean get() = this == TWO_HAND_MOAKEY || this == MOAKEY

    companion object {
        fun fromString(value: String?): HangulInputMode =
            values().find { it.name == value } ?: TWO_HAND_MOAKEY
    }
}
