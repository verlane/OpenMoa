package pe.aioo.openmoa.hardware

class TabHoldVimDetector {
    var enabled = false
        set(value) { field = value; if (!value) reset() }
    private var tabHeld = false
    private var tabConsumedAsModifier = false
    private var visualMode = false
    private var visualEnteredThisTab = false

    val isTabHeld: Boolean get() = tabHeld
    val isVisualMode: Boolean get() = visualMode
    val isVisualEnteredThisTab: Boolean get() = visualEnteredThisTab

    fun onKeyDown(keyCode: Int, isShift: Boolean, isRepeat: Boolean): VimAction {
        if (!enabled) return VimAction.PassThrough

        if (keyCode == KEYCODE_TAB) {
            if (isRepeat) return VimAction.Consume
            tabHeld = true
            return VimAction.Consume
        }

        if (!tabHeld) return VimAction.PassThrough

        if (keyCode in MODIFIER_KEYCODES) {
            tabConsumedAsModifier = true
            return VimAction.Consume
        }

        tabConsumedAsModifier = true

        val extend = visualMode

        return when {
            keyCode == KEYCODE_H -> VimAction.MoveCursor(CursorDirection.LEFT, extend)
            keyCode == KEYCODE_J -> VimAction.MoveCursor(CursorDirection.DOWN, extend)
            keyCode == KEYCODE_K -> VimAction.MoveCursor(CursorDirection.UP, extend)
            keyCode == KEYCODE_L -> VimAction.MoveCursor(CursorDirection.RIGHT, extend)
            keyCode == KEYCODE_W -> VimAction.MoveWord(forward = true, extend = extend)
            keyCode == KEYCODE_B -> VimAction.MoveWord(forward = false, extend = extend)
            keyCode == KEYCODE_E -> VimAction.LineEnd(extend)
            keyCode == KEYCODE_R -> VimAction.LineHome(extend)
            keyCode == KEYCODE_F && !isShift -> VimAction.PageScroll(down = true, extend = extend)
            keyCode == KEYCODE_F && isShift -> VimAction.PageScroll(down = false, extend = extend)
            keyCode == KEYCODE_G && !isShift -> VimAction.DocumentEdge(end = false, extend = extend)
            keyCode == KEYCODE_G && isShift -> VimAction.DocumentEdge(end = true, extend = extend)
            keyCode == KEYCODE_ZERO -> VimAction.LineHome(extend)
            isShift && keyCode == KEYCODE_4 -> VimAction.LineEnd(extend)
            isShift && keyCode == KEYCODE_6 -> VimAction.LineHome(extend)
            keyCode == KEYCODE_X -> {
                if (visualMode) { visualMode = false; VimAction.DeleteSelection }
                else VimAction.DeleteChar
            }
            keyCode == KEYCODE_D -> {
                if (visualMode) {
                    visualMode = false
                    VimAction.DeleteSelection
                } else {
                    VimAction.DeleteLine
                }
            }
            keyCode == KEYCODE_V -> {
                if (visualMode) {
                    visualMode = false
                    visualEnteredThisTab = false
                    VimAction.Consume
                } else {
                    visualMode = true
                    visualEnteredThisTab = true
                    VimAction.VisualEnter
                }
            }
            keyCode == KEYCODE_Y -> {
                if (visualMode) {
                    visualMode = false
                    VimAction.YankSelection
                } else {
                    VimAction.YankLine
                }
            }
            keyCode == KEYCODE_P -> VimAction.Paste
            keyCode == KEYCODE_U && !isShift -> VimAction.Undo
            keyCode == KEYCODE_U && isShift -> VimAction.Redo
            keyCode == KEYCODE_N -> VimAction.DeleteWordBack
            keyCode == KEYCODE_COMMA -> VimAction.DeleteBack
            keyCode == KEYCODE_PERIOD -> VimAction.DeleteChar
            keyCode == KEYCODE_C -> VimAction.DeleteSelection
            keyCode == KEYCODE_I -> VimAction.NewLineAbove
            keyCode == KEYCODE_O && !isShift -> VimAction.NewLineBelow
            keyCode == KEYCODE_O && isShift -> VimAction.NewLineAbove
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
        if (tabHeld && keyCode in MODIFIER_KEYCODES) return VimAction.Consume
        if (keyCode != KEYCODE_TAB) return VimAction.PassThrough

        val wasModifier = tabConsumedAsModifier
        tabHeld = false
        tabConsumedAsModifier = false
        visualMode = false
        visualEnteredThisTab = false

        return if (wasModifier) VimAction.Consume else VimAction.InjectTab
    }

    fun reset() {
        tabHeld = false
        tabConsumedAsModifier = false
        visualMode = false
        visualEnteredThisTab = false
    }

    companion object {
        const val KEYCODE_TAB = 61
        const val KEYCODE_B = 30
        const val KEYCODE_D = 32
        const val KEYCODE_E = 33
        const val KEYCODE_F = 34
        const val KEYCODE_G = 35
        const val KEYCODE_H = 36
        const val KEYCODE_C = 31
        const val KEYCODE_I = 37
        const val KEYCODE_J = 38
        const val KEYCODE_K = 39
        const val KEYCODE_L = 40
        const val KEYCODE_O = 43
        const val KEYCODE_P = 44
        const val KEYCODE_U = 49
        const val KEYCODE_N = 42
        const val KEYCODE_COMMA = 55
        const val KEYCODE_PERIOD = 56
        const val KEYCODE_R = 46
        const val KEYCODE_V = 50
        const val KEYCODE_W = 51
        const val KEYCODE_X = 52
        const val KEYCODE_Y = 53
        const val KEYCODE_ZERO = 7
        const val KEYCODE_4 = 11
        const val KEYCODE_6 = 13
        const val KEYCODE_ESCAPE = 111
        private const val KEYCODE_SHIFT_LEFT = 59
        private const val KEYCODE_SHIFT_RIGHT = 60
        private const val KEYCODE_CTRL_LEFT = 113
        private const val KEYCODE_CTRL_RIGHT = 114
        private const val KEYCODE_ALT_LEFT = 57
        private const val KEYCODE_ALT_RIGHT = 58
        private val MODIFIER_KEYCODES = setOf(
            KEYCODE_SHIFT_LEFT, KEYCODE_SHIFT_RIGHT,
            KEYCODE_CTRL_LEFT, KEYCODE_CTRL_RIGHT,
            KEYCODE_ALT_LEFT, KEYCODE_ALT_RIGHT,
        )
    }
}
