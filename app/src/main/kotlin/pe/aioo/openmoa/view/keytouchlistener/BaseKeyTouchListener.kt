package pe.aioo.openmoa.view.keytouchlistener

import android.content.Context
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pe.aioo.openmoa.OpenMoaIME
import pe.aioo.openmoa.config.Config
import pe.aioo.openmoa.config.HapticStrength
import pe.aioo.openmoa.config.SoundVolume
import pe.aioo.openmoa.config.SoundType
import pe.aioo.openmoa.settings.SettingsPreferences
import pe.aioo.openmoa.view.feedback.KeyFeedbackPlayer
import pe.aioo.openmoa.view.message.BaseKeyMessage
import pe.aioo.openmoa.view.message.SpecialKeyMessage
import pe.aioo.openmoa.view.message.StringKeyMessage
import pe.aioo.openmoa.view.skin.SkinApplier

open class BaseKeyTouchListener(context: Context) : OnTouchListener, KoinComponent {

    protected val config: Config by inject()
    private val feedbackPlayer: KeyFeedbackPlayer by inject()

    private val broadcastManager = LocalBroadcastManager.getInstance(context)
    private val skin = SettingsPreferences.getKeyboardSkin(context)
    private val backgrounds = listOf(
        SkinApplier.buildKeyDrawable(context, skin, pressed = true),
        SkinApplier.buildKeyDrawable(context, skin, pressed = false),
    )

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                view.background = backgrounds[0]
                playFeedback()
            }
            MotionEvent.ACTION_CANCEL -> {
                view.background = backgrounds[1]
            }
            MotionEvent.ACTION_UP -> {
                view.background = backgrounds[1]
                view.performClick()
            }
        }
        return true
    }

    protected fun playFeedback() {
        val strength = config.hapticStrength
        if (strength != HapticStrength.OFF) {
            feedbackPlayer.playHaptic(strength.durationMs, strength.amplitude)
        }
        val volume = config.soundVolume
        if (volume != SoundVolume.OFF) {
            feedbackPlayer.playSound(config.soundType.effectId, volume.volume)
        }
    }

    protected fun sendKeyMessage(keyMessage: BaseKeyMessage) {
        broadcastManager.sendBroadcast(
            Intent(OpenMoaIME.INTENT_ACTION).apply {
                putExtra(OpenMoaIME.EXTRA_NAME, when (keyMessage) {
                    is StringKeyMessage -> keyMessage.key
                    is SpecialKeyMessage -> keyMessage.key
                    else -> ""
                })
            }
        )
    }

}