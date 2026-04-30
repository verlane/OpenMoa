package pe.aioo.openmoa.hardware

class HardwareKeyShortcutDetector {
    private var rAltAlone = false

    fun onKeyDown(keyCode: Int, isShift: Boolean, isCtrl: Boolean, isAlt: Boolean): ShortcutAction {
        if (keyCode == KEYCODE_SPACE && isShift && !isCtrl && !isAlt) {
            rAltAlone = false
            return ShortcutAction.ConsumeToggleLanguage
        }
        if (keyCode in IMMEDIATE_LANGUAGE_TOGGLE_KEYS) {
            rAltAlone = false
            return ShortcutAction.ConsumeToggleLanguage
        }
        if (keyCode == KEYCODE_ALT_RIGHT) {
            rAltAlone = true
            return ShortcutAction.Pass
        }
        rAltAlone = false
        return ShortcutAction.Pass
    }

    fun reset() {
        rAltAlone = false
    }

    fun onKeyUp(keyCode: Int): ShortcutAction {
        if (keyCode == KEYCODE_ALT_RIGHT) {
            val shouldToggle = rAltAlone
            rAltAlone = false
            return if (shouldToggle) ShortcutAction.ConsumeToggleLanguage else ShortcutAction.Pass
        }
        return ShortcutAction.Pass
    }

    companion object {
        const val KEYCODE_SPACE = 62
        const val KEYCODE_ALT_RIGHT = 58
        const val KEYCODE_LANGUAGE_SWITCH = 204
        const val KEYCODE_KANA = 218
        const val KEYCODE_HENKAN = 214

        private val IMMEDIATE_LANGUAGE_TOGGLE_KEYS = setOf(
            KEYCODE_LANGUAGE_SWITCH,
            KEYCODE_KANA,
            KEYCODE_HENKAN,
        )
    }
}
