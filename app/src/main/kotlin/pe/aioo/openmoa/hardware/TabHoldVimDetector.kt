package pe.aioo.openmoa.hardware

class TabHoldVimDetector {
    var enabled = false
    private var tabHeld = false
    private var tabConsumedAsModifier = false
    private var pendingD = false
    private var visualMode = false

    fun onKeyDown(keyCode: Int, isShift: Boolean, isRepeat: Boolean): VimAction {
        if (!enabled) return VimAction.PassThrough

        if (keyCode == KEYCODE_TAB) {
            if (isRepeat) return VimAction.Consume
            tabHeld = true
            return VimAction.Consume
        }

        if (!tabHeld) return VimAction.PassThrough

        tabConsumedAsModifier = true

        if (pendingD && keyCode != KEYCODE_D) {
            pendingD = false
        }

        val extend = visualMode

        return when {
            keyCode == KEYCODE_H -> VimAction.MoveCursor(CursorDirection.LEFT, extend)
            keyCode == KEYCODE_J -> VimAction.MoveCursor(CursorDirection.DOWN, extend)
            keyCode == KEYCODE_K -> VimAction.MoveCursor(CursorDirection.UP, extend)
            keyCode == KEYCODE_L -> VimAction.MoveCursor(CursorDirection.RIGHT, extend)
            keyCode == KEYCODE_W -> VimAction.MoveWord(forward = true, extend = extend)
            keyCode == KEYCODE_B -> VimAction.MoveWord(forward = false, extend = extend)
            keyCode == KEYCODE_E -> VimAction.MoveWord(forward = true, extend = extend)
            keyCode == KEYCODE_ZERO -> VimAction.LineHome
            isShift && keyCode == KEYCODE_4 -> VimAction.LineEnd
            keyCode == KEYCODE_X -> {
                if (visualMode) {
                    visualMode = false
                    VimAction.DeleteSelection
                } else {
                    VimAction.DeleteChar
                }
            }
            keyCode == KEYCODE_D -> {
                if (visualMode) {
                    visualMode = false
                    VimAction.DeleteSelection
                } else if (pendingD) {
                    pendingD = false
                    VimAction.DeleteLine
                } else {
                    pendingD = true
                    VimAction.Consume
                }
            }
            keyCode == KEYCODE_V -> {
                if (visualMode) {
                    visualMode = false
                    VimAction.CollapseSelection
                } else {
                    visualMode = true
                    VimAction.VisualEnter
                }
            }
            keyCode == KEYCODE_Y -> {
                visualMode = false
                VimAction.Yank
            }
            keyCode == KEYCODE_ESCAPE -> {
                if (visualMode) {
                    visualMode = false
                    VimAction.CollapseSelection
                } else {
                    VimAction.Consume
                }
            }
            else -> VimAction.PassThrough
        }
    }

    fun onKeyUp(keyCode: Int): VimAction {
        if (!enabled) return VimAction.PassThrough
        if (keyCode != KEYCODE_TAB) return VimAction.PassThrough

        val wasModifier = tabConsumedAsModifier
        tabHeld = false
        tabConsumedAsModifier = false
        pendingD = false
        // visualMode는 Tab 해제 후에도 유지 — Tab+y 또는 Tab+v로 해제

        return if (wasModifier) VimAction.Consume else VimAction.InjectTab
    }

    fun reset() {
        tabHeld = false
        tabConsumedAsModifier = false
        pendingD = false
        visualMode = false
    }

    companion object {
        const val KEYCODE_TAB = 61
        const val KEYCODE_H = 36
        const val KEYCODE_J = 38
        const val KEYCODE_K = 39
        const val KEYCODE_L = 40
        const val KEYCODE_W = 51
        const val KEYCODE_B = 30
        const val KEYCODE_E = 33
        const val KEYCODE_X = 52
        const val KEYCODE_D = 32
        const val KEYCODE_V = 50
        const val KEYCODE_Y = 53
        const val KEYCODE_ZERO = 7
        const val KEYCODE_4 = 11
        const val KEYCODE_ESCAPE = 111
    }
}
