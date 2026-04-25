package pe.aioo.openmoa.clipboard

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Patterns
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

private fun Int.withAlpha(alpha: Int): Int =
    Color.argb(alpha, Color.red(this), Color.green(this), Color.blue(this))

private fun Int.luminance(): Float =
    (0.299f * Color.red(this) + 0.587f * Color.green(this) + 0.114f * Color.blue(this)) / 255f

private fun Context.selectableBackground() =
    obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground)).let { it.getDrawable(0).also { _ -> it.recycle() } }

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
    private lateinit var pinToggleBtn: ImageView
    private var showPinSwitches = false

    enum class Filter(val label: String) {
        ALL("전체"), PINNED("고정"), NUMBER("숫자"), URL("URL"), EMAIL("Email")
    }

    enum class EntryType { TEXT, NUMBER, URL, EMAIL }

    internal data class DisplayEntry(val entry: ClipboardEntry, val type: EntryType)

    private class DisplayEntryDiff(
        private val old: List<DisplayEntry>,
        private val new: List<DisplayEntry>,
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = old.size
        override fun getNewListSize() = new.size
        override fun areItemsTheSame(o: Int, n: Int) = old[o].entry.id == new[n].entry.id
        override fun areContentsTheSame(o: Int, n: Int) = old[o] == new[n]
    }

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
            layoutManager = GridLayoutManager(context, 3)
            adapter = itemAdapter
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f)
            overScrollMode = OVER_SCROLL_NEVER
            setPadding(dp4, dp4, dp4, dp4)
            clipToPadding = false
            addItemDecoration(GridSpacingDecoration(dp4))
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
        val dp = resources.displayMetrics.density
        val dp6 = (6 * dp).toInt()
        val dp20 = (20 * dp).toInt()

        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp8, dp4, dp4, dp4)

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)

                Filter.values().forEach { filter ->
                    val tab = TextView(context).apply {
                        text = filter.label
                        textSize = 11f
                        setPadding(dp6, dp4, dp6, dp4)
                        isClickable = true
                        isFocusable = true
                        isSingleLine = true
                        setOnClickListener { setFilter(filter) }
                    }
                    filterTabs[filter] = tab
                    addView(tab)
                }
            })

            pinToggleBtn = ImageView(context).apply {
                setImageResource(pe.aioo.openmoa.R.drawable.ic_pin)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setPadding(dp8, dp4, dp8, dp4)
                isClickable = true
                isFocusable = true
                imageAlpha = 120
                layoutParams = LinearLayout.LayoutParams(dp20 + dp8 * 2, WRAP_CONTENT)
                setOnClickListener {
                    showPinSwitches = !showPinSwitches
                    imageAlpha = if (showPinSwitches) 255 else 120
                    itemAdapter.showPinSwitches = showPinSwitches
                    itemAdapter.notifyDataSetChanged()
                }
            }
            addView(pinToggleBtn)

            addView(ImageView(context).apply {
                setImageResource(pe.aioo.openmoa.R.drawable.ic_trash)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setPadding(dp8, dp4, dp8, dp4)
                isClickable = true
                isFocusable = true
                layoutParams = LinearLayout.LayoutParams(dp20 + dp8 * 2, WRAP_CONTENT)
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
        filterTabs.forEach { (f, v) ->
            val alpha = if (f == currentFilter) 255 else 120
            v.setTextColor(fgColor.withAlpha(alpha))
        }
    }

    fun refresh(ctx: Context) {
        allEntries = ClipboardRepository.getAll(ctx)
        updateList()
    }

    private fun updateList() {
        val allWithType = allEntries.map { DisplayEntry(it, detectType(it.text)) }
        val filtered = when (currentFilter) {
            Filter.ALL -> allWithType
            Filter.PINNED -> allWithType.filter { it.entry.pinned }
            Filter.NUMBER -> allWithType.filter { it.type == EntryType.NUMBER }
            Filter.URL -> allWithType.filter { it.type == EntryType.URL }
            Filter.EMAIL -> allWithType.filter { it.type == EntryType.EMAIL }
        }
        val newItems = filtered.sortedWith(
            compareByDescending<DisplayEntry> { it.entry.pinned }.thenByDescending { it.entry.createdAt }
        )
        val diff = DiffUtil.calculateDiff(DisplayEntryDiff(itemAdapter.items, newItems))
        itemAdapter.items = newItems
        diff.dispatchUpdatesTo(itemAdapter)
        val isEmpty = newItems.isEmpty()
        emptyText.visibility = if (isEmpty) VISIBLE else GONE
        recyclerView.visibility = if (isEmpty) GONE else VISIBLE
    }

    fun applyColors(fg: Int, bg: Int) {
        val changed = fg != fgColor || bg != bgColor
        fgColor = fg
        bgColor = bg
        setBackgroundColor(bg)
        applyColorToViewGroup(headerView, fg)
        if (changed) {
            val pinnedAccent = if (bg.luminance() < 0.5f)
                Color.parseColor("#FF9500")
            else
                Color.parseColor("#C86000")
            itemAdapter.fgColor = fg
            itemAdapter.bgColor = bg
            itemAdapter.pinnedAccent = pinnedAccent
            itemAdapter.updateSwitchTints(fg, pinnedAccent)
            itemAdapter.notifyDataSetChanged()
        }
        updateFilterTabAppearance()
        emptyText.setTextColor(fgColor.withAlpha(150))
    }

    private fun applyColorToViewGroup(vg: ViewGroup, fg: Int) {
        for (i in 0 until vg.childCount) {
            when (val child = vg.getChildAt(i)) {
                is ImageView -> child.setColorFilter(fg, PorterDuff.Mode.SRC_IN)
                is TextView -> child.setTextColor(fg)
                is ViewGroup -> applyColorToViewGroup(child, fg)
            }
        }
    }

    private fun detectType(text: String): EntryType {
        val t = text.trim()
        return when {
            Patterns.EMAIL_ADDRESS.matcher(t).matches() -> EntryType.EMAIL
            Patterns.WEB_URL.matcher(t).matches() -> EntryType.URL
            t.count { it.isDigit() } >= 3 && t.all { it.isDigit() || it in " -.,()+#*" } -> EntryType.NUMBER
            else -> EntryType.TEXT
        }
    }

    private fun openUrl(url: String) = onOpenUrl?.invoke(url.trim())

    private fun openEmail(email: String) = onOpenEmail?.invoke(email.trim())

    private fun updateMenuCardBackground() {
        val radius = 16f * resources.displayMetrics.density
        menuCard.background = GradientDrawable().apply {
            setShape(GradientDrawable.RECTANGLE)
            setCornerRadii(floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f))
            setColor(bgColor)
        }
    }

    private fun showInlineMenu(menuItems: List<Pair<String, () -> Unit>>) {
        val dp = resources.displayMetrics.density
        val dp8 = (8 * dp).toInt()
        val dp16 = (16 * dp).toInt()

        menuCard.removeAllViews()
        updateMenuCardBackground()

        menuItems.forEach { (label, action) ->
            menuCard.addView(TextView(context).apply {
                text = label
                textSize = 13f
                setPadding(dp16, dp8, dp16, dp8)
                setTextColor(fgColor)
                isClickable = true
                isFocusable = true
                background = context.selectableBackground()
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
        updateMenuCardBackground()

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
                background = context.selectableBackground()
                setOnClickListener { hideInlineMenu() }
            })

            addView(TextView(context).apply {
                text = context.getString(pe.aioo.openmoa.R.string.clipboard_clear_yes)
                textSize = 13f
                setPadding(dp12, dp8, dp12, dp8)
                setTextColor(fgColor)
                isClickable = true
                isFocusable = true
                background = context.selectableBackground()
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
            add(context.getString(pe.aioo.openmoa.R.string.clipboard_action_paste) to {
                ClipboardRepository.use(context, entry.id)
                refresh(context)
                onPaste?.invoke(entry.text)
            })
            when (type) {
                EntryType.URL -> add(context.getString(pe.aioo.openmoa.R.string.clipboard_action_open_url) to {
                    ClipboardRepository.use(context, entry.id)
                    refresh(context)
                    openUrl(entry.text)
                })
                EntryType.EMAIL -> add(context.getString(pe.aioo.openmoa.R.string.clipboard_action_send_email) to {
                    ClipboardRepository.use(context, entry.id)
                    refresh(context)
                    openEmail(entry.text)
                })
                else -> Unit
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

    private class GridSpacingDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.set(spacing, spacing, spacing, spacing)
        }
    }

    inner class ClipboardAdapter : RecyclerView.Adapter<ClipboardAdapter.VH>() {
        internal var items: List<DisplayEntry> = emptyList()
        var fgColor = Color.BLACK
        var bgColor = Color.WHITE
        var pinnedAccent = Color.parseColor("#C86000")
        var showPinSwitches = false

        private val switchStates = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked),
        )
        private var thumbTints = ColorStateList(switchStates, intArrayOf(pinnedAccent, Color.BLACK.withAlpha(160)))
        private var trackTints = ColorStateList(switchStates, intArrayOf(pinnedAccent.withAlpha(100), Color.BLACK.withAlpha(40)))

        fun updateSwitchTints(fg: Int, accent: Int) {
            thumbTints = ColorStateList(switchStates, intArrayOf(accent, fg.withAlpha(160)))
            trackTints = ColorStateList(switchStates, intArrayOf(accent.withAlpha(100), fg.withAlpha(40)))
        }

        inner class VH(
            val root: FrameLayout,
            val textView: TextView,
            val typeView: TextView,
            val pinView: SwitchCompat,
            val cardBg: GradientDrawable,
            val rippleDrawable: RippleDrawable,
        ) : RecyclerView.ViewHolder(root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val dp = parent.resources.displayMetrics.density
            val dp4 = (4 * dp).toInt()
            val dp6 = (6 * dp).toInt()
            val dp48 = (48 * dp).toInt()
            val cornerRadius = 10f * dp

            val cardBg = GradientDrawable().apply {
                setShape(GradientDrawable.RECTANGLE)
                setCornerRadius(cornerRadius)
                setColor(Color.TRANSPARENT)
            }
            val rippleMask = GradientDrawable().apply {
                setShape(GradientDrawable.RECTANGLE)
                setCornerRadius(cornerRadius)
                setColor(Color.WHITE)
            }
            val rippleDrawable = RippleDrawable(
                ColorStateList.valueOf(Color.TRANSPARENT), cardBg, rippleMask
            )

            val root = FrameLayout(context).apply {
                isClickable = true
                isFocusable = true
                minimumHeight = dp48
                background = rippleDrawable
                layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            }

            val content = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp6, dp4, dp6, dp6)
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            }
            root.addView(content)

            val topRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            }
            content.addView(topRow)

            val typeView = TextView(context).apply {
                textSize = 9f
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
            }
            topRow.addView(typeView)

            val themedCtx = ContextThemeWrapper(context, pe.aioo.openmoa.R.style.Theme_OpenMoa_Settings)
            val neg = (-12 * dp).toInt()
            val pinView = SwitchCompat(themedCtx).apply {
                showText = false
                scaleX = 0.75f
                scaleY = 0.75f
                isClickable = true
                isFocusable = true
                layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                    topMargin = neg
                    bottomMargin = neg
                }
            }
            topRow.addView(pinView)

            val textView = TextView(context).apply {
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
                textSize = 11f
                setPadding(0, dp4, 0, 0)
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            }
            content.addView(textView)

            return VH(root, textView, typeView, pinView, cardBg, rippleDrawable)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val dp = resources.displayMetrics.density
            val (entry, type) = items[position]
            val dimFg = fgColor.withAlpha(120)
            val isFirst = position == 0

            val cardBgAlpha = if (isFirst) 45 else 20
            val strokeColor = when {
                entry.pinned -> pinnedAccent
                isFirst -> fgColor.withAlpha(140)
                else -> fgColor.withAlpha(40)
            }
            val strokeWidth = if (entry.pinned || isFirst) (1.5f * dp).toInt() else (1f * dp).toInt()

            holder.cardBg.setColor(fgColor.withAlpha(cardBgAlpha))
            holder.cardBg.setStroke(strokeWidth, strokeColor)
            holder.rippleDrawable.setColor(ColorStateList.valueOf(fgColor.withAlpha(80)))

            holder.textView.text = entry.text
            holder.textView.setTextColor(fgColor)

            holder.typeView.text = when (type) {
                EntryType.URL -> "URL"
                EntryType.EMAIL -> "Email"
                EntryType.NUMBER -> "숫자"
                EntryType.TEXT -> "Text"
            }
            holder.typeView.setTextColor(dimFg)

            holder.pinView.visibility = if (showPinSwitches) VISIBLE else GONE
            holder.pinView.setOnCheckedChangeListener(null)
            holder.pinView.isChecked = entry.pinned
            holder.pinView.jumpDrawablesToCurrentState()
            holder.pinView.thumbTintList = thumbTints
            holder.pinView.trackTintList = trackTints
            holder.pinView.setOnCheckedChangeListener { _, isChecked ->
                val pos = holder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnCheckedChangeListener
                val e = items[pos].entry
                if (isChecked) ClipboardRepository.pin(context, e.id)
                else ClipboardRepository.unpin(context, e.id)
                refresh(context)
            }

            holder.root.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                val (e, t) = items[pos]
                if (t == EntryType.URL || t == EntryType.EMAIL) {
                    showTypeActions(e, t)
                } else {
                    ClipboardRepository.use(context, e.id)
                    refresh(context)
                    onPaste?.invoke(e.text)
                }
            }
            holder.root.setOnLongClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnLongClickListener true
                holder.root.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                showContextMenu(items[pos].entry)
                true
            }
        }

        override fun getItemCount() = items.size
    }
}
