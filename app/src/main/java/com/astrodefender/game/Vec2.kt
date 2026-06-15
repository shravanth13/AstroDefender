package com.astrodefender.game

import kotlin.math.sqrt

data class Vec2(var x: Float, var y: Float) {

    operator fun plus(o: Vec2) = Vec2(x + o.x, y + o.y)
    operator fun minus(o: Vec2) = Vec2(x - o.x, y - o.y)
    operator fun times(s: Float) = Vec2(x * s, y * s)

    fun len() = sqrt(x * x + y * y)

    fun normalize(): Vec2 {
        val l = len()
        return if (l == 0f) Vec2(0f, 0f) else Vec2(x / l, y / l)
    }

    fun copy() = Vec2(x, y)
}
