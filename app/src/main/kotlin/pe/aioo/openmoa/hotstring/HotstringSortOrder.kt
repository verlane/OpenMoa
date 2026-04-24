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
        private val collator: Collator = Collator.getInstance(Locale.KOREAN)

        fun fromString(name: String?): HotstringSortOrder =
            values().find { it.name == name } ?: DEFAULT

        fun compare(a: String, b: String): Int = collator.compare(a, b)
    }
}

fun List<HotstringRule>.sortedByOrder(order: HotstringSortOrder): List<HotstringRule> =
    when (order) {
        HotstringSortOrder.INSERTION_ORDER -> this
        HotstringSortOrder.TRIGGER_ASC -> sortedWith { a, b -> HotstringSortOrder.compare(a.trigger, b.trigger) }
        HotstringSortOrder.TRIGGER_DESC -> sortedWith { a, b -> HotstringSortOrder.compare(b.trigger, a.trigger) }
        HotstringSortOrder.EXPANSION_ASC -> sortedWith { a, b -> HotstringSortOrder.compare(a.expansion, b.expansion) }
        HotstringSortOrder.EXPANSION_DESC -> sortedWith { a, b -> HotstringSortOrder.compare(b.expansion, a.expansion) }
    }
