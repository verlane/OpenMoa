package pe.aioo.openmoa.view.keytouchlistener

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import pe.aioo.openmoa.config.SpaceLongPressAction
import pe.aioo.openmoa.settings.SettingsPreferences
import pe.aioo.openmoa.view.message.SpecialKey
import pe.aioo.openmoa.view.message.SpecialKeyMessage
import pe.aioo.openmoa.view.message.StringKeyMessage

class SpaceKeyTouchListener(private val context: Context) : BaseKeyTouchListener(context) {

    private var longPressTriggered = false
    private var swipeTriggered = false
    private var defaultBackground: Drawable? = null
    private var longPressRunnable: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())
    private var swipeStartX = 0f
    private var swipeStartY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                longPressTriggered = false
                swipeTriggered = false
                swipeStartX = motionEvent.rawX
                swipeStartY = motionEvent.rawY
                defaultBackground = view.background
                val action = SettingsPreferences.getSpaceLongPressAction(context)
                val runnable = Runnable {
                    when (action) {
                        SpaceLongPressAction.IME_PICKER -> {
                            longPressTriggered = true
                            sendKeyMessage(SpecialKeyMessage(SpecialKey.SHOW_IME_PICKER))
                        }
                        SpaceLongPressAction.SWITCH_LANGUAGE -> {
                            longPressTriggered = true
                            sendKeyMessage(SpecialKeyMessage(SpecialKey.LANGUAGE))
                        }
                        SpaceLongPressAction.ARROW -> {
                            longPressTriggered = true
                            sendKeyMessage(SpecialKeyMessage(SpecialKey.ARROW))
                        }
                        SpaceLongPressAction.NONE -> Unit
                    }
                }
                longPressRunnable = runnable
                handler.postDelayed(runnable, config.longPressThresholdTime)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!longPressTriggered && !swipeTriggered) {
                    val dy = swipeStartY - motionEvent.rawY
                    val dx = Math.abs(motionEvent.rawX - swipeStartX)
                    if (dy > config.gestureThreshold && dy > dx) {
                        swipeTriggered = true
                        longPressRunnable?.let { handler.removeCallbacks(it) }
                        sendKeyMessage(SpecialKeyMessage(SpecialKey.CLIPBOARD_OPEN))
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                longPressRunnable?.let { handler.removeCallbacks(it) }
                if (longPressTriggered || swipeTriggered) {
                    longPressTriggered = false
                    swipeTriggered = false
                    view.background = defaultBackground
                    return true
                }
                sendKeyMessage(StringKeyMessage(" "))
            }
            MotionEvent.ACTION_CANCEL -> {
                longPressRunnable?.let { handler.removeCallbacks(it) }
                longPressTriggered = false
                swipeTriggered = false
            }
        }
        return super.onTouch(view, motionEvent)
    }
}
