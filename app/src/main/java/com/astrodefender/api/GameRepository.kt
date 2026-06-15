package com.astrodefender.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class GameAssets {
    var laserColor: Int = Color.parseColor("#00FFCC")
    var backgroundBitmap: Bitmap? = null
    var audioFile: File? = null
    var shipSkinUrl: String? = null
    var asteroidSkinUrl: String? = null
}

class GameRepository {

    private val api = RetrofitClient.service
    private val assets = GameAssets()

    fun loadEverything(cacheDir: File, onStep: (String) -> Unit, onDone: (GameAssets) -> Unit) {
        onStep("Connecting to Astro Connect...")
        getColor(cacheDir, onStep, onDone)
    }

    private fun getColor(cacheDir: File, onStep: (String) -> Unit, onDone: (GameAssets) -> Unit) {
        api.getLaserColor().enqueue(object : Callback<ColorResponse> {
            override fun onResponse(call: Call<ColorResponse>, response: Response<ColorResponse>) {
                val c = response.body()?.color
                if (c != null) {
                    try {
                        assets.laserColor = Color.parseColor(c)
                    } catch (e: Exception) {
                    }
                }
                onStep("Loading background image...")
                getImage(cacheDir, onStep, onDone)
            }

            override fun onFailure(call: Call<ColorResponse>, t: Throwable) {
                onStep("Loading background image...")
                getImage(cacheDir, onStep, onDone)
            }
        })
    }

    private fun getImage(cacheDir: File, onStep: (String) -> Unit, onDone: (GameAssets) -> Unit) {
        api.getThemeImage().enqueue(object : Callback<okhttp3.ResponseBody> {
            override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {
                val body = response.body()
                if (body != null) {
                    try {
                        val bytes = body.bytes()
                        assets.backgroundBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (e: Exception) {
                    }
                }
                onStep("Loading music...")
                getAudio(cacheDir, onStep, onDone)
            }

            override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {
                onStep("Loading music...")
                getAudio(cacheDir, onStep, onDone)
            }
        })
    }

    private fun getAudio(cacheDir: File, onStep: (String) -> Unit, onDone: (GameAssets) -> Unit) {
        api.getThemeAudio().enqueue(object : Callback<okhttp3.ResponseBody> {
            override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {
                val body = response.body()
                if (body != null) {
                    try {
                        val bytes = body.bytes()
                        val f = File(cacheDir, "theme_audio.mp3")
                        val out = FileOutputStream(f)
                        out.write(bytes)
                        out.close()
                        assets.audioFile = f
                    } catch (e: Exception) {
                    }
                }
                onStep("Loading ship skins...")
                getSkins(onStep, onDone)
            }

            override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {
                onStep("Loading ship skins...")
                getSkins(onStep, onDone)
            }
        })
    }

    private fun getSkins(onStep: (String) -> Unit, onDone: (GameAssets) -> Unit) {
        api.getSkins().enqueue(object : Callback<SkinsResponse> {
            override fun onResponse(call: Call<SkinsResponse>, response: Response<SkinsResponse>) {
                val skins = response.body()
                if (skins != null) {
                    assets.shipSkinUrl = skins.spaceship
                    assets.asteroidSkinUrl = skins.asteroid
                }
                onStep("Done!")
                onDone(assets)
            }

            override fun onFailure(call: Call<SkinsResponse>, t: Throwable) {
                onStep("Done!")
                onDone(assets)
            }
        })
    }
}
