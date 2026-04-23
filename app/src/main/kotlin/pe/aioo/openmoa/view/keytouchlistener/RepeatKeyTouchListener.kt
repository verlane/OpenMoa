package pe.aioo.openmoa.view.keytouchlistener

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import pe.aioo.openmoa.view.message.BaseKeyMessage

class RepeatKeyTouchListener(
    context: Context,
    private val keyProvider: () -> BaseKeyMessage?,
) : BaseKeyTouchListener(context) {

    constructor(context: Context, key: BaseKeyMessage) : this(context, { key })

    private val handler = Handler(Looper.getMainLooper())
    private var elapsed = 0L
    private var repeatRunnable: Runnable? = null

    private fun startRepeat() {
        endRepeat()
        elapsed = 0L
        keyProvider()?.let { sendKeyMessage(it) }
        repeatRunnable = object : Runnable {
            override fun run() {
                elapsed += config.longPressRepeatTime
                if (elapsed >= config.longPressThresholdTime) {
                    keyProvider()?.let { sendKeyMessage(it) }
                }
                handler.postDelayed(this, config.longPressRepeatTime)
            }
        }.also { handler.postDelayed(it, config.longPressRepeatTime) }
    }

    fun endRepeat() {
        repeatRunnable?.let { handler.removeCallbacks(it) }
        repeatRunnable = null
        elapsed = 0L
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> startRepeat()
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> endRepeat()
        }
        return super.onTouch(view, motionEvent)
    }

}
