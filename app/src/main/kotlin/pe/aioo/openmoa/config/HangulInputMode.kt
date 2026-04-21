package pe.aioo.openmoa.config

import pe.aioo.openmoa.R

enum class HangulInputMode(val labelResId: Int) {
    TWO_HAND_MOAKEY(R.string.settings_input_mode_two_hand_moakey),
    MOAKEY(R.string.settings_input_mode_moakey),
    MOAKEY_PLUS(R.string.settings_input_mode_moakey_plus);

    val isMoakeyLayout: Boolean get() = this == MOAKEY || this == MOAKEY_PLUS
    val showsMoeumKey: Boolean get() = this != MOAKEY_PLUS

    companion object {
        fun fromString(value: String?): HangulInputMode =
            values().find { it.name == value } ?: TWO_HAND_MOAKEY
    }
}
