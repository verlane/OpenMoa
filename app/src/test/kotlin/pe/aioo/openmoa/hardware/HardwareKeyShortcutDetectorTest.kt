package pe.aioo.openmoa.hardware

import org.junit.Assert.assertEquals
import org.junit.Test

class HardwareKeyShortcutDetectorTest {

    private fun newDetector(
        languageSwitchEnabled: Boolean = true,
        shiftSpaceEnabled: Boolean = true,
        rAltEnabled: Boolean = true,
    ) = HardwareKeyShortcutDetector(
        isLanguageSwitchEnabled = { languageSwitchEnabled },
        isShiftSpaceEnabled = { shiftSpaceEnabled },
        isRAltEnabled = { rAltEnabled },
    )

    @Test
    fun `Shift Space 단축키는 토글 액션`() {
        val detector = newDetector()
        val action = detector.onKeyDown(
            keyCode = HardwareKeyShortcutDetector.KEYCODE_SPACE,
            isShift = true,
            isCtrl = false,
            isAlt = false,
        )
        assertEquals(ShortcutAction.ConsumeToggleLanguage, action)
    }

    @Test
    fun `Ctrl Shift Space는 토글 안 함`() {
        val detector = newDetector()
        val action = detector.onKeyDown(
            keyCode = HardwareKeyShortcutDetector.KEYCODE_SPACE,
            isShift = true,
            isCtrl = true,
            isAlt = false,
        )
        assertEquals(ShortcutAction.Pass, action)
    }

    @Test
    fun `그냥 Space는 토글 안 함`() {
        val detector = newDetector()
        val action = detector.onKeyDown(
            keyCode = HardwareKeyShortcutDetector.KEYCODE_SPACE,
            isShift = false,
            isCtrl = false,
            isAlt = false,
        )
        assertEquals(ShortcutAction.Pass, action)
    }

    @Test
    fun `LANGUAGE_SWITCH 키는 즉시 토글`() {
        val detector = newDetector()
        val action = detector.onKeyDown(
            keyCode = HardwareKeyShortcutDetector.KEYCODE_LANGUAGE_SWITCH,
            isShift = false,
            isCtrl = false,
            isAlt = false,
        )
        assertEquals(ShortcutAction.ConsumeToggleLanguage, action)
    }

    @Test
    fun `KANA 키는 즉시 토글`() {
        val detector = newDetector()
        val action = detector.onKeyDown(
            keyCode = HardwareKeyShortcutDetector.KEYCODE_KANA,
            isShift = false,
            isCtrl = false,
            isAlt = false,
        )
        assertEquals(ShortcutAction.ConsumeToggleLanguage, action)
    }

    @Test
    fun `RAlt 단독 - Down 시점은 Pass, Up 시점에 토글`() {
        val detector = newDetector()
        val downAction = detector.onKeyDown(
            keyCode = HardwareKeyShortcutDetector.KEYCODE_ALT_RIGHT,
            isShift = false,
            isCtrl = false,
            isAlt = true,
        )
        assertEquals(ShortcutAction.Pass, downAction)
        val upAction = detector.onKeyUp(HardwareKeyShortcutDetector.KEYCODE_ALT_RIGHT)
        assertEquals(ShortcutAction.ConsumeToggleLanguage, upAction)
    }

    @Test
    fun `RAlt 누른 채 Tab 누르면 토글 안 함`() {
        val detector = newDetector()
        detector.onKeyDown(
            keyCode = HardwareKeyShortcutDetector.KEYCODE_ALT_RIGHT,
            isShift = false,
            isCtrl = false,
            isAlt = true,
        )
        detector.onKeyDown(
            keyCode = 61,
            isShift = false,
            isCtrl = false,
            isAlt = true,
        )
        detector.onKeyUp(61)
        val upAction = detector.onKeyUp(HardwareKeyShortcutDetector.KEYCODE_ALT_RIGHT)
        assertEquals(ShortcutAction.Pass, upAction)
    }

    @Test
    fun `LAlt는 한영 키가 아니므로 Pass`() {
        val detector = newDetector()
        val downAction = detector.onKeyDown(
            keyCode = 57,
            isShift = false,
            isCtrl = false,
            isAlt = true,
        )
        assertEquals(ShortcutAction.Pass, downAction)
        val upAction = detector.onKeyUp(57)
        assertEquals(ShortcutAction.Pass, upAction)
    }

    @Test
    fun `Shift Space 비활성화 시 토글 안 함`() {
        val detector = newDetector(shiftSpaceEnabled = false)
        val action = detector.onKeyDown(
            keyCode = HardwareKeyShortcutDetector.KEYCODE_SPACE,
            isShift = true,
            isCtrl = false,
            isAlt = false,
        )
        assertEquals(ShortcutAction.Pass, action)
    }

    @Test
    fun `RAlt 비활성화 시 단독 RAlt도 토글 안 함`() {
        val detector = newDetector(rAltEnabled = false)
        detector.onKeyDown(
            keyCode = HardwareKeyShortcutDetector.KEYCODE_ALT_RIGHT,
            isShift = false,
            isCtrl = false,
            isAlt = true,
        )
        val upAction = detector.onKeyUp(HardwareKeyShortcutDetector.KEYCODE_ALT_RIGHT)
        assertEquals(ShortcutAction.Pass, upAction)
    }

    @Test
    fun `LANGUAGE_SWITCH 비활성화 시 토글 안 함`() {
        val detector = newDetector(languageSwitchEnabled = false)
        val action = detector.onKeyDown(
            keyCode = HardwareKeyShortcutDetector.KEYCODE_LANGUAGE_SWITCH,
            isShift = false,
            isCtrl = false,
            isAlt = false,
        )
        assertEquals(ShortcutAction.Pass, action)
    }
}
