package com.astrodefender.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    var thread: Thread? = null
    var running = false

    var screenW = 0
    var screenH = 0

    var player: Player? = null

    var asteroidList = mutableListOf<Asteroid>()
    var bulletList = mutableListOf<Bullet>()

    var particleX = mutableListOf<Float>()
    var particleY = mutableListOf<Float>()
    var particleVX = mutableListOf<Float>()
    var particleVY = mutableListOf<Float>()
    var particleLife = mutableListOf<Float>()

    var score = 0
    var lives = 3
    var wave = 1
    var gameOver = false
    var paused = false
    var gameTimeSeconds = 0f
    var asteroidCount = 0

    var laserColor = Color.WHITE
    var bgBitmap: Bitmap? = null
    var asteroidBitmap: Bitmap? = null

    var onGameOver: ((score: Int) -> Unit)? = null

    var joyCX = 0f
    var joyCY = 0f
    var joyR = 80f
    var joyThumbX = 0f
    var joyThumbY = 0f
    var joyActive = false
    var joyPointerId = -1
    var joyDX = 0f
    var joyMag = 0f

    var fireRect = RectF()
    var firePointerId = -1
    var firing = false
    var fireR = 50f

    var fireCooldown = 0f
    var hitFlashTimer = 0f
    var blinkOn = false
    var blinkTimer = 0f

    val bgPaint = Paint()

    val hudLabelPaint = Paint().apply {
        color = Color.parseColor("#888888")
        textSize = 22f
        isAntiAlias = true
        letterSpacing = 0.15f
    }
    val hudValuePaint = Paint().apply {
        color = Color.WHITE
        textSize = 34f
        isAntiAlias = true
        typeface = Typeface.MONOSPACE
    }
    val hudTimerPaint = Paint().apply {
        color = Color.WHITE
        textSize = 38f
        isAntiAlias = true
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.CENTER
    }
    val hudRightPaint = Paint().apply {
        color = Color.WHITE
        textSize = 34f
        isAntiAlias = true
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.RIGHT
    }
    val hudRightLabelPaint = Paint().apply {
        color = Color.parseColor("#888888")
        textSize = 22f
        isAntiAlias = true
        letterSpacing = 0.15f
        textAlign = Paint.Align.RIGHT
    }
    val hudWavePaint = Paint().apply {
        color = Color.parseColor("#888888")
        textSize = 20f
        isAntiAlias = true
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.1f
    }

    val joyOuterPaint = Paint().apply {
        color = Color.argb(40, 255, 255, 255)
        style = Paint.Style.FILL
    }
    val joyBorderPaint = Paint().apply {
        color = Color.argb(120, 255, 255, 255)
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
        isAntiAlias = true
    }
    val joyInnerPaint = Paint().apply {
        color = Color.argb(60, 255, 255, 255)
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    val joyInnerBorderPaint = Paint().apply {
        color = Color.argb(180, 255, 255, 255)
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
        isAntiAlias = true
    }

    val fireBorderPaint = Paint().apply {
        color = Color.argb(160, 255, 255, 255)
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
        isAntiAlias = true
    }
    val fireFillPaint = Paint().apply {
        color = Color.argb(40, 255, 255, 255)
        style = Paint.Style.FILL
    }
    val fireTextPaint = Paint().apply {
        color = Color.argb(180, 255, 255, 255)
        textSize = 20f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        letterSpacing = 0.15f
    }

    val starPaint = Paint().apply {
        color = Color.argb(160, 255, 255, 255)
        style = Paint.Style.FILL
    }

    val starsX = FloatArray(80) { (Math.random() * 2000).toFloat() }
    val starsY = FloatArray(80) { (Math.random() * 1500).toFloat() }
    val starSizes = FloatArray(80) { (Math.random() * 1.5f + 0.5f).toFloat() }

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        screenW = width
        screenH = height

        player = Player(Vec2(screenW / 2f, screenH / 2f))

        joyCX = joyR + 48f
        joyCY = screenH - joyR - 48f
        joyThumbX = joyCX
        joyThumbY = joyCY

        val bx = screenW - fireR - 48f
        val by = screenH - fireR - 48f
        fireRect = RectF(bx - fireR, by - fireR, bx + fireR, by + fireR)

        spawnWave()

        running = true
        thread = Thread { gameLoop() }
        thread?.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        try { thread?.join() } catch (e: InterruptedException) { }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) { }

    fun gameLoop() {
        var lastTime = System.currentTimeMillis()
        while (running) {
            val now = System.currentTimeMillis()
            var dt = (now - lastTime) / 1000f
            lastTime = now
            if (dt > 0.05f) dt = 0.05f

            val canvas = holder.lockCanvas()
            if (canvas != null) {
                try {
                    update(dt)
                    drawGame(canvas)
                } finally {
                    holder.unlockCanvasAndPost(canvas)
                }
            }

            val frameTime = System.currentTimeMillis() - now
            val sleepTime = 16 - frameTime
            if (sleepTime > 0) {
                try { Thread.sleep(sleepTime) } catch (e: InterruptedException) { }
            }
        }
    }

    fun update(dt: Float) {
        if (gameOver || paused) return
        val p = player ?: return

        gameTimeSeconds += dt
        asteroidCount = asteroidList.size

        if (joyActive && joyMag > 0.15f) {
            p.angle += joyDX * 180f * dt
        }

        val thrusting = joyActive && joyMag > 0.2f
        p.update(dt, thrusting)

        if (hitFlashTimer > 0f) {
            hitFlashTimer -= dt
            blinkTimer += dt
            if (blinkTimer > 0.12f) {
                blinkTimer = 0f
                blinkOn = !blinkOn
            }
        } else {
            blinkOn = false
        }

        fireCooldown -= dt
        if (firing && fireCooldown <= 0f) {
            fireCooldown = 0.18f
            shootBullet()
        }

        val bulletIter = bulletList.iterator()
        while (bulletIter.hasNext()) {
            val b = bulletIter.next()
            b.pos.x += b.vel.x * dt
            b.pos.y += b.vel.y * dt
            if (b.pos.x < -20 || b.pos.x > screenW + 20 || b.pos.y < -20 || b.pos.y > screenH + 20) {
                bulletIter.remove()
            }
        }

        for (a in asteroidList) {
            a.pos.x += a.vel.x * dt
            a.pos.y += a.vel.y * dt
            a.rotation += a.rotSpeed * dt
            val margin = a.radius + 10f
            if (a.pos.x < -margin) a.pos.x = screenW + margin
            if (a.pos.x > screenW + margin) a.pos.x = -margin
            if (a.pos.y < -margin) a.pos.y = screenH + margin
            if (a.pos.y > screenH + margin) a.pos.y = -margin
        }

        for (i in particleLife.indices.reversed()) {
            particleX[i] += particleVX[i] * dt
            particleY[i] += particleVY[i] * dt
            particleLife[i] -= dt
            if (particleLife[i] <= 0f) {
                particleX.removeAt(i)
                particleY.removeAt(i)
                particleVX.removeAt(i)
                particleVY.removeAt(i)
                particleLife.removeAt(i)
            }
        }

        checkBulletHits()
        checkPlayerHit()

        if (asteroidList.isEmpty()) {
            wave++
            spawnWave()
        }
    }

    fun shootBullet() {
        val p = player ?: return
        val rad = Math.toRadians(p.angle.toDouble())
        val dirX = cos(rad).toFloat()
        val dirY = sin(rad).toFloat()
        val b = Bullet(Vec2(p.pos.x + dirX * 24f, p.pos.y + dirY * 24f), Vec2(dirX * 520f, dirY * 520f), laserColor)
        bulletList.add(b)
    }

    fun checkBulletHits() {
        val bulletsToRemove = mutableListOf<Bullet>()
        val asteroidsToRemove = mutableListOf<Asteroid>()
        val newAsteroids = mutableListOf<Asteroid>()

        for (b in bulletList) {
            for (a in asteroidList) {
                if (asteroidsToRemove.contains(a)) continue
                val dist = hypot(b.pos.x - a.pos.x, b.pos.y - a.pos.y)
                if (dist < a.radius + 4f) {
                    bulletsToRemove.add(b)
                    asteroidsToRemove.add(a)
                    if (a.size == AsteroidSize.LARGE) {
                        score += 20
                        spawnExplosion(a.pos.x, a.pos.y, 16)
                        val speed = a.vel.len() * 1.4f + 30f
                        for (j in 0..1) {
                            val ang = Random.nextFloat() * 2f * Math.PI.toFloat()
                            val na = Asteroid(Vec2(a.pos.x, a.pos.y), Vec2(cos(ang) * speed, sin(ang) * speed), AsteroidSize.SMALL)
                            na.setSvgBitmap(asteroidBitmap)
                            newAsteroids.add(na)
                        }
                    } else {
                        score += 50
                        spawnExplosion(a.pos.x, a.pos.y, 10)
                    }
                    break
                }
            }
        }

        bulletList.removeAll(bulletsToRemove)
        asteroidList.removeAll(asteroidsToRemove)
        asteroidList.addAll(newAsteroids)
    }

    fun checkPlayerHit() {
        if (hitFlashTimer > 0f) return
        val p = player ?: return
        for (a in asteroidList) {
            val dist = hypot(p.pos.x - a.pos.x, p.pos.y - a.pos.y)
            if (dist < a.radius + p.radius * 0.75f) {
                lives--
                spawnExplosion(p.pos.x, p.pos.y, 24)
                if (lives <= 0) {
                    gameOver = true
                    onGameOver?.invoke(score)
                } else {
                    p.pos.x = screenW / 2f
                    p.pos.y = screenH / 2f
                    p.angle = -90f
                    hitFlashTimer = 2.5f
                    val toRemove = asteroidList.filter { a2 ->
                        hypot(a2.pos.x - screenW / 2f, a2.pos.y - screenH / 2f) < 120f
                    }
                    asteroidList.removeAll(toRemove)
                }
                break
            }
        }
    }

    fun spawnExplosion(x: Float, y: Float, count: Int) {
        for (i in 0 until count) {
            val ang = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 60f + Random.nextFloat() * 120f
            particleX.add(x)
            particleY.add(y)
            particleVX.add(cos(ang) * speed)
            particleVY.add(sin(ang) * speed)
            particleLife.add(0.3f + Random.nextFloat() * 0.4f)
        }
    }

    fun spawnWave() {
        val count = 3 + wave * 2
        for (i in 0 until count) {
            spawnAsteroid()
        }
    }

    fun spawnAsteroid() {
        val edge = Random.nextInt(4)
        val pos: Vec2 = when (edge) {
            0 -> Vec2(Random.nextFloat() * screenW, -50f)
            1 -> Vec2(Random.nextFloat() * screenW, screenH + 50f)
            2 -> Vec2(-50f, Random.nextFloat() * screenH)
            else -> Vec2(screenW + 50f, Random.nextFloat() * screenH)
        }
        val speed = 55f + Random.nextFloat() * 65f + wave * 5f
        val ang = Random.nextFloat() * 2f * Math.PI.toFloat()
        val a = Asteroid(pos, Vec2(cos(ang) * speed, sin(ang) * speed), AsteroidSize.LARGE)
        a.setSvgBitmap(asteroidBitmap)
        asteroidList.add(a)
    }

    fun drawGame(canvas: Canvas) {
        val bmp = bgBitmap
        if (bmp != null) {
            val scale = maxOf(screenW.toFloat() / bmp.width, screenH.toFloat() / bmp.height)
            val m = android.graphics.Matrix()
            m.setScale(scale, scale)
            canvas.drawBitmap(bmp, m, Paint())
            bgPaint.color = Color.argb(100, 0, 0, 0)
            canvas.drawRect(0f, 0f, screenW.toFloat(), screenH.toFloat(), bgPaint)
        } else {
            bgPaint.color = Color.BLACK
            canvas.drawRect(0f, 0f, screenW.toFloat(), screenH.toFloat(), bgPaint)
            for (i in starsX.indices) {
                starPaint.alpha = (80 + (i * 3) % 80)
                canvas.drawCircle(starsX[i] % screenW, starsY[i] % screenH, starSizes[i], starPaint)
            }
        }

        for (i in particleLife.indices) {
            val life = particleLife[i]
            val alpha = (life / 0.5f * 200).toInt().coerceIn(0, 200)
            val pp = Paint()
            pp.color = Color.argb(alpha, 255, 255, 255)
            canvas.drawCircle(particleX[i], particleY[i], 2.5f * life, pp)
        }

        for (a in asteroidList) {
            a.draw(canvas)
        }

        for (b in bulletList) {
            val bp = Paint()
            bp.color = b.color
            bp.isAntiAlias = true
            canvas.drawCircle(b.pos.x, b.pos.y, 4f, bp)
        }

        if (!blinkOn) {
            drawShip(canvas)
        }

        drawHud(canvas)
        drawJoystick(canvas)
        drawFireButton(canvas)
    }

    fun drawShip(canvas: Canvas) {
        val p = player ?: return
        canvas.save()
        canvas.translate(p.pos.x, p.pos.y)
        canvas.rotate(p.angle + 90f)

        val shipPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        val path = Path()
        path.moveTo(0f, -18f)
        path.lineTo(12f, 10f)
        path.lineTo(-12f, 10f)
        path.close()
        canvas.drawPath(path, shipPaint)
        canvas.restore()
    }

    fun drawHud(canvas: Canvas) {
        val topPad = 28f

        canvas.drawText("SCORE", 24f, topPad + 18f, hudLabelPaint)
        canvas.drawText(String.format("%06d", score), 24f, topPad + 52f, hudValuePaint)

        val cx = screenW / 2f
        val totalSecs = gameTimeSeconds.toInt()
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        val millis = ((gameTimeSeconds - totalSecs) * 100).toInt()
        val timeStr = String.format("%02d:%02d:%02d", mins, secs, millis)
        canvas.drawText(timeStr, cx, topPad + 46f, hudTimerPaint)
        canvas.drawText("WAVE ${String.format("%02d", wave)}", cx, topPad + 70f, hudWavePaint)

        canvas.drawText("ASTEROIDS", screenW - 24f, topPad + 18f, hudRightLabelPaint)
        canvas.drawText("${asteroidList.size}", screenW - 24f, topPad + 52f, hudRightPaint)
    }

    fun drawJoystick(canvas: Canvas) {
        canvas.drawCircle(joyCX, joyCY, joyR, joyOuterPaint)
        canvas.drawCircle(joyCX, joyCY, joyR, joyBorderPaint)
        canvas.drawCircle(joyThumbX, joyThumbY, joyR * 0.35f, joyInnerPaint)
        canvas.drawCircle(joyThumbX, joyThumbY, joyR * 0.35f, joyInnerBorderPaint)
    }

    fun drawFireButton(canvas: Canvas) {
        val cx = fireRect.centerX()
        val cy = fireRect.centerY()
        canvas.drawCircle(cx, cy, fireR, fireFillPaint)
        canvas.drawCircle(cx, cy, fireR, fireBorderPaint)
        canvas.drawText("FIRE", cx, cy + 8f, fireTextPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val idx = event.actionIndex
        val id = event.getPointerId(idx)
        val x = event.getX(idx)
        val y = event.getY(idx)

        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val fcx = fireRect.centerX()
                val fcy = fireRect.centerY()
                if (hypot(x - fcx, y - fcy) < fireR + 20f) {
                    firing = true
                    firePointerId = id
                    return true
                }
                if (hypot(x - joyCX, y - joyCY) < joyR * 1.4f) {
                    joyPointerId = id
                    joyActive = true
                    updateJoystick(x, y)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val pid = event.getPointerId(i)
                    if (pid == joyPointerId) {
                        updateJoystick(event.getX(i), event.getY(i))
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if (id == firePointerId) { firing = false; firePointerId = -1 }
                if (id == joyPointerId) {
                    joyPointerId = -1; joyActive = false
                    joyThumbX = joyCX; joyThumbY = joyCY
                    joyDX = 0f; joyMag = 0f
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                firing = false; firePointerId = -1
                joyPointerId = -1; joyActive = false
                joyThumbX = joyCX; joyThumbY = joyCY
                joyDX = 0f; joyMag = 0f
            }
        }
        return true
    }

    fun updateJoystick(x: Float, y: Float) {
        val dx = x - joyCX
        val dy = y - joyCY
        val dist = hypot(dx, dy)
        joyMag = (dist / joyR).coerceAtMost(1f)
        if (dist > joyR) {
            joyThumbX = joyCX + dx / dist * joyR
            joyThumbY = joyCY + dy / dist * joyR
        } else {
            joyThumbX = x
            joyThumbY = y
        }
        joyDX = (joyThumbX - joyCX) / joyR
    }
}
