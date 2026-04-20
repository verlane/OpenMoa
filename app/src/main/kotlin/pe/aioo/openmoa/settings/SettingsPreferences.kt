package pe.aioo.openmoa.settings

import android.content.Context
import pe.aioo.openmoa.config.HangulInputMode
import pe.aioo.openmoa.config.KeypadHeight
import pe.aioo.openmoa.config.LongPressTime
import pe.aioo.openmoa.config.OneHandMode

object SettingsPreferences {

    const val PREFS_NAME = "openmoa_settings"
    const val KEY_HANGUL_INPUT_MODE = "hangul_input_mode"
    const val KEY_KEYPAD_HEIGHT = "keypad_height"
    const val KEY_ONE_HAND_MODE = "one_hand_mode"
    const val KEY_KEY_PREVIEW = "key_preview_enabled"
    const val KEY_LONG_PRESS_TIME = "long_press_time"

    fun getKeyPreviewEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_KEY_PREVIEW, true)
    }

    fun setKeyPreviewEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_KEY_PREVIEW, enabled)
            .apply()
    }

    fun getHangulInputMode(context: Context): HangulInputMode {
        val value = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_HANGUL_INPUT_MODE, null)
        return HangulInputMode.fromString(value)
    }

    fun getKeypadHeight(context: Context): KeypadHeight {
        val value = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_KEYPAD_HEIGHT, null)
        return KeypadHeight.fromString(value)
    }

    fun getLongPressTime(context: Context): LongPressTime {
        val value = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LONG_PRESS_TIME, null)
        return LongPressTime.fromString(value)
    }

    fun getOneHandMode(context: Context): OneHandMode {
        val value = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ONE_HAND_MODE, null)
        return OneHandMode.fromString(value)
    }

    fun save(context: Context, key: String, value: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(key, value)
            .apply()
    }
}
