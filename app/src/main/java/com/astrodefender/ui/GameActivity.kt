package com.astrodefender.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.astrodefender.game.GameView
import com.astrodefender.utils.SvgLoader
import java.io.File

class GameActivity : AppCompatActivity() {

    lateinit var gameView: GameView
    var mediaPlayer: MediaPlayer? = null
    var gameOverFired = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        gameView = GameView(this)
        setContentView(gameView)

        setupGame()
    }

    fun setupGame() {
        val assets = AssetHolder.assets ?: return

        gameView.laserColor = assets.laserColor
        gameView.bgBitmap = assets.backgroundBitmap

        if (assets.audioFile != null) {
            startMusic(assets.audioFile!!)
        }

        val skinUrl = assets.asteroidSkinUrl
        if (skinUrl != null) {
            Thread {
                val bmp = SvgLoader.loadFromUrl(skinUrl, 96, 96)
                runOnUiThread {
                    gameView.asteroidBitmap = bmp
                }
            }.start()
        }

        gameView.onGameOver = { finalScore ->
            if (!gameOverFired) {
                gameOverFired = true
                runOnUiThread {
                    stopMusic()
                    val i = android.content.Intent(this, GameOverActivity::class.java)
                    i.putExtra("score", finalScore)
                    startActivity(i)
                    finish()
                }
            }
        }
    }

    fun startMusic(file: File) {
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(file.absolutePath)
            mediaPlayer?.isLooping = true
            mediaPlayer?.prepare()
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying) mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMusic()
    }
}
