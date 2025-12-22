package com.example.project_map.ui.other.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class SemiCircleProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()
    private var percent = 0

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 30f // Increased slightly for a better "rounded" look
        paint.strokeCap = Paint.Cap.ROUND // Keeps the ends rounded
    }

    // FIX 1: Renamed to match LoanFormFragment call
    fun setPercentage(progress: Int) {
        this.percent = progress.coerceIn(0, 100)
        invalidate() // Redraw the view
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = 20f
        val w = width.toFloat()
        val h = height.toFloat()

        // FIX 2: Force Perfect Circle Aspect Ratio
        // We calculate the maximum radius that fits in the view dimensions
        // Radius is constrained by either the full Height or half the Width.
        val maxRadiusByHeight = h - padding
        val maxRadiusByWidth = (w - (2 * padding)) / 2

        val radius = min(maxRadiusByHeight, maxRadiusByWidth)

        // Center the arc horizontally
        val centerX = w / 2
        // Position the arc so the bottom sits at the view's bottom (minus padding)
        val centerY = h - padding / 2

        rectF.set(
            centerX - radius, // Left
            centerY - radius, // Top
            centerX + radius, // Right
            centerY + radius  // Bottom
        )

        // Draw Background Track (Gray)
        paint.color = Color.LTGRAY
        canvas.drawArc(rectF, 180f, 180f, false, paint)

        // Draw Progress (Green/Orange)
        paint.color = if (percent > 70) Color.parseColor("#43A047") else Color.parseColor("#F57C00")
        val sweepAngle = (percent / 100f) * 180f
        canvas.drawArc(rectF, 180f, sweepAngle, false, paint)
    }
}