package pe.aioo.openmoa.suggestion

interface UserWordStore {
    fun increment(word: String)
    fun topN(prefix: String, limit: Int): List<String>
    fun clear()
}

class NoOpUserWordStore : UserWordStore {
    override fun increment(word: String) = Unit
    override fun topN(prefix: String, limit: Int): List<String> = emptyList()
    override fun clear() = Unit
}
