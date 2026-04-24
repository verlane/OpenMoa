package pe.aioo.openmoa.suggestion

interface Dictionary {
    suspend fun prefix(prefix: String, limit: Int): List<String>
}
