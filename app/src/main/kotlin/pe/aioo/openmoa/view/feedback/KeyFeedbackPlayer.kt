package pe.aioo.openmoa.view.feedback

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class KeyFeedbackPlayer(context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    init {
        audioManager.loadSoundEffects()
    }

    fun playSound(effectId: Int, volume: Float) {
        audioManager.playSoundEffect(effectId, volume)
    }

    fun playHaptic(durationMs: Long, amplitude: Int) {
        if (durationMs <= 0 || amplitude <= 0) return
        val effect = if (vibrator.hasAmplitudeControl()) {
            VibrationEffect.createOneShot(durationMs, amplitude)
        } else {
            VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        vibrator.vibrate(effect)
    }

    fun hasAmplitudeControl(): Boolean = vibrator.hasAmplitudeControl()

    fun release() {
        vibrator.cancel()
        audioManager.unloadSoundEffects()
    }
}
