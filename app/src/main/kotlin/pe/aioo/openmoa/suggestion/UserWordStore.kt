package pe.aioo.openmoa.suggestion

interface UserWordStore {
    suspend fun topN(prefix: String, limit: Int): List<String>
    fun increment(word: String)
    fun clear()
}

class NoOpUserWordStore : UserWordStore {
    override suspend fun topN(prefix: String, limit: Int): List<String> = emptyList()
    override fun increment(word: String) = Unit
    override fun clear() = Unit
}
