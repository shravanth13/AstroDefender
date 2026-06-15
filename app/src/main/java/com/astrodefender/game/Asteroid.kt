package com.astrodefender.game

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class AsteroidSize { LARGE, SMALL }

class Asteroid(
    var pos: Vec2,
    val vel: Vec2,
    val size: AsteroidSize,
    var rotation: Float = 0f,
    val rotSpeed: Float = Random.nextFloat() * 80f - 40f,
    private var svgBitmap: Bitmap? = null
) {
    var alive = true

    val radius = when (size) {
        AsteroidSize.LARGE -> 42f
        AsteroidSize.SMALL -> 22f
    }

    private val ptCount = if (size == AsteroidSize.LARGE) 9 else 7
    private val pts = FloatArray(ptCount * 2)

    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 1.8f
    }

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mtx = Matrix()

    init {
        for (i in 0 until ptCount) {
            val angle = (2 * Math.PI * i / ptCount).toFloat()
            val jitter = radius * (0.72f + Random.nextFloat() * 0.28f)
            pts[i * 2] = cos(angle) * jitter
            pts[i * 2 + 1] = sin(angle) * jitter
        }
    }

    fun setSvgBitmap(bmp: Bitmap?) {
        svgBitmap = bmp
    }

    fun draw(canvas: Canvas) {
        canvas.save()
        canvas.translate(pos.x, pos.y)
        canvas.rotate(rotation)

        if (svgBitmap != null) {
            val bmp = svgBitmap!!
            val s = (radius * 2f) / bmp.width.toFloat()
            mtx.reset()
            mtx.setScale(s, s)
            mtx.postTranslate(-radius, -radius)
            canvas.drawBitmap(bmp, mtx, bitmapPaint)
        } else {
            val path = Path()
            path.moveTo(pts[0], pts[1])
            for (i in 1 until ptCount) {
                path.lineTo(pts[i * 2], pts[i * 2 + 1])
            }
            path.close()
            canvas.drawPath(path, outlinePaint)
        }

        canvas.restore()
    }
}
