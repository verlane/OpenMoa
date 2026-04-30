package pe.aioo.openmoa.hardware

import android.view.KeyEvent

class CapsLockRemapper {
    var enabled = false
        set(value) { field = value; if (!value) reset() }
    private var capsHeld = false
    private var lastRewrittenKeyCode: Int? = null

    sealed interface Result {
        data object Consumed : Result
        data class RewriteAsCtrl(val keyCode: Int, val metaState: Int) : Result
        data object Pass : Result
    }

    fun onKeyDown(keyCode: Int, metaState: Int): Result {
        if (!enabled) return Result.Pass
        if (keyCode == KeyEvent.KEYCODE_CAPS_LOCK) {
            capsHeld = true
            return Result.Consumed
        }
        if (capsHeld) {
            lastRewrittenKeyCode = keyCode
            return Result.RewriteAsCtrl(keyCode, metaState or KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_LEFT_ON)
        }
        return Result.Pass
    }

    fun onKeyUp(keyCode: Int): Result {
        if (!enabled) return Result.Pass
        if (keyCode == KeyEvent.KEYCODE_CAPS_LOCK) {
            capsHeld = false
            return Result.Consumed
        }
        if (lastRewrittenKeyCode == keyCode) {
            lastRewrittenKeyCode = null
            return Result.Consumed
        }
        return Result.Pass
    }

    fun reset() {
        capsHeld = false
        lastRewrittenKeyCode = null
    }
}
