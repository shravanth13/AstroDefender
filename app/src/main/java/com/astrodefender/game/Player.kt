package com.astrodefender.game

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path

class Player(var pos: Vec2, var angle: Float = -90f) {

    val radius = 20f
    var alive = true
    private var svgBitmap: Bitmap? = null
    private var thrustAnim = 0f
    private var thrustDir = 1f

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mtx = Matrix()

    fun setSvgBitmap(bmp: Bitmap?) {
        svgBitmap = bmp
    }

    fun update(dt: Float, thrusting: Boolean) {
        if (thrusting) {
            thrustAnim += dt * 8f * thrustDir
            if (thrustAnim > 1f) { thrustAnim = 1f; thrustDir = -1f }
            if (thrustAnim < 0f) { thrustAnim = 0f; thrustDir = 1f }
        }
    }

    fun draw(canvas: Canvas, thrusting: Boolean) {
        canvas.save()
        canvas.translate(pos.x, pos.y)
        canvas.rotate(angle + 90f)

        if (svgBitmap != null) {
            val bmp = svgBitmap!!
            val s = (radius * 2.2f) / bmp.width.toFloat()
            mtx.reset()
            mtx.setScale(s, s)
            mtx.postTranslate(-radius * 1.1f, -radius * 1.1f)
            canvas.drawBitmap(bmp, mtx, bitmapPaint)
        }

        canvas.restore()
    }

    fun forwardVec(): Vec2 {
        val rad = Math.toRadians(angle.toDouble()).toFloat()
        return Vec2(kotlin.math.cos(rad), kotlin.math.sin(rad))
    }
}
