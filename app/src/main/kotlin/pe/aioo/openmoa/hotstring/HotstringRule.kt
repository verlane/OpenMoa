package pe.aioo.openmoa.hotstring

data class HotstringRule(
    val id: String,
    val trigger: String,
    val expansion: String,
    val enabled: Boolean = true
)
