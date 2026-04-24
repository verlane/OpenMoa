package pe.aioo.openmoa.settings

import android.content.Context
import pe.aioo.openmoa.config.EnterLongPressAction
import pe.aioo.openmoa.config.GestureAnglePreset
import pe.aioo.openmoa.config.GestureAngles
import pe.aioo.openmoa.config.HangulInputMode
import pe.aioo.openmoa.config.HapticStrength
import pe.aioo.openmoa.config.KeyboardSkin
import pe.aioo.openmoa.config.SoundType
import pe.aioo.openmoa.config.KeypadHeight
import pe.aioo.openmoa.config.LongPressTime
import pe.aioo.openmoa.config.OneHandMode
import pe.aioo.openmoa.config.SoundVolume
import pe.aioo.openmoa.config.SpaceLongPressAction
import pe.aioo.openmoa.hotstring.HotstringSortOrder
import pe.aioo.openmoa.quickphrase.UserCharKey

object SettingsPreferences {

    const val PREFS_NAME = "openmoa_settings"
    const val KEY_HANGUL_INPUT_MODE = "hangul_input_mode"
    const val KEY_KEYPAD_HEIGHT = "keypad_height"
    const val KEY_ONE_HAND_MODE = "one_hand_mode"
    const val KEY_KEY_PREVIEW = "key_preview_enabled"
    const val KEY_LONG_PRESS_TIME = "long_press_time"
    const val KEY_SPACE_LONG_PRESS_ACTION = "space_long_press_action"
    const val KEY_AUTO_SPACE_PERIOD = "auto_space_period"
    const val KEY_KEYBOARD_SKIN = "keyboard_skin"
    const val KEY_AUTO_CAPITALIZE_ENGLISH = "auto_capitalize_english"
    const val KEY_ENTER_LONG_PRESS_ACTION = "enter_long_press_action"
    const val KEY_GESTURE_ANGLES = "gesture_angles"
    const val KEY_GESTURE_ANGLE_PRESET = "gesture_angle_preset"
    const val KEY_GESTURE_THRESHOLD = "gesture_threshold"
    const val KEY_HOTSTRING_ENABLED = "hotstring_enabled"
    const val KEY_HOTSTRING_SORT_ORDER = "hotstring_sort_order"
    const val KEY_HAPTIC_STRENGTH = "haptic_strength"
    const val KEY_SOUND_VOLUME = "sound_volume"
    const val KEY_SOUND_TYPE = "sound_type"
    const val KEY_WORD_SUGGESTION_ENABLED = "word_suggestion_enabled"

    val ALL_KEYS = setOf(
        KEY_HANGUL_INPUT_MODE,
        KEY_KEYPAD_HEIGHT,
        KEY_ONE_HAND_MODE,
        KEY_KEY_PREVIEW,
        KEY_LONG_PRESS_TIME,
        KEY_SPACE_LONG_PRESS_ACTION,
        KEY_AUTO_SPACE_PERIOD,
        KEY_KEYBOARD_SKIN,
        KEY_AUTO_CAPITALIZE_ENGLISH,
        KEY_ENTER_LONG_PRESS_ACTION,
        KEY_GESTURE_ANGLES,
        KEY_GESTURE_ANGLE_PRESET,
        KEY_GESTURE_THRESHOLD,
        KEY_HOTSTRING_ENABLED,
        KEY_HOTSTRING_SORT_ORDER,
        KEY_HAPTIC_STRENGTH,
        KEY_SOUND_VOLUME,
        KEY_SOUND_TYPE,
        KEY_WORD_SUGGESTION_ENABLED,
    ) + UserCharKey.values().map { it.prefKey }.toSet()

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getKeyPreviewEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_KEY_PREVIEW, true)

    fun setKeyPreviewEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_KEY_PREVIEW, enabled).apply()
    }

    fun getHangulInputMode(context: Context): HangulInputMode =
        HangulInputMode.fromString(prefs(context).getString(KEY_HANGUL_INPUT_MODE, null))

    fun getKeypadHeight(context: Context): KeypadHeight =
        KeypadHeight.fromString(prefs(context).getString(KEY_KEYPAD_HEIGHT, null))

    fun getLongPressTime(context: Context): LongPressTime =
        LongPressTime.fromString(prefs(context).getString(KEY_LONG_PRESS_TIME, null))

    fun getOneHandMode(context: Context): OneHandMode =
        OneHandMode.fromString(prefs(context).getString(KEY_ONE_HAND_MODE, null))

    fun getSpaceLongPressAction(context: Context): SpaceLongPressAction =
        SpaceLongPressAction.fromString(prefs(context).getString(KEY_SPACE_LONG_PRESS_ACTION, null))

    fun getKeyboardSkin(context: Context): KeyboardSkin =
        KeyboardSkin.fromString(prefs(context).getString(KEY_KEYBOARD_SKIN, null))

    fun getAutoSpacePeriod(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_SPACE_PERIOD, false)

    fun setAutoSpacePeriod(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUTO_SPACE_PERIOD, enabled).apply()
    }

    fun getAutoCapitalizeEnglish(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_CAPITALIZE_ENGLISH, true)

    fun setAutoCapitalizeEnglish(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUTO_CAPITALIZE_ENGLISH, enabled).apply()
    }

    fun getEnterLongPressAction(context: Context): EnterLongPressAction =
        EnterLongPressAction.fromString(prefs(context).getString(KEY_ENTER_LONG_PRESS_ACTION, null))

    fun getGestureAngles(context: Context): GestureAngles =
        GestureAngles.fromString(prefs(context).getString(KEY_GESTURE_ANGLES, null))

    fun setGestureAngles(context: Context, angles: GestureAngles) {
        prefs(context).edit().putString(KEY_GESTURE_ANGLES, angles.toPrefsString()).apply()
    }

    fun getGestureAnglePreset(context: Context): GestureAnglePreset =
        GestureAnglePreset.fromString(prefs(context).getString(KEY_GESTURE_ANGLE_PRESET, null))

    fun setGestureAnglePreset(context: Context, preset: GestureAnglePreset) {
        prefs(context).edit().putString(KEY_GESTURE_ANGLE_PRESET, preset.name).apply()
    }

    fun getGestureThreshold(context: Context): Int =
        prefs(context).getString(KEY_GESTURE_THRESHOLD, null)?.toIntOrNull() ?: 30

    fun setGestureThreshold(context: Context, value: Int) {
        prefs(context).edit().putString(KEY_GESTURE_THRESHOLD, value.toString()).apply()
    }

    fun getHotstringEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HOTSTRING_ENABLED, false)

    fun setHotstringEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_HOTSTRING_ENABLED, enabled).apply()
    }

    fun getHotstringSortOrder(context: Context): HotstringSortOrder =
        HotstringSortOrder.fromString(prefs(context).getString(KEY_HOTSTRING_SORT_ORDER, null))

    fun setHotstringSortOrder(context: Context, order: HotstringSortOrder) {
        prefs(context).edit().putString(KEY_HOTSTRING_SORT_ORDER, order.name).apply()
    }

    fun getHapticStrength(context: Context): HapticStrength =
        HapticStrength.fromString(prefs(context).getString(KEY_HAPTIC_STRENGTH, null))

    fun getSoundVolume(context: Context): SoundVolume =
        SoundVolume.fromString(prefs(context).getString(KEY_SOUND_VOLUME, null))

    fun getSoundType(context: Context): SoundType =
        SoundType.fromString(prefs(context).getString(KEY_SOUND_TYPE, null))

    fun getWordSuggestionEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_WORD_SUGGESTION_ENABLED, false)

    fun setWordSuggestionEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_WORD_SUGGESTION_ENABLED, enabled).apply()
    }

    fun save(context: Context, key: String, value: String) {
        prefs(context).edit().putString(key, value).apply()
    }
}
