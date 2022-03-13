package com.ovs.puzzlegame

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


class TouchListener(activity: PuzzleActivity): View.OnTouchListener {

    private var xDelta = 0f
    private var yDelta = 0f
    private val puzzleActivity: PuzzleActivity? = activity

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {

        val x: Float = motionEvent!!.rawX
        val y: Float = motionEvent!!.rawY
        val tolerance: Double = sqrt(
            view!!.width.toDouble().pow(2.0) + view!!.height.toDouble().pow(2.0)
        ) / 10

        val piece = view as PuzzlePiece
        if (!piece.canMove) {
            return true
        }

        val lParams = view!!.layoutParams as RelativeLayout.LayoutParams

        when (motionEvent!!.action and MotionEvent.ACTION_MASK) {

            MotionEvent.ACTION_DOWN -> {
                xDelta = x!! - lParams.leftMargin
                yDelta = y!! - lParams.topMargin
                piece.bringToFront()
            }

            MotionEvent.ACTION_MOVE -> {
                lParams.leftMargin = (x!! - xDelta).toInt()
                lParams.topMargin = (y!! - yDelta).toInt()
                view.layoutParams = lParams
            }

            MotionEvent.ACTION_UP -> {
                val xDiff: Int = abs(piece.xCord - lParams.leftMargin)
                val yDiff: Int = abs(piece.yCord - lParams.topMargin)
                if (xDiff <= tolerance && yDiff <= tolerance) {
                    lParams.leftMargin = piece.xCord
                    lParams.topMargin = piece.yCord
                    piece.layoutParams = lParams
                    piece.canMove = false
                    sendViewToBack(piece)
                    puzzleActivity!!.checkGameOver();
                }
            }
        }

        return true
    }

    private fun sendViewToBack(child: View) {
        val parent = child.parent as ViewGroup
        if (null != parent) {
            parent.removeView(child)
            parent.addView(child, 0)
        }
    }
}