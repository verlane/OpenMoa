package pe.aioo.openmoa.hardware

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TabHoldVimDetectorTest {

    private fun newDetector(enabled: Boolean = true) = TabHoldVimDetector().also { it.enabled = enabled }

    private fun TabHoldVimDetector.tabDown() = onKeyDown(TabHoldVimDetector.KEYCODE_TAB, false, false)
    private fun TabHoldVimDetector.tabUp() = onKeyUp(TabHoldVimDetector.KEYCODE_TAB)
    private fun TabHoldVimDetector.key(kc: Int, shift: Boolean = false) = onKeyDown(kc, shift, false)

    @Test
    fun `disabled 상태에서 모든 키는 PassThrough`() {
        val d = newDetector(enabled = false)
        assertEquals(VimAction.PassThrough, d.tabDown())
        assertEquals(VimAction.PassThrough, d.key(TabHoldVimDetector.KEYCODE_H))
        assertEquals(VimAction.PassThrough, d.tabUp())
    }

    @Test
    fun `Tab 단독 tap은 InjectTab`() {
        val d = newDetector()
        assertEquals(VimAction.Consume, d.tabDown())
        assertEquals(VimAction.InjectTab, d.tabUp())
    }

    @Test
    fun `Tab repeat은 Consume`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.Consume, d.onKeyDown(TabHoldVimDetector.KEYCODE_TAB, false, true))
    }

    @Test
    fun `Tab hold 중 h는 MoveLeft`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.MoveCursor(CursorDirection.LEFT), d.key(TabHoldVimDetector.KEYCODE_H))
    }

    @Test
    fun `Tab hold 중 j는 MoveDown`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.MoveCursor(CursorDirection.DOWN), d.key(TabHoldVimDetector.KEYCODE_J))
    }

    @Test
    fun `Tab hold 중 k는 MoveUp`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.MoveCursor(CursorDirection.UP), d.key(TabHoldVimDetector.KEYCODE_K))
    }

    @Test
    fun `Tab hold 중 l는 MoveRight`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.MoveCursor(CursorDirection.RIGHT), d.key(TabHoldVimDetector.KEYCODE_L))
    }

    @Test
    fun `Tab hold 중 w는 MoveWord forward`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.MoveWord(forward = true), d.key(TabHoldVimDetector.KEYCODE_W))
    }

    @Test
    fun `Tab hold 중 b는 MoveWord backward`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.MoveWord(forward = false), d.key(TabHoldVimDetector.KEYCODE_B))
    }

    @Test
    fun `Tab hold 중 e는 LineEnd`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.LineEnd(), d.key(TabHoldVimDetector.KEYCODE_E))
    }

    @Test
    fun `Tab hold 중 r는 LineHome`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.LineHome(), d.key(TabHoldVimDetector.KEYCODE_R))
    }

    @Test
    fun `Tab hold 중 f는 PageScroll down`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.PageScroll(down = true), d.key(TabHoldVimDetector.KEYCODE_F))
    }

    @Test
    fun `Tab hold 중 Shift+f는 PageScroll up`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.PageScroll(down = false), d.key(TabHoldVimDetector.KEYCODE_F, shift = true))
    }

    @Test
    fun `Tab hold 중 g는 DocumentEdge start`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.DocumentEdge(end = false), d.key(TabHoldVimDetector.KEYCODE_G))
    }

    @Test
    fun `Tab hold 중 Shift+g는 DocumentEdge end`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.DocumentEdge(end = true), d.key(TabHoldVimDetector.KEYCODE_G, shift = true))
    }

    @Test
    fun `Tab hold 중 0는 LineHome`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.LineHome(), d.key(TabHoldVimDetector.KEYCODE_ZERO))
    }

    @Test
    fun `Tab hold 중 Shift+4는 LineEnd`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.LineEnd(), d.key(TabHoldVimDetector.KEYCODE_4, shift = true))
    }

    @Test
    fun `Tab hold 중 Shift+6는 LineHome`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.LineHome(), d.key(TabHoldVimDetector.KEYCODE_6, shift = true))
    }

    @Test
    fun `Tab hold 중 x는 DeleteChar`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.DeleteChar, d.key(TabHoldVimDetector.KEYCODE_X))
    }

    @Test
    fun `Tab hold 중 d는 DeleteLine`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.DeleteLine, d.key(TabHoldVimDetector.KEYCODE_D))
    }

    @Test
    fun `Tab hold 중 y는 YankLine`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.YankLine, d.key(TabHoldVimDetector.KEYCODE_Y))
    }

    @Test
    fun `Tab hold 중 p는 Paste`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.Paste, d.key(TabHoldVimDetector.KEYCODE_P))
    }

    @Test
    fun `Tab hold 중 o는 NewLineBelow`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.NewLineBelow, d.key(TabHoldVimDetector.KEYCODE_O))
    }

    @Test
    fun `Tab hold 중 Shift+o는 NewLineAbove`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.NewLineAbove, d.key(TabHoldVimDetector.KEYCODE_O, shift = true))
    }

    @Test
    fun `Tab을 modifier로 쓴 뒤 Tab up은 Consume`() {
        val d = newDetector()
        d.tabDown()
        d.key(TabHoldVimDetector.KEYCODE_H)
        assertEquals(VimAction.Consume, d.tabUp())
    }

    @Test
    fun `Tab hold 중 Shift 키 자체는 Consume`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.Consume, d.onKeyDown(59, false, false)) // SHIFT_LEFT
        assertEquals(VimAction.Consume, d.onKeyUp(59))
    }

    @Test
    fun `Tab hold 중 v 누르면 visual mode 진입 (VisualEnter), 이후 h는 extend=true`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.VisualEnter, d.key(TabHoldVimDetector.KEYCODE_V))
        val move = d.key(TabHoldVimDetector.KEYCODE_H)
        assertEquals(VimAction.MoveCursor(CursorDirection.LEFT, extend = true), move)
    }

    @Test
    fun `visual mode 중 v 재입력은 Consume (선택 유지)`() {
        val d = newDetector()
        d.tabDown()
        d.key(TabHoldVimDetector.KEYCODE_V)
        assertEquals(VimAction.Consume, d.key(TabHoldVimDetector.KEYCODE_V))
    }

    @Test
    fun `visual mode 종료 후 hjkl은 extend=false`() {
        val d = newDetector()
        d.tabDown()
        d.key(TabHoldVimDetector.KEYCODE_V)
        d.key(TabHoldVimDetector.KEYCODE_V)  // exit visual
        assertEquals(VimAction.MoveCursor(CursorDirection.LEFT, extend = false), d.key(TabHoldVimDetector.KEYCODE_H))
    }

    @Test
    fun `visual mode 중 y는 YankSelection 후 visual 해제`() {
        val d = newDetector()
        d.tabDown()
        d.key(TabHoldVimDetector.KEYCODE_V)
        assertEquals(VimAction.YankSelection, d.key(TabHoldVimDetector.KEYCODE_Y))
        assertEquals(VimAction.MoveCursor(CursorDirection.LEFT, extend = false), d.key(TabHoldVimDetector.KEYCODE_H))
    }

    @Test
    fun `visual mode 아닐 때 y는 YankLine`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.YankLine, d.key(TabHoldVimDetector.KEYCODE_Y))
    }

    @Test
    fun `visual mode 중 d는 DeleteSelection 후 visual mode 해제`() {
        val d = newDetector()
        d.tabDown()
        d.key(TabHoldVimDetector.KEYCODE_V)
        assertEquals(VimAction.DeleteSelection, d.key(TabHoldVimDetector.KEYCODE_D))
        assertEquals(VimAction.MoveCursor(CursorDirection.LEFT, extend = false), d.key(TabHoldVimDetector.KEYCODE_H))
    }

    @Test
    fun `visual mode 중 x는 DeleteSelection 후 visual mode 해제`() {
        val d = newDetector()
        d.tabDown()
        d.key(TabHoldVimDetector.KEYCODE_V)
        assertEquals(VimAction.DeleteSelection, d.key(TabHoldVimDetector.KEYCODE_X))
    }

    @Test
    fun `visual mode 아닐 때 x는 DeleteChar`() {
        val d = newDetector()
        d.tabDown()
        assertEquals(VimAction.DeleteChar, d.key(TabHoldVimDetector.KEYCODE_X))
    }

    @Test
    fun `visual mode 중 e는 LineEnd(extend=true)`() {
        val d = newDetector()
        d.tabDown()
        d.key(TabHoldVimDetector.KEYCODE_V)
        assertEquals(VimAction.LineEnd(extend = true), d.key(TabHoldVimDetector.KEYCODE_E))
    }

    @Test
    fun `visual mode 중 r는 LineHome(extend=true)`() {
        val d = newDetector()
        d.tabDown()
        d.key(TabHoldVimDetector.KEYCODE_V)
        assertEquals(VimAction.LineHome(extend = true), d.key(TabHoldVimDetector.KEYCODE_R))
    }

    @Test
    fun `visual mode 중 f는 PageScroll(down=true, extend=true)`() {
        val d = newDetector()
        d.tabDown()
        d.key(TabHoldVimDetector.KEYCODE_V)
        assertEquals(VimAction.PageScroll(down = true, extend = true), d.key(TabHoldVimDetector.KEYCODE_F))
    }

    @Test
    fun `visual mode 중 g는 DocumentEdge(end=false, extend=true)`() {
        val d = newDetector()
        d.tabDown()
        d.key(TabHoldVimDetector.KEYCODE_V)
        assertEquals(VimAction.DocumentEdge(end = false, extend = true), d.key(TabHoldVimDetector.KEYCODE_G))
    }

    @Test
    fun `Tab release 시 visual mode 해제 - 재차 Tab+h는 extend=false`() {
        val d = newDetector()
        d.tabDown()
        d.key(TabHoldVimDetector.KEYCODE_V)
        d.tabUp()
        d.tabDown()
        val move = d.key(TabHoldVimDetector.KEYCODE_H)
        assertEquals(VimAction.MoveCursor(CursorDirection.LEFT, extend = false), move)
    }

    @Test
    fun `reset 후 상태 초기화`() {
        val d = newDetector()
        d.tabDown()
        d.key(TabHoldVimDetector.KEYCODE_V)
        d.reset()
        assertEquals(VimAction.InjectTab, d.tabDown().let { d.tabUp() })
    }

    @Test
    fun `isTabHeld 상태 반영`() {
        val d = newDetector()
        assert(!d.isTabHeld)
        d.tabDown()
        assert(d.isTabHeld)
        d.tabUp()
        assert(!d.isTabHeld)
    }

    @Test
    fun `isVisualMode 상태 반영`() {
        val d = newDetector()
        assert(!d.isVisualMode)
        d.tabDown()
        d.key(TabHoldVimDetector.KEYCODE_V)
        assert(d.isVisualMode)
        d.key(TabHoldVimDetector.KEYCODE_V)
        assert(!d.isVisualMode)
    }
}
