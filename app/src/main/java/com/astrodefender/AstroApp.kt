package com.astrodefender

import android.app.Application

class AstroApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: AstroApp
            private set
    }
}
