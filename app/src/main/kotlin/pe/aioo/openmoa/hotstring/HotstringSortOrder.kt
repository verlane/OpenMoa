package pe.aioo.openmoa.hotstring

import java.text.Collator
import java.util.Locale

enum class HotstringSortOrder {
    INSERTION_ORDER,
    TRIGGER_ASC,
    TRIGGER_DESC,
    EXPANSION_ASC,
    EXPANSION_DESC;

    companion object {
        val DEFAULT = INSERTION_ORDER

        fun fromString(name: String?): HotstringSortOrder =
            values().find { it.name == name } ?: DEFAULT
    }
}

fun List<HotstringRule>.sortedBy(order: HotstringSortOrder): List<HotstringRule> {
    val collator = Collator.getInstance(Locale.KOREAN)
    return when (order) {
        HotstringSortOrder.INSERTION_ORDER -> this
        HotstringSortOrder.TRIGGER_ASC -> sortedWith { a, b -> collator.compare(a.trigger, b.trigger) }
        HotstringSortOrder.TRIGGER_DESC -> sortedWith { a, b -> collator.compare(b.trigger, a.trigger) }
        HotstringSortOrder.EXPANSION_ASC -> sortedWith { a, b -> collator.compare(a.expansion, b.expansion) }
        HotstringSortOrder.EXPANSION_DESC -> sortedWith { a, b -> collator.compare(b.expansion, a.expansion) }
    }
}
