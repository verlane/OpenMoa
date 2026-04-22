package pe.aioo.openmoa.hotstring

object HotstringMatcher {

    fun findMatch(buffer: String, rules: List<HotstringRule>): HotstringRule? {
        if (buffer.isEmpty() || rules.isEmpty()) return null
        return rules
            .filter { it.enabled && it.trigger.isNotEmpty() }
            .sortedByDescending { it.trigger.length }
            .firstOrNull { buffer.endsWith(it.trigger) }
    }

    fun bufferLengthNeeded(rules: List<HotstringRule>): Int =
        rules.filter { it.enabled }.maxOfOrNull { it.trigger.length } ?: 0
}
