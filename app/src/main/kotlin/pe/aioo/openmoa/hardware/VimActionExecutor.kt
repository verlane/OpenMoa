package pe.aioo.openmoa.hardware

import android.os.SystemClock
import android.view.KeyEvent
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection

object VimActionExecutor {

    fun execute(action: VimAction, ic: InputConnection) {
        when (action) {
            is VimAction.MoveCursor -> moveCursor(action.direction, action.extend, ic)
            is VimAction.MoveWord -> moveWord(action.forward, action.extend, ic)
            VimAction.LineHome -> sendKey(ic, KeyEvent.KEYCODE_MOVE_HOME)
            VimAction.LineEnd -> sendKey(ic, KeyEvent.KEYCODE_MOVE_END)
            VimAction.DeleteChar -> sendKey(ic, KeyEvent.KEYCODE_FORWARD_DEL)
            VimAction.DeleteLine -> deleteLine(ic)
            VimAction.VisualEnter -> visualEnter(ic)
            VimAction.CollapseSelection -> collapseSelection(ic)
            VimAction.DeleteSelection -> ic.commitText("", 1)
            VimAction.Yank -> ic.performContextMenuAction(android.R.id.copy)
            VimAction.InjectTab -> ic.commitText("\t", 1)
            VimAction.Consume, VimAction.PassThrough -> Unit
        }
    }

    private fun moveCursor(dir: CursorDirection, extend: Boolean, ic: InputConnection) {
        val keyCode = when (dir) {
            CursorDirection.LEFT -> KeyEvent.KEYCODE_DPAD_LEFT
            CursorDirection.RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT
            CursorDirection.UP -> KeyEvent.KEYCODE_DPAD_UP
            CursorDirection.DOWN -> KeyEvent.KEYCODE_DPAD_DOWN
        }
        val meta = if (extend) KeyEvent.META_SHIFT_ON else 0
        sendKey(ic, keyCode, meta)
    }

    private fun moveWord(forward: Boolean, extend: Boolean, ic: InputConnection) {
        val keyCode = if (forward) KeyEvent.KEYCODE_DPAD_RIGHT else KeyEvent.KEYCODE_DPAD_LEFT
        val ctrlMeta = KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_LEFT_ON
        val meta = ctrlMeta or if (extend) KeyEvent.META_SHIFT_ON else 0
        sendKey(ic, keyCode, meta)
    }

    private fun deleteLine(ic: InputConnection) {
        sendKey(ic, KeyEvent.KEYCODE_MOVE_HOME)
        sendKey(ic, KeyEvent.KEYCODE_MOVE_END, KeyEvent.META_SHIFT_ON)
        ic.commitText("", 1)
        sendKey(ic, KeyEvent.KEYCODE_FORWARD_DEL)
    }

    private fun visualEnter(ic: InputConnection) {
        val extracted = ic.getExtractedText(ExtractedTextRequest(), 0) ?: return
        val cursor = extracted.startOffset + extracted.selectionStart
        val textLen = extracted.startOffset + extracted.text.length
        if (cursor < textLen) {
            ic.setSelection(cursor, cursor + 1)
        }
    }

    private fun collapseSelection(ic: InputConnection) {
        val extracted = ic.getExtractedText(ExtractedTextRequest(), 0)
        if (extracted != null) {
            val pos = extracted.startOffset + minOf(extracted.selectionStart, extracted.selectionEnd)
            ic.setSelection(pos, pos)
        } else {
            sendKey(ic, KeyEvent.KEYCODE_DPAD_LEFT)
        }
    }

    private fun sendKey(ic: InputConnection, keyCode: Int, meta: Int = 0) {
        val now = SystemClock.uptimeMillis()
        ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, meta))
        ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, meta))
    }
}
