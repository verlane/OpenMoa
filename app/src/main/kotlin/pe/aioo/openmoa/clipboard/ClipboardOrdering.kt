package pe.aioo.openmoa.clipboard

import java.util.UUID

internal fun reorderOnAdd(
    entries: List<ClipboardEntry>,
    text: String,
    now: Long,
    maxItems: Int,
): List<ClipboardEntry> {
    val mutable = entries.toMutableList()
    val existingIndex = mutable.indexOfFirst { it.text == text }
    if (existingIndex >= 0) {
        val existing = mutable.removeAt(existingIndex)
        mutable.add(0, existing.copy(createdAt = now))
    } else {
        mutable.add(0, ClipboardEntry(id = UUID.randomUUID().toString(), text = text, createdAt = now))
    }
    return trimUnpinned(mutable, maxItems)
}

internal fun reorderOnUse(
    entries: List<ClipboardEntry>,
    id: String,
    now: Long,
): List<ClipboardEntry> {
    val index = entries.indexOfFirst { it.id == id }
    if (index < 0) return entries
    val mutable = entries.toMutableList()
    val item = mutable.removeAt(index)
    mutable.add(0, item.copy(createdAt = now))
    return mutable
}

internal fun togglePin(
    entries: List<ClipboardEntry>,
    id: String,
    pinned: Boolean,
): List<ClipboardEntry> = entries.map { if (it.id == id) it.copy(pinned = pinned) else it }

// 순서를 유지하면서 unpinned 항목이 maxItems를 초과하면 꼬리부터 제거. pinned는 항상 보존.
private fun trimUnpinned(entries: List<ClipboardEntry>, maxItems: Int): List<ClipboardEntry> {
    var unpinnedCount = 0
    return entries.filter { entry ->
        if (entry.pinned) {
            true
        } else {
            unpinnedCount++
            unpinnedCount <= maxItems
        }
    }
}
