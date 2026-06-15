package com.astrodefender.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.astrodefender.api.GameAssets
import com.astrodefender.api.GameRepository
import com.astrodefender.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    lateinit var binding: ActivitySplashBinding
    val repo = GameRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo.loadEverything(
            cacheDir,
            onStep = { msg ->
                runOnUiThread {
                    binding.tvStatus.text = msg
                }
            },
            onDone = { assets ->
                runOnUiThread {
                    goToMainMenu(assets)
                }
            }
        )
    }

    fun goToMainMenu(assets: GameAssets) {
        AssetHolder.assets = assets
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

object AssetHolder {
    var assets: GameAssets? = null
}
