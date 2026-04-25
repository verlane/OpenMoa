package pe.aioo.openmoa.suggestion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserWordStoreTest {

    private fun makeStore(): InMemoryUserWordStore = InMemoryUserWordStore()

    @Test
    fun `increment 후 topN에서 반환`() = runTest {
        val store = makeStore()
        store.increment("쏭바보")
        store.increment("쏭바보")
        assertTrue(store.topN("쏭", 5).contains("쏭바보"))
    }

    @Test
    fun `decrement 후 카운트 감소`() {
        val store = makeStore()
        store.increment("쏭바보")
        store.increment("쏭바보")
        store.decrement("쏭바보")
        assertEquals(1, store.countOf("쏭바보"))
    }

    @Test
    fun `decrement 카운트가 0이 되면 제거`() = runTest {
        val store = makeStore()
        store.increment("쏭바보")
        store.decrement("쏭바보")
        assertFalse(store.topN("쏭", 5).contains("쏭바보"))
    }

    @Test
    fun `decrement 없는 단어는 무시`() {
        val store = makeStore()
        store.decrement("없는단어")
    }

    @Test
    fun `entries는 모든 단어와 카운트 반환`() {
        val store = makeStore()
        store.increment("사랑")
        store.increment("사랑")
        store.increment("우정")
        val entries = store.entries()
        assertEquals(2, entries.size)
        val map = entries.toMap()
        assertEquals(2, map["사랑"])
        assertEquals(1, map["우정"])
    }

    @Test
    fun `remove 후 topN에서 사라짐`() = runTest {
        val store = makeStore()
        store.increment("쏭바보")
        store.increment("쏭바보")
        store.remove("쏭바보")
        assertFalse(store.topN("쏭", 5).contains("쏭바보"))
    }

    @Test
    fun `addToBlacklist 후 topN에서 제외`() = runTest {
        val store = makeStore()
        store.increment("쏭바보")
        store.increment("쏭바보")
        store.addToBlacklist("쏭바보")
        assertFalse(store.topN("쏭", 5).contains("쏭바보"))
    }

    @Test
    fun `removeFromBlacklist 후 topN에서 다시 나타남`() = runTest {
        val store = makeStore()
        store.increment("쏭바보")
        store.increment("쏭바보")
        store.addToBlacklist("쏭바보")
        store.removeFromBlacklist("쏭바보")
        assertTrue(store.topN("쏭", 5).contains("쏭바보"))
    }

    @Test
    fun `blacklist 목록 반환`() {
        val store = makeStore()
        store.addToBlacklist("쏭바보")
        store.addToBlacklist("바보")
        assertTrue(store.blacklist().contains("쏭바보"))
        assertTrue(store.blacklist().contains("바보"))
    }

    @Test
    fun `minCount 이하 단어는 topN에서 제외`() = runTest {
        val store = makeStore()
        store.increment("쏭바보")
        store.increment("사랑")
        store.increment("사랑")
        assertFalse(store.topN("쏭", 5, minCount = 2).contains("쏭바보"))
    }

    @Test
    fun `minCount 이상 단어는 topN에서 포함`() = runTest {
        val store = makeStore()
        store.increment("사랑")
        store.increment("사랑")
        assertTrue(store.topN("사", 5, minCount = 2).contains("사랑"))
    }
}
