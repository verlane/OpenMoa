package pe.aioo.openmoa.hardware

import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CapsLockRemapperTest {

    private fun newRemapper(enabled: Boolean = true) = CapsLockRemapper().also { it.enabled = enabled }

    @Test
    fun `disabled 상태에서 모든 키는 Pass`() {
        val r = newRemapper(enabled = false)
        assertEquals(CapsLockRemapper.Result.Pass, r.onKeyDown(KeyEvent.KEYCODE_CAPS_LOCK, 0))
        assertEquals(CapsLockRemapper.Result.Pass, r.onKeyDown(KeyEvent.KEYCODE_A, 0))
        assertEquals(CapsLockRemapper.Result.Pass, r.onKeyUp(KeyEvent.KEYCODE_CAPS_LOCK))
    }

    @Test
    fun `CapsLock down은 Consumed`() {
        val r = newRemapper()
        assertEquals(CapsLockRemapper.Result.Consumed, r.onKeyDown(KeyEvent.KEYCODE_CAPS_LOCK, 0))
    }

    @Test
    fun `CapsLock up은 Consumed`() {
        val r = newRemapper()
        r.onKeyDown(KeyEvent.KEYCODE_CAPS_LOCK, 0)
        assertEquals(CapsLockRemapper.Result.Consumed, r.onKeyUp(KeyEvent.KEYCODE_CAPS_LOCK))
    }

    @Test
    fun `CapsLock 누른 상태에서 A키는 RewriteAsCtrl`() {
        val r = newRemapper()
        r.onKeyDown(KeyEvent.KEYCODE_CAPS_LOCK, 0)
        val result = r.onKeyDown(KeyEvent.KEYCODE_A, 0)
        assertTrue(result is CapsLockRemapper.Result.RewriteAsCtrl)
        val rewrite = result as CapsLockRemapper.Result.RewriteAsCtrl
        assertEquals(KeyEvent.KEYCODE_A, rewrite.keyCode)
        assertTrue(rewrite.metaState and KeyEvent.META_CTRL_ON != 0)
    }

    @Test
    fun `CapsLock 누른 상태에서 키 up은 Consumed`() {
        val r = newRemapper()
        r.onKeyDown(KeyEvent.KEYCODE_CAPS_LOCK, 0)
        r.onKeyDown(KeyEvent.KEYCODE_A, 0)
        assertEquals(CapsLockRemapper.Result.Consumed, r.onKeyUp(KeyEvent.KEYCODE_A))
    }

    @Test
    fun `CapsLock 안 누른 상태에서 일반 키는 Pass`() {
        val r = newRemapper()
        assertEquals(CapsLockRemapper.Result.Pass, r.onKeyDown(KeyEvent.KEYCODE_A, 0))
        assertEquals(CapsLockRemapper.Result.Pass, r.onKeyUp(KeyEvent.KEYCODE_A))
    }

    @Test
    fun `reset 후 CapsLock held 해제`() {
        val r = newRemapper()
        r.onKeyDown(KeyEvent.KEYCODE_CAPS_LOCK, 0)
        r.reset()
        assertEquals(CapsLockRemapper.Result.Pass, r.onKeyDown(KeyEvent.KEYCODE_A, 0))
    }
}
