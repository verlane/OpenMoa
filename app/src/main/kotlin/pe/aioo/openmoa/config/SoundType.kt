package pe.aioo.openmoa.config

import android.media.AudioManager
import pe.aioo.openmoa.R

enum class SoundType(val labelResId: Int, val effectId: Int) {
    STANDARD(R.string.settings_sound_type_standard, AudioManager.FX_KEYPRESS_STANDARD),
    CLICK(R.string.settings_sound_type_click, AudioManager.FX_KEY_CLICK),
    SPACEBAR(R.string.settings_sound_type_spacebar, AudioManager.FX_KEYPRESS_SPACEBAR),
    DELETE(R.string.settings_sound_type_delete, AudioManager.FX_KEYPRESS_DELETE),
    RETURN(R.string.settings_sound_type_return, AudioManager.FX_KEYPRESS_RETURN);

    companion object {
        fun fromString(value: String?): SoundType =
            values().find { it.name == value } ?: STANDARD
    }
}
