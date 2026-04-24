package pe.aioo.openmoa.suggestion

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class TrieDictionary(private val context: Context) : Dictionary {

    private val mutex = Mutex()
    @Volatile
    private var root: Node? = null

    private class Node {
        val children = arrayOfNulls<Node>(26)
        var isEnd = false
    }

    private suspend fun ensureLoaded() {
        if (root != null) return
        mutex.withLock {
            if (root != null) return
            root = buildTrie()
        }
    }

    private suspend fun buildTrie(): Node = withContext(Dispatchers.IO) {
        val trie = Node()
        context.assets.open("dict/en_words.txt").bufferedReader().useLines { lines ->
            lines.map { it.trim().lowercase() }
                .filter { it.isNotEmpty() && it.all { c -> c in 'a'..'z' } }
                .toHashSet()
                .forEach { word -> insert(trie, word) }
        }
        trie
    }

    private fun insert(root: Node, word: String) {
        var node = root
        for (c in word) {
            val idx = c - 'a'
            val child = node.children[idx] ?: Node().also { node.children[idx] = it }
            node = child
        }
        node.isEnd = true
    }

    override suspend fun prefix(prefix: String, limit: Int): List<String> {
        ensureLoaded()
        val normalized = prefix.lowercase()
        if (normalized.isEmpty() || normalized.any { it !in 'a'..'z' }) return emptyList()

        val localRoot = root ?: return emptyList()
        var node = localRoot
        for (c in normalized) {
            node = node.children[c - 'a'] ?: return emptyList()
        }

        val results = mutableListOf<String>()
        collectWords(node, normalized, results, limit)
        return results
    }

    private fun collectWords(node: Node, current: String, results: MutableList<String>, limit: Int) {
        if (results.size >= limit) return
        if (node.isEnd) results.add(current)
        for (i in 0 until 26) {
            if (results.size >= limit) return
            node.children[i]?.let { collectWords(it, current + ('a' + i), results, limit) }
        }
    }
}
