package pe.aioo.openmoa.clipboard

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ClipboardPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    var onPaste: ((String) -> Unit)? = null
    var onClose: (() -> Unit)? = null
    var onEdit: ((ClipboardEntry) -> Unit)? = null
    var onAddHotstring: ((ClipboardEntry) -> Unit)? = null
    var onOpenUrl: ((String) -> Unit)? = null
    var onOpenEmail: ((String) -> Unit)? = null

    private val itemAdapter = ClipboardAdapter()
    private val recyclerView: RecyclerView
    private val emptyText: TextView
    private val filterTabs = mutableMapOf<Filter, TextView>()
    private var currentFilter = Filter.ALL
    private var allEntries: List<ClipboardEntry> = emptyList()
    private var fgColor = Color.BLACK
    private var bgColor = Color.WHITE

    private val headerView: LinearLayout
    private val menuCard: LinearLayout
    private val menuOverlay: LinearLayout

    enum class Filter(val label: String) {
        ALL("전체"), PINNED("고정"), URL("URL"), EMAIL("이메일")
    }

    enum class EntryType { TEXT, URL, EMAIL }

    init {
        val dp = resources.displayMetrics.density
        val dp4 = (4 * dp).toInt()
        val dp8 = (8 * dp).toInt()

        val contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
        addView(contentLayout)

        headerView = buildHeader(dp4, dp8)
        contentLayout.addView(headerView)

        recyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = itemAdapter
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f)
            overScrollMode = OVER_SCROLL_NEVER
        }
        contentLayout.addView(recyclerView)

        emptyText = TextView(context).apply {
            text = context.getString(pe.aioo.openmoa.R.string.clipboard_empty_hint)
            gravity = Gravity.CENTER
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f)
            visibility = GONE
        }
        contentLayout.addView(emptyText)

        menuCard = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        menuOverlay = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            visibility = GONE
            setBackgroundColor(Color.argb(128, 0, 0, 0))
            layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
            addView(View(context).apply {
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f)
                setOnClickListener { hideInlineMenu() }
            })
            addView(menuCard)
        }
        addView(menuOverlay)
    }

    private fun buildHeader(dp4: Int, dp8: Int): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp8, dp4, dp4, dp4)

            // 왼쪽 그룹(제목+탭)에 weight=1 → 공간 부족 시 자동 압축, 오른쪽 버튼 항상 확보
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)

                Filter.values().forEach { filter ->
                    val tab = TextView(context).apply {
                        text = filter.label
                        textSize = 11f
                        setPadding(dp8, dp4, dp8, dp4)
                        isClickable = true
                        isFocusable = true
                        isSingleLine = true
                        setOnClickListener { setFilter(filter) }
                    }
                    filterTabs[filter] = tab
                    addView(tab)
                }
            })

            addView(TextView(context).apply {
                text = context.getString(pe.aioo.openmoa.R.string.clipboard_clear_unpinned)
                textSize = 11f
                setPadding(dp8, dp4, dp8, dp4)
                isClickable = true
                isFocusable = true
                isSingleLine = true
                setOnClickListener {
                    showConfirmMenu(
                        message = context.getString(pe.aioo.openmoa.R.string.clipboard_clear_confirm),
                        onConfirm = {
                            ClipboardRepository.clearUnpinned(context)
                            refresh(context)
                        },
                    )
                }
            })

            addView(TextView(context).apply {
                text = "✕"
                textSize = 16f
                setPadding(dp8, dp4, dp8, dp4)
                isClickable = true
                isFocusable = true
                setOnClickListener { onClose?.invoke() }
            })
        }
    }

    private fun setFilter(filter: Filter) {
        currentFilter = filter
        updateFilterTabAppearance()
        updateList()
    }

    private fun updateFilterTabAppearance() {
        filterTabs.forEach { (f, tv) ->
            val alpha = if (f == currentFilter) 255 else 120
            tv.setTextColor(Color.argb(alpha, Color.red(fgColor), Color.green(fgColor), Color.blue(fgColor)))
        }
    }

    fun refresh(ctx: Context) {
        allEntries = ClipboardRepository.getAll(ctx)
        updateList()
    }

    private fun updateList() {
        val filtered = when (currentFilter) {
            Filter.ALL -> allEntries
            Filter.PINNED -> allEntries.filter { it.pinned }
            Filter.URL -> allEntries.filter { detectType(it.text) == EntryType.URL }
            Filter.EMAIL -> allEntries.filter { detectType(it.text) == EntryType.EMAIL }
        }
        val sorted = filtered.sortedWith(
            compareByDescending<ClipboardEntry> { it.pinned }.thenByDescending { it.createdAt }
        )
        itemAdapter.items = sorted
        itemAdapter.notifyDataSetChanged()
        val isEmpty = sorted.isEmpty()
        emptyText.visibility = if (isEmpty) VISIBLE else GONE
        recyclerView.visibility = if (isEmpty) GONE else VISIBLE
    }

    fun applyColors(fg: Int, bg: Int) {
        val fgChanged = fg != fgColor
        fgColor = fg
        bgColor = bg
        setBackgroundColor(bg)
        applyColorToHeader(fg)
        if (fgChanged) {
            itemAdapter.fgColor = fg
            itemAdapter.notifyDataSetChanged()
        }
        updateFilterTabAppearance()
        emptyText.setTextColor(Color.argb(150, Color.red(fg), Color.green(fg), Color.blue(fg)))
    }

    private fun applyColorToHeader(fg: Int) {
        applyColorToViewGroup(headerView, fg)
    }

    private fun applyColorToViewGroup(vg: android.view.ViewGroup, fg: Int) {
        for (i in 0 until vg.childCount) {
            when (val child = vg.getChildAt(i)) {
                is TextView -> child.setTextColor(fg)
                is android.view.ViewGroup -> applyColorToViewGroup(child, fg)
            }
        }
    }

    private fun detectType(text: String): EntryType {
        val t = text.trim()
        return when {
            Patterns.EMAIL_ADDRESS.matcher(t).matches() -> EntryType.EMAIL
            Patterns.WEB_URL.matcher(t).matches() -> EntryType.URL
            else -> EntryType.TEXT
        }
    }

    private fun openUrl(url: String) {
        onOpenUrl?.invoke(url.trim())
    }

    private fun openEmail(email: String) {
        onOpenEmail?.invoke(email.trim())
    }

    private fun showInlineMenu(menuItems: List<Pair<String, () -> Unit>>) {
        val dp = resources.displayMetrics.density
        val dp8 = (8 * dp).toInt()
        val dp16 = (16 * dp).toInt()

        menuCard.removeAllViews()
        menuCard.setBackgroundColor(bgColor)

        menuItems.forEach { (label, action) ->
            menuCard.addView(TextView(context).apply {
                text = label
                textSize = 13f
                setPadding(dp16, dp8, dp16, dp8)
                setTextColor(fgColor)
                isClickable = true
                isFocusable = true
                background = context.obtainStyledAttributes(
                    intArrayOf(android.R.attr.selectableItemBackground)
                ).use { it.getDrawable(0) }
                setOnClickListener {
                    action()
                    hideInlineMenu()
                }
            })
        }

        menuOverlay.visibility = VISIBLE
    }

    private fun hideInlineMenu() {
        menuOverlay.visibility = GONE
    }

    private fun showConfirmMenu(message: String, onConfirm: () -> Unit) {
        val dp = resources.displayMetrics.density
        val dp8 = (8 * dp).toInt()
        val dp12 = (12 * dp).toInt()
        val dp16 = (16 * dp).toInt()

        menuCard.removeAllViews()
        menuCard.setBackgroundColor(bgColor)

        menuCard.addView(TextView(context).apply {
            text = message
            textSize = 13f
            setPadding(dp16, dp12, dp16, dp8)
            setTextColor(fgColor)
        })

        menuCard.addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(dp8, 0, dp8, dp8)
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)

            addView(TextView(context).apply {
                text = context.getString(pe.aioo.openmoa.R.string.clipboard_clear_cancel)
                textSize = 13f
                setPadding(dp12, dp8, dp12, dp8)
                setTextColor(fgColor)
                isClickable = true
                isFocusable = true
                background = context.obtainStyledAttributes(
                    intArrayOf(android.R.attr.selectableItemBackground)
                ).use { it.getDrawable(0) }
                setOnClickListener { hideInlineMenu() }
            })

            addView(TextView(context).apply {
                text = context.getString(pe.aioo.openmoa.R.string.clipboard_clear_yes)
                textSize = 13f
                setPadding(dp12, dp8, dp12, dp8)
                setTextColor(fgColor)
                isClickable = true
                isFocusable = true
                background = context.obtainStyledAttributes(
                    intArrayOf(android.R.attr.selectableItemBackground)
                ).use { it.getDrawable(0) }
                setOnClickListener {
                    onConfirm()
                    hideInlineMenu()
                }
            })
        })

        menuOverlay.visibility = VISIBLE
    }

    private fun showTypeActions(entry: ClipboardEntry, type: EntryType) {
        showInlineMenu(buildList {
            add(context.getString(pe.aioo.openmoa.R.string.clipboard_action_paste) to { onPaste?.invoke(entry.text) })
            when (type) {
                EntryType.URL -> add(context.getString(pe.aioo.openmoa.R.string.clipboard_action_open_url) to { openUrl(entry.text) })
                EntryType.EMAIL -> add(context.getString(pe.aioo.openmoa.R.string.clipboard_action_send_email) to { openEmail(entry.text) })
                EntryType.TEXT -> Unit
            }
        })
    }

    private fun showContextMenu(entry: ClipboardEntry) {
        showInlineMenu(buildList {
            add(context.getString(pe.aioo.openmoa.R.string.clipboard_action_edit) to { onEdit?.invoke(entry) })
            val pinLabel = if (entry.pinned)
                context.getString(pe.aioo.openmoa.R.string.clipboard_action_unpin)
            else
                context.getString(pe.aioo.openmoa.R.string.clipboard_action_pin)
            add(pinLabel to {
                if (entry.pinned) ClipboardRepository.unpin(context, entry.id)
                else ClipboardRepository.pin(context, entry.id)
                refresh(context)
            })
            add(context.getString(pe.aioo.openmoa.R.string.clipboard_action_add_hotstring) to { onAddHotstring?.invoke(entry) })
            add(context.getString(pe.aioo.openmoa.R.string.clipboard_action_delete) to {
                ClipboardRepository.delete(context, entry.id)
                refresh(context)
            })
        })
    }

    inner class ClipboardAdapter : RecyclerView.Adapter<ClipboardAdapter.VH>() {
        var items: List<ClipboardEntry> = emptyList()
        var fgColor = Color.BLACK

        inner class VH(
            val root: LinearLayout,
            val textView: TextView,
            val typeView: TextView,
            val pinView: TextView,
        ) : RecyclerView.ViewHolder(root)

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val dp = parent.resources.displayMetrics.density
            val dp4 = (4 * dp).toInt()
            val dp6 = (6 * dp).toInt()
            val dp8 = (8 * dp).toInt()

            val root = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                isClickable = true
                isFocusable = true
                background = context.obtainStyledAttributes(
                    intArrayOf(android.R.attr.selectableItemBackground)
                ).use { it.getDrawable(0) }
            }

            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp8, dp6, dp8, dp6)
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            }

            val typeView = TextView(context).apply {
                textSize = 10f
                setPadding(0, 0, dp4, 0)
                layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            }
            row.addView(typeView)

            val textView = TextView(context).apply {
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                textSize = 13f
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
            }
            row.addView(textView)

            val pinView = TextView(context).apply {
                textSize = 18f
                setPadding(dp8, 0, 0, 0)
                isClickable = true
                isFocusable = true
            }
            row.addView(pinView)

            root.addView(row)

            root.addView(View(context).apply {
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 1)
                setBackgroundColor(Color.argb(40, 128, 128, 128))
            })

            return VH(root, textView, typeView, pinView)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val entry = items[position]
            val type = detectType(entry.text)
            val dimFg = Color.argb(120, Color.red(fgColor), Color.green(fgColor), Color.blue(fgColor))

            holder.textView.text = entry.text
            holder.textView.setTextColor(fgColor)

            holder.typeView.text = when (type) {
                EntryType.URL -> "URL"
                EntryType.EMAIL -> "✉"
                EntryType.TEXT -> ""
            }
            holder.typeView.setTextColor(dimFg)

            holder.pinView.text = if (entry.pinned) "★" else "☆"
            holder.pinView.setTextColor(
                if (entry.pinned) Color.parseColor("#FF8C00") else dimFg
            )

            holder.root.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                val e = items[pos]
                val t = detectType(e.text)
                if (t != EntryType.TEXT) showTypeActions(e, t) else onPaste?.invoke(e.text)
            }
            holder.root.setOnLongClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnLongClickListener true
                showContextMenu(items[pos])
                true
            }
            holder.pinView.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                val e = items[pos]
                if (e.pinned) ClipboardRepository.unpin(context, e.id)
                else ClipboardRepository.pin(context, e.id)
                refresh(context)
            }
        }

        override fun getItemCount() = items.size
    }
}
