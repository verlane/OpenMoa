package pe.aioo.openmoa.clipboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardOrderingTest {

    private val now = 1_000_000L

    private fun entry(id: String, text: String, pinned: Boolean = false, createdAt: Long = now) =
        ClipboardEntry(id = id, text = text, pinned = pinned, createdAt = createdAt)

    // --- reorderOnAdd ---

    @Test
    fun `add - 새 항목은 index 0에 위치`() {
        val pinned = entry("p1", "pinned", pinned = true)
        val unpinned = entry("u1", "unpinned")
        val result = reorderOnAdd(listOf(pinned, unpinned), "new", now, maxItems = 10)
        assertEquals("new", result[0].text)
    }

    @Test
    fun `add - 기존 항목 재저장 시 index 0으로 이동 및 createdAt 갱신`() {
        val a = entry("a", "hello", createdAt = 100L)
        val b = entry("b", "world", createdAt = 200L)
        val result = reorderOnAdd(listOf(a, b), "hello", now, maxItems = 10)
        assertEquals("hello", result[0].text)
        assertEquals(now, result[0].createdAt)
        assertEquals("world", result[1].text)
    }

    @Test
    fun `add - pinned 항목이 있어도 새 항목이 index 0`() {
        val p = entry("p", "pinned", pinned = true)
        val result = reorderOnAdd(listOf(p), "new", now, maxItems = 10)
        assertEquals("new", result[0].text)
        assertEquals("pinned", result[1].text)
    }

    @Test
    fun `add - maxItems 초과 시 unpinned 꼬리부터 제거, pinned 보존`() {
        val p = entry("p", "pinned", pinned = true)
        val u1 = entry("u1", "a")
        val u2 = entry("u2", "b")
        val result = reorderOnAdd(listOf(p, u1, u2), "new", now, maxItems = 2)
        assertTrue(result.any { it.pinned })
        val unpinnedCount = result.count { !it.pinned }
        assertEquals(2, unpinnedCount)
        assertEquals("new", result[0].text)
    }

    @Test
    fun `add - 빈 텍스트는 처리 안 함 (Repository에서 걸러지지만 순수 함수 동작 확인)`() {
        val entries = listOf(entry("a", "hello"))
        val result = reorderOnAdd(entries, "  ", now, maxItems = 10)
        assertEquals(2, result.size)
    }

    // --- reorderOnUse ---

    @Test
    fun `use - unpinned 항목 사용 시 index 0으로 이동`() {
        val p = entry("p", "pinned", pinned = true)
        val u = entry("u", "used")
        val result = reorderOnUse(listOf(p, u), "u", now)
        assertEquals("used", result[0].text)
    }

    @Test
    fun `use - pinned 항목 사용 시 index 0 유지, createdAt 갱신`() {
        val p = entry("p", "pinned", pinned = true, createdAt = 100L)
        val u = entry("u", "other")
        val result = reorderOnUse(listOf(p, u), "p", now)
        assertEquals("pinned", result[0].text)
        assertEquals(now, result[0].createdAt)
    }

    @Test
    fun `use - 존재하지 않는 id면 리스트 그대로 반환`() {
        val entries = listOf(entry("a", "hello"))
        val result = reorderOnUse(entries, "nonexistent", now)
        assertEquals(entries, result)
    }

    // --- togglePin ---

    @Test
    fun `pin - pinned 플래그만 변경, 순서 유지`() {
        val a = entry("a", "a")
        val b = entry("b", "b")
        val result = togglePin(listOf(a, b), "b", pinned = true)
        assertEquals("a", result[0].text)
        assertEquals("b", result[1].text)
        assertTrue(result[1].pinned)
    }

    @Test
    fun `unpin - pinned 플래그만 변경, 순서 유지`() {
        val p = entry("p", "pinned", pinned = true)
        val u = entry("u", "unpinned")
        val result = togglePin(listOf(p, u), "p", pinned = false)
        assertEquals("pinned", result[0].text)
        assertEquals(false, result[0].pinned)
    }

    @Test
    fun `togglePin - 존재하지 않는 id면 리스트 그대로 반환`() {
        val entries = listOf(entry("a", "hello"))
        val result = togglePin(entries, "nonexistent", pinned = true)
        assertEquals(entries, result)
    }
}
