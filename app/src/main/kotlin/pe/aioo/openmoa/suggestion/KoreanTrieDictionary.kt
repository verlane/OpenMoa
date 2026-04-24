package pe.aioo.openmoa.suggestion

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class KoreanTrieDictionary : Dictionary {

    private val mutex = Mutex()
    @Volatile
    private var root: Node? = null

    private val wordSource: suspend () -> Sequence<String>

    constructor(context: Context) {
        wordSource = {
            withContext(Dispatchers.IO) {
                context.assets.open("dict/ko_words.txt")
                    .bufferedReader()
                    .lineSequence()
            }
        }
    }

    internal constructor(words: Sequence<String>) {
        wordSource = { words }
    }

    private class Node {
        val children = java.util.TreeMap<Char, Node>()
        var isEnd = false
    }

    private suspend fun ensureLoaded() {
        if (root != null) return
        mutex.withLock {
            if (root != null) return
            root = buildTrie(wordSource())
        }
    }

    private fun buildTrie(words: Sequence<String>): Node {
        val trie = Node()
        words.map { it.trim() }
            .filter { it.isNotEmpty() && it.all { c -> c in '가'..'힣' } }
            .toHashSet()
            .forEach { word -> insert(trie, word) }
        return trie
    }

    private fun insert(root: Node, word: String) {
        var node = root
        for (c in word) {
            node = node.children.getOrPut(c) { Node() }
        }
        node.isEnd = true
    }

    override suspend fun prefix(prefix: String, limit: Int): List<String> {
        ensureLoaded()
        if (prefix.isEmpty() || prefix.any { it !in '가'..'힣' }) return emptyList()

        val localRoot = root ?: return emptyList()

        // 앞부분(마지막 음절 제외) 정확 탐색
        var node = localRoot
        for (i in 0 until prefix.length - 1) {
            node = node.children[prefix[i]] ?: return emptyList()
        }

        val lastChar = prefix.last()
        val results = mutableListOf<String>()

        if (!HangulSyllable.hasJongseong(lastChar)) {
            // 받침 없는 음절: 같은 초성+중성의 모든 종성 변형을 탐색
            // 정확 매칭(받침 없음)을 먼저, 그 다음 받침 있는 변형 순으로
            val prefix0 = prefix.dropLast(1)
            for (c in HangulSyllable.jongseongRange(lastChar)) {
                if (results.size >= limit) break
                node.children[c]?.let { child ->
                    collectWords(child, prefix0 + c, results, limit)
                }
            }
        } else {
            // 받침 있는 음절: 정확 매칭만
            node = node.children[lastChar] ?: return emptyList()
            collectWords(node, prefix, results, limit)
        }

        return results
    }

    private fun collectWords(node: Node, current: String, results: MutableList<String>, limit: Int) {
        if (results.size >= limit) return
        if (node.isEnd) results.add(current)
        for ((char, child) in node.children) {
            if (results.size >= limit) return
            collectWords(child, current + char, results, limit)
        }
    }
}
