package com.example.battleshipgame.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.battleshipgame.R


class BattleView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val gSize = 10
    private var withShips: Boolean = false
    var shipRects = mutableListOf<RectF>()
    var cells = Array(10) { Array(10) { Cell() } }
    var cellWidth: Float = 0.0F
    var cellHeight: Float = 0.0F
    private val gridPaint = Paint()
    private val shipPaint = Paint()
    private val hitPaint = Paint()
    private val missPaint = Paint()

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BattleView)
        withShips = a.getBoolean(R.styleable.BattleView_withShips, false)

        gridPaint.color = ContextCompat.getColor(context, R.color.purple_500)
        gridPaint.isAntiAlias = true
        gridPaint.style = Paint.Style.STROKE
        gridPaint.strokeWidth = resources.displayMetrics.density * 2

        shipPaint.color = ContextCompat.getColor(context, R.color.purple_700)
        shipPaint.isAntiAlias = true
        shipPaint.style = Paint.Style.FILL
        shipPaint.strokeWidth = resources.displayMetrics.density * 2

        hitPaint.color = ContextCompat.getColor(context, R.color.red)
        hitPaint.isAntiAlias = true
        hitPaint.style = Paint.Style.FILL
        hitPaint.strokeWidth = resources.displayMetrics.density * 2

        missPaint.color = ContextCompat.getColor(context, R.color.blue)
        missPaint.isAntiAlias = true
        missPaint.style = Paint.Style.FILL
        missPaint.strokeWidth = resources.displayMetrics.density * 4
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawGrid(canvas)
        if (withShips) {
            drawShips(canvas)
        }
        drawCells(canvas)
    }

    private fun drawGrid(canvas: Canvas) {
        cellWidth = width.toFloat() / gSize
        cellHeight = height.toFloat() / gSize

        for (i in 0..gSize) {
            canvas.drawLine(i * cellWidth, 0F, i * cellWidth, height.toFloat(), gridPaint)
        }

        for (i in 0..gSize) {
            canvas.drawLine(0F, i * cellHeight, width.toFloat(), i * cellHeight, gridPaint)
        }
    }

    fun addShip(i: Int, j: Int, orientation: Orientation, rank: Int) {
        val left = (i + 0.25F) * cellWidth
        val top = (j + 0.25F) * cellHeight
        val right: Float
        val bottom: Float

        when (orientation) {
            Orientation.HORIZONTAL -> {
                right = (i + rank - 1 + 0.75F) * cellWidth
                bottom = (j + 0.75F) * cellHeight
            }
            else -> {
                right = (i + 0.75F) * cellWidth
                bottom = (j + rank - 1 + 0.75F) * cellHeight
            }
        }
        shipRects.add(RectF(left, top, right, bottom))
        refreshCanvas()
    }

    private fun drawShips(canvas: Canvas) {
        for (rect in shipRects) {
            canvas.drawRect(rect, shipPaint)
        }
    }

    private fun drawCells(canvas: Canvas) {
        for(i in 0 until 10) {
            for (j in 0 until 10) {
                when (cells[i][j].state) {
                    CellState.MISS -> {
                        drawMiss(canvas, i, j)
                    }
                    CellState.HIT -> {
                        drawHit(canvas, i, j)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun drawMiss(canvas: Canvas, i: Int, j: Int) {
        cellWidth = width.toFloat() / gSize
        cellHeight = height.toFloat() / gSize

        canvas.drawCircle((i + 0.5F) * cellWidth, (j + 0.5F) * cellHeight, 0.3F * cellWidth, missPaint)
    }

    private fun drawHit(canvas: Canvas, i: Int, j: Int) {
        cellWidth = width.toFloat() / gSize
        cellHeight = height.toFloat() / gSize

        canvas.drawLine((i + 0.2F) * cellWidth, (j + 0.2F) * cellHeight,
            (i + 0.8F) * cellWidth, (j + 0.8F) * cellHeight, hitPaint)

        canvas.drawLine((i + 0.8F) * cellWidth, (j + 0.2F) * cellHeight,
            (i + 0.2F) * cellWidth, (j + 0.8F) * cellHeight, hitPaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        performClick()
        return true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    fun refreshCanvas() = invalidate()
}