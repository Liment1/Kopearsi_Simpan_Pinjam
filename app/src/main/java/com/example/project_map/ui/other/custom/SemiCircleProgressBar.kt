package com.example.project_map.ui.other.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class SemiCircleProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()
    private var percent = 0

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 20f // Adjust thickness
        paint.strokeCap = Paint.Cap.ROUND
    }

    // This is the function that was missing!
    fun setPercent(progress: Int) {
        this.percent = progress.coerceIn(0, 100)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        rectF.set(20f, 20f, w - 20f, (h * 2) - 20f)

        paint.color = Color.LTGRAY
        canvas.drawArc(rectF, 180f, 180f, false, paint)


        paint.color = if (percent > 70) Color.parseColor("#43A047") else Color.parseColor("#F57C00")
        val sweepAngle = (percent / 100f) * 180f
        canvas.drawArc(rectF, 180f, sweepAngle, false, paint)
    }
}