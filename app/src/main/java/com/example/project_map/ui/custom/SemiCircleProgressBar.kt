package com.example.project_map.ui.custom

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

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 100f)
            invalidate()
        }

    init {
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.strokeWidth = 40f
        backgroundPaint.color = Color.parseColor("#E0E0E0") // Light Gray
        backgroundPaint.strokeCap = Paint.Cap.ROUND

        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeWidth = 40f
        progressPaint.color = Color.parseColor("#4CAF50") // Green
        progressPaint.strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val radius = min(width, height * 2) / 2 - 40f // Padding

        val centerX = width / 2
        val centerY = height - 40f // Align to bottom

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        // Draw Background Arch (180 degrees)
        canvas.drawArc(rectF, 180f, 180f, false, backgroundPaint)

        // Draw Progress Arch
        val sweepAngle = (progress / 100f) * 180f

        // Dynamic Color based on score
        progressPaint.color = when {
            progress < 50 -> Color.RED
            progress < 75 -> Color.parseColor("#FFC107") // Amber
            else -> Color.parseColor("#4CAF50") // Green
        }

        canvas.drawArc(rectF, 180f, sweepAngle, false, progressPaint)
    }
}