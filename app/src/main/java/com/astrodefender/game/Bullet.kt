package com.astrodefender.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Bullet(
    var pos: Vec2,
    val vel: Vec2,
    var color: Int = Color.parseColor("#00FFCC")
) {
    var alive = true

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    fun update(dt: Float) {
        pos.x += vel.x * dt
        pos.y += vel.y * dt
    }

    fun isOffScreen(w: Int, h: Int): Boolean {
        return pos.x < -20 || pos.x > w + 20 || pos.y < -20 || pos.y > h + 20
    }

    fun draw(canvas: Canvas) {
        paint.color = color
        canvas.drawCircle(pos.x, pos.y, 5f, paint)
    }
}
