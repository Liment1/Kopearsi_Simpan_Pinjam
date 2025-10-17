package com.example.project_map.ui.loans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R

class TimelineItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private val linePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.grey)
        strokeWidth = 4f
    }
    private val space = 48 // Space between items

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        if (parent.getChildAdapterPosition(view) != (parent.adapter?.itemCount ?: 0) - 1) {
            outRect.bottom = space
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.top - params.topMargin
            val bottom = child.bottom + params.bottomMargin + space

            val icon = child.findViewById<View>(R.id.ivTimelineIcon)
            val lineX = icon.x + icon.width / 2

            // Draw line from bottom of icon to the bottom of the item view
            c.drawLine(lineX, icon.y + icon.height, lineX, bottom.toFloat(), linePaint)
        }
    }
}