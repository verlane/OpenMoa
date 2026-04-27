package pe.aioo.openmoa.view.keytouchlistener

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import pe.aioo.openmoa.view.message.SpecialKey
import pe.aioo.openmoa.view.message.SpecialKeyMessage

class LanguageKeyTouchListener(context: Context) : BaseKeyTouchListener(context) {

    private var longPressTriggered = false
    private val handler = Handler(Looper.getMainLooper())
    private var longPressRunnable: Runnable? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                longPressTriggered = false
                longPressRunnable = Runnable {
                    longPressTriggered = true
                    longPressRunnable = null
                    sendKeyMessage(SpecialKeyMessage(SpecialKey.OPEN_SETTINGS))
                }.also { handler.postDelayed(it, config.longPressThresholdTime) }
            }
            MotionEvent.ACTION_UP -> {
                cancelLongPress()
                if (!longPressTriggered) {
                    sendKeyMessage(SpecialKeyMessage(SpecialKey.LANGUAGE))
                }
                longPressTriggered = false
            }
            MotionEvent.ACTION_CANCEL -> {
                cancelLongPress()
                longPressTriggered = false
            }
        }
        return super.onTouch(view, motionEvent)
    }

    fun cancel() {
        cancelLongPress()
        longPressTriggered = false
    }

    private fun cancelLongPress() {
        longPressRunnable?.let { handler.removeCallbacks(it) }
        longPressRunnable = null
    }


}
