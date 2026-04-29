package pe.aioo.openmoa.floating

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import pe.aioo.openmoa.R
import pe.aioo.openmoa.settings.SettingsPreferences
import kotlin.math.abs

class FloatingIndicatorManager(context: Context) {

    private val context: Context = context.applicationContext
    private val windowManager: WindowManager =
        this.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val edgeMarginPx = dp(8)
    private val verticalSafeMarginPx = dp(INDICATOR_SIZE_DP)
    private val indicatorSizePx = dp(INDICATOR_SIZE_DP)

    private var rootView: FrameLayout? = null
    private var labelView: TextView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var onTap: (() -> Unit)? = null
    private var snapAnimator: ValueAnimator? = null

    fun canShow(): Boolean = Settings.canDrawOverlays(context)

    fun isShowing(): Boolean = rootView != null

    fun setOnTapListener(listener: () -> Unit) {
        onTap = listener
    }

    fun show(isKorean: Boolean) {
        if (rootView != null) {
            updateLanguage(isKorean)
            return
        }
        if (!canShow()) return

        val view = LayoutInflater.from(context)
            .inflate(R.layout.floating_indicator, null) as FrameLayout
        val label = view.findViewById<TextView>(R.id.floating_indicator_label)
        applyLanguageLabel(label, isKorean)

        val params = buildLayoutParams()
        val (initialX, initialY) = resolveInitialPosition(view)
        params.x = initialX
        params.y = initialY

        attachTouchListener(view, params)

        windowManager.addView(view, params)
        rootView = view
        labelView = label
        layoutParams = params
    }

    fun hide() {
        snapAnimator?.cancel()
        snapAnimator = null
        rootView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: IllegalArgumentException) {
                // 이미 제거됐거나 attach되지 않은 경우 무시
            }
        }
        rootView = null
        labelView = null
        layoutParams = null
    }

    fun updateLanguage(isKorean: Boolean) {
        labelView?.let { applyLanguageLabel(it, isKorean) }
    }

    private fun applyLanguageLabel(label: TextView, isKorean: Boolean) {
        label.text = context.getString(
            if (isKorean) R.string.floating_indicator_label_ko
            else R.string.floating_indicator_label_en
        )
    }

    private fun buildLayoutParams(): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        return WindowManager.LayoutParams(
            indicatorSizePx,
            indicatorSizePx,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
    }

    private fun resolveInitialPosition(view: View): Pair<Int, Int> {
        val metrics = displayMetrics()
        val viewSize = indicatorSizePx
        val saved = SettingsPreferences.getFloatingIndicatorPosition(context)
        return if (saved != null) {
            val (x, y) = saved
            val clampedY = FloatingSnapCalculator.clampY(
                screenHeight = metrics.heightPixels,
                viewHeight = viewSize,
                currentY = y,
                topMargin = verticalSafeMarginPx,
                bottomMargin = verticalSafeMarginPx,
            )
            x to clampedY
        } else {
            FloatingSnapCalculator.defaultPosition(
                screenWidth = metrics.widthPixels,
                screenHeight = metrics.heightPixels,
                viewWidth = viewSize,
                viewHeight = viewSize,
                edgeMargin = edgeMarginPx,
            )
        }
    }

    private fun attachTouchListener(view: View, params: WindowManager.LayoutParams) {
        var initialRawX = 0f
        var initialRawY = 0f
        var initialParamsX = 0
        var initialParamsY = 0
        var downTime = 0L
        var dragging = false

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    snapAnimator?.cancel()
                    initialRawX = event.rawX
                    initialRawY = event.rawY
                    initialParamsX = params.x
                    initialParamsY = params.y
                    downTime = System.currentTimeMillis()
                    dragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialRawX).toInt()
                    val dy = (event.rawY - initialRawY).toInt()
                    if (!dragging && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                        dragging = true
                    }
                    if (dragging) {
                        params.x = initialParamsX + dx
                        params.y = FloatingSnapCalculator.clampY(
                            screenHeight = displayMetrics().heightPixels,
                            viewHeight = v.height.takeIf { it > 0 } ?: indicatorSizePx,
                            currentY = initialParamsY + dy,
                            topMargin = verticalSafeMarginPx,
                            bottomMargin = verticalSafeMarginPx,
                        )
                        try {
                            windowManager.updateViewLayout(v, params)
                        } catch (_: IllegalArgumentException) {
                            // 뷰가 detach된 경우 무시
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val elapsed = System.currentTimeMillis() - downTime
                    if (!dragging && elapsed < TAP_THRESHOLD_MS) {
                        v.performClick()
                        onTap?.invoke()
                    } else if (dragging) {
                        snapToEdge(v, params)
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (dragging) snapToEdge(v, params)
                    true
                }
                else -> false
            }
        }
    }

    private fun snapToEdge(view: View, params: WindowManager.LayoutParams) {
        val metrics = displayMetrics()
        val viewWidth = view.width.takeIf { it > 0 } ?: indicatorSizePx
        val targetX = FloatingSnapCalculator.snap(
            screenWidth = metrics.widthPixels,
            viewWidth = viewWidth,
            currentX = params.x,
            edgeMargin = edgeMarginPx,
        )
        val animator = ValueAnimator.ofInt(params.x, targetX).apply {
            duration = SNAP_DURATION_MS
            addUpdateListener {
                if (rootView == null || rootView !== view) {
                    cancel()
                    return@addUpdateListener
                }
                params.x = it.animatedValue as Int
                try {
                    windowManager.updateViewLayout(view, params)
                } catch (_: IllegalArgumentException) {
                    cancel()
                }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (rootView === view) {
                        SettingsPreferences.setFloatingIndicatorPosition(context, params.x, params.y)
                    }
                }
            })
        }
        snapAnimator = animator
        animator.start()
    }

    private fun displayMetrics(): DisplayMetrics = context.resources.displayMetrics

    private fun dp(value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()

    companion object {
        private const val TAP_THRESHOLD_MS = 250L
        private const val SNAP_DURATION_MS = 200L
        private const val INDICATOR_SIZE_DP = 40
    }
}
