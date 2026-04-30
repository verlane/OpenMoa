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
            is VimAction.LineHome -> {
                if (action.extend) sendKeyWithShift(ic, KeyEvent.KEYCODE_MOVE_HOME)
                else sendKey(ic, KeyEvent.KEYCODE_MOVE_HOME)
            }
            is VimAction.LineEnd -> {
                if (action.extend) sendKeyWithShift(ic, KeyEvent.KEYCODE_MOVE_END)
                else sendKey(ic, KeyEvent.KEYCODE_MOVE_END)
            }
            is VimAction.PageScroll -> {
                val kc = if (action.down) KeyEvent.KEYCODE_PAGE_DOWN else KeyEvent.KEYCODE_PAGE_UP
                if (action.extend) sendKeyWithShift(ic, kc) else sendKey(ic, kc)
            }
            is VimAction.DocumentEdge -> documentEdge(action.end, action.extend, ic)
            VimAction.DeleteWordBack -> sendKey(ic, KeyEvent.KEYCODE_DEL, ctrlMeta())
            VimAction.DeleteBack -> sendKey(ic, KeyEvent.KEYCODE_DEL)
            VimAction.DeleteChar -> sendKey(ic, KeyEvent.KEYCODE_FORWARD_DEL)
            VimAction.DeleteLine -> deleteLine(ic)
            VimAction.DeleteSelection -> sendKey(ic, KeyEvent.KEYCODE_X, ctrlMeta())
            VimAction.VisualEnter -> Unit
            VimAction.CollapseSelection -> collapseSelection(ic)
            VimAction.Yank -> ic.performContextMenuAction(android.R.id.copy)
            VimAction.YankLine -> yankLine(ic)
            VimAction.YankSelection -> sendKey(ic, KeyEvent.KEYCODE_C, ctrlMeta())
            VimAction.Paste -> sendKey(ic, KeyEvent.KEYCODE_V, ctrlMeta())
            VimAction.NewLineBelow -> newLineBelow(ic)
            VimAction.NewLineAbove -> newLineAbove(ic)
            VimAction.Undo -> sendKey(ic, KeyEvent.KEYCODE_Z, ctrlMeta())
            VimAction.Redo -> sendKey(ic, KeyEvent.KEYCODE_Y, ctrlMeta())
            VimAction.InjectTab -> ic.commitText("\t", 1)
            VimAction.Consume, VimAction.PassThrough -> Unit
        }
    }

    private fun moveCursor(dir: CursorDirection, extend: Boolean, ic: InputConnection) {
        val kc = when (dir) {
            CursorDirection.LEFT -> KeyEvent.KEYCODE_DPAD_LEFT
            CursorDirection.RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT
            CursorDirection.UP -> KeyEvent.KEYCODE_DPAD_UP
            CursorDirection.DOWN -> KeyEvent.KEYCODE_DPAD_DOWN
        }
        if (extend) sendKeyWithShift(ic, kc) else sendKey(ic, kc)
    }

    private fun moveWord(forward: Boolean, extend: Boolean, ic: InputConnection) {
        val kc = if (forward) KeyEvent.KEYCODE_DPAD_RIGHT else KeyEvent.KEYCODE_DPAD_LEFT
        if (extend) sendKeyWithShift(ic, kc, ctrlMeta()) else sendKey(ic, kc, ctrlMeta())
    }

    private fun documentEdge(end: Boolean, extend: Boolean, ic: InputConnection) {
        val kc = if (end) KeyEvent.KEYCODE_MOVE_END else KeyEvent.KEYCODE_MOVE_HOME
        if (extend) sendKeyWithShift(ic, kc, ctrlMeta()) else sendKey(ic, kc, ctrlMeta())
    }

    private fun deleteLine(ic: InputConnection) {
        sendKey(ic, KeyEvent.KEYCODE_MOVE_HOME)
        sendKeyWithShift(ic, KeyEvent.KEYCODE_MOVE_END)
        sendKey(ic, KeyEvent.KEYCODE_X, ctrlMeta())
        sendKey(ic, KeyEvent.KEYCODE_FORWARD_DEL)
    }

    private fun yankLine(ic: InputConnection) {
        sendKey(ic, KeyEvent.KEYCODE_MOVE_HOME)
        sendKeyWithShift(ic, KeyEvent.KEYCODE_MOVE_END)
        sendKey(ic, KeyEvent.KEYCODE_C, ctrlMeta())
        sendKey(ic, KeyEvent.KEYCODE_DPAD_LEFT)
    }

    private fun newLineBelow(ic: InputConnection) {
        sendKey(ic, KeyEvent.KEYCODE_MOVE_END)
        sendKey(ic, KeyEvent.KEYCODE_ENTER)
    }

    private fun newLineAbove(ic: InputConnection) {
        sendKey(ic, KeyEvent.KEYCODE_MOVE_HOME)
        sendKey(ic, KeyEvent.KEYCODE_ENTER)
        sendKey(ic, KeyEvent.KEYCODE_DPAD_UP)
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

    private fun ctrlMeta() = KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_LEFT_ON

    private fun sendKeyWithShift(ic: InputConnection, keyCode: Int, extraMeta: Int = 0) {
        val now = SystemClock.uptimeMillis()
        val shiftMeta = KeyEvent.META_SHIFT_ON or KeyEvent.META_SHIFT_LEFT_ON
        val meta = shiftMeta or extraMeta
        ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT, 0, shiftMeta))
        ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, meta))
        ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, meta))
        ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT, 0, shiftMeta))
    }

    private fun sendKey(ic: InputConnection, keyCode: Int, meta: Int = 0) {
        val now = SystemClock.uptimeMillis()
        ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, meta))
        ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, meta))
    }
}
