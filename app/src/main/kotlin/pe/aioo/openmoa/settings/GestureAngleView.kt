package pe.aioo.openmoa.settings

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class GestureAngleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val angles = IntArray(8)

    private var cx = 0f
    private var cy = 0f
    private var pinR = 0f
    private var labelR = 0f
    private var angleTextR = 0f
    private var pinHitR = 0f
    private var pinOuterR = 0f
    private var pinInnerR = 0f
    private var draggingIndex = -1

    var onAnglesChanged: ((IntArray) -> Unit)? = null

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CC3333")
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }

    private val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#DD4444")
        style = Paint.Style.FILL
    }

    private val pinInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val sectorTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    private val angleTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333")
        textAlign = Paint.Align.CENTER
    }

    private data class Sector(val vowel: String, val startPin: Int, val endPin: Int, val wraps: Boolean)

    // 0°=왼쪽, 90°=위, 180°=오른쪽, 270°=아래 기준 좌표계
    // ㅓ 섹터가 0° 근방을 감싸는 wrap-around 섹터
    private val sectors = listOf(
        Sector("어", 7, 0, true),
        Sector("이", 0, 1, false),
        Sector("오", 1, 2, false),
        Sector("이", 2, 3, false),
        Sector("아", 3, 4, false),
        Sector("으", 4, 5, false),
        Sector("우", 5, 6, false),
        Sector("으", 6, 7, false),
    )

    fun setAngles(newAngles: IntArray) {
        newAngles.copyInto(angles)
        invalidate()
    }

    fun getAngles(): IntArray = angles.copyOf()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        cx = w / 2f
        cy = h / 2f
        val base = min(w, h) / 2f * 0.88f
        pinR = base * 0.76f
        labelR = base * 0.40f
        angleTextR = base * 0.98f
        pinHitR = dpToPx(30f)
        pinOuterR = dpToPx(13f)
        pinInnerR = dpToPx(6f)
        sectorTextPaint.textSize = min(w, h) * 0.042f
        angleTextPaint.textSize = min(w, h) * 0.030f
        linePaint.pathEffect = DashPathEffect(floatArrayOf(pinR * 0.09f, pinR * 0.07f), 0f)
    }

    override fun onDraw(canvas: Canvas) {
        for (i in 0 until 8) drawBoundaryLine(canvas, i)
        drawSectorLabels(canvas)
        for (i in 0 until 8) drawPin(canvas, i)
    }

    // 좌표계: pinX = cx - cos(angle)*R, pinY = cy - sin(angle)*R
    // → 0°=왼쪽(9시), 90°=위(12시), 180°=오른쪽(3시), 270°=아래(6시)
    private fun pinX(i: Int): Float = (cx - pinR * cos(toRad(angles[i]))).toFloat()
    private fun pinY(i: Int): Float = (cy - pinR * sin(toRad(angles[i]))).toFloat()

    private fun drawBoundaryLine(canvas: Canvas, i: Int) {
        canvas.drawLine(cx, cy, pinX(i), pinY(i), linePaint)
        val ax = cx - angleTextR * cos(toRad(angles[i]))
        val ay = cy - angleTextR * sin(toRad(angles[i]))
        canvas.drawText("${angles[i]}°", ax.toFloat(), ay.toFloat() + angleTextPaint.textSize * 0.4f, angleTextPaint)
    }

    private fun drawPin(canvas: Canvas, i: Int) {
        val px = pinX(i)
        val py = pinY(i)
        canvas.drawCircle(px, py, pinOuterR, pinPaint)
        canvas.drawCircle(px, py, pinInnerR, pinInnerPaint)
    }

    private fun drawSectorLabels(canvas: Canvas) {
        for (sector in sectors) {
            val start = angles[sector.startPin].toFloat()
            val end = angles[sector.endPin].toFloat()
            val (width, mid) = if (sector.wraps) {
                val w = (angles[sector.endPin] - angles[sector.startPin] + 360) % 360
                val m = (angles[sector.startPin] + w / 2f + 360f) % 360f
                Pair(w, m)
            } else {
                val w = (end - start).toInt().coerceAtLeast(0)
                Pair(w, (start + end) / 2f)
            }
            if (width == 0) continue
            val lx = (cx - labelR * cos(toRad(mid))).toFloat()
            val ly = (cy - labelR * sin(toRad(mid))).toFloat()
            val lineH = sectorTextPaint.textSize
            canvas.drawText(sector.vowel, lx, ly - lineH * 0.2f, sectorTextPaint)
            canvas.drawText("(${width}°)", lx, ly + lineH * 0.9f, sectorTextPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                draggingIndex = findNearestPin(event.x, event.y)
                return draggingIndex >= 0
            }
            MotionEvent.ACTION_MOVE -> {
                if (draggingIndex < 0) return false
                updateAngle(draggingIndex, event.x, event.y)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val wasDragging = draggingIndex >= 0
                draggingIndex = -1
                return wasDragging
            }
        }
        return false
    }

    private fun findNearestPin(tx: Float, ty: Float): Int {
        for (i in 0 until 8) {
            val dx = tx - pinX(i)
            val dy = ty - pinY(i)
            if (sqrt(dx.pow(2) + dy.pow(2)) <= pinHitR) return i
        }
        return -1
    }

    private fun updateAngle(index: Int, tx: Float, ty: Float) {
        // 좌표계에 맞게 atan2(cy-ty, cx-tx) 사용
        val dx = cx - tx
        val dy = cy - ty
        if (sqrt(dx.pow(2) + dy.pow(2)) < dpToPx(8f)) return
        var newAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toInt()
        if (newAngle < 0) newAngle += 360
        newAngle = clampAngle(index, newAngle)
        if (angles[index] != newAngle) {
            angles[index] = newAngle
            invalidate()
            onAnglesChanged?.invoke(angles.copyOf())
        }
    }

    private fun clampAngle(i: Int, v: Int): Int = when (i) {
        0 -> if (v > 180) angles[0] else v.coerceIn(0, maxOf(0, angles[1]))
        1 -> v.coerceIn(minOf(angles[0], angles[2]), maxOf(angles[0], angles[2]))
        2 -> v.coerceIn(minOf(angles[1], angles[3]), maxOf(angles[1], angles[3]))
        3 -> v.coerceIn(minOf(angles[2], 180), 180)
        4 -> v.coerceIn(181, maxOf(181, angles[5]))
        5 -> v.coerceIn(minOf(angles[4], angles[6]), maxOf(angles[4], angles[6]))
        6 -> v.coerceIn(minOf(angles[5], angles[7]), maxOf(angles[5], angles[7]))
        7 -> if (v < 181) angles[7] else v.coerceIn(maxOf(181, minOf(angles[6], 359)), 359)
        else -> v
    }

    private fun toRad(deg: Number): Double = Math.toRadians(deg.toDouble())
    private fun dpToPx(dp: Float): Float = dp * resources.displayMetrics.density
}
