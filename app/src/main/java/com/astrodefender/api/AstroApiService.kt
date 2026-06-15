package com.astrodefender.api

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

data class ColorResponse(
    @SerializedName("color") val color: String
)

data class SkinsResponse(
    @SerializedName("spaceship") val spaceship: String,
    @SerializedName("asteroid") val asteroid: String,
    @SerializedName("ufo") val ufo: String
)

interface AstroApiService {

    @GET("color")
    fun getLaserColor(): Call<ColorResponse>

    @GET("themePack/image")
    fun getThemeImage(): Call<ResponseBody>

    @GET("themePack/audio")
    fun getThemeAudio(): Call<ResponseBody>

    @GET("skins")
    fun getSkins(): Call<SkinsResponse>
}
