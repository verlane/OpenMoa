package pe.aioo.openmoa.config

import android.content.Context
import pe.aioo.openmoa.settings.SettingsPreferences

class Config(private val context: Context) {
    val longPressRepeatTime: Long = 50L
    val longPressThresholdTime: Long
        get() = SettingsPreferences.getLongPressTime(context).millis
    val gestureThreshold: Float
        get() {
            val dp = SettingsPreferences.getGestureThreshold(context)
            return dp * context.resources.displayMetrics.density
        }
    val hapticStrength: HapticStrength
        get() = SettingsPreferences.getHapticStrength(context)
    val soundVolume: SoundVolume
        get() = SettingsPreferences.getSoundVolume(context)
    val soundType: SoundType
        get() = SettingsPreferences.getSoundType(context)
    val maxSuggestionCount: Int = 10
    val keyPreviewEnabled: Boolean
        get() = SettingsPreferences.getKeyPreviewEnabled(context)
    val autoCapitalizeEnglish: Boolean
        get() = SettingsPreferences.getAutoCapitalizeEnglish(context)
    val gestureAngles: GestureAngles
        get() = SettingsPreferences.getGestureAngles(context)
}
