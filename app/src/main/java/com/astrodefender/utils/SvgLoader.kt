package com.astrodefender.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import com.caverock.androidsvg.SVG
import java.net.HttpURLConnection
import java.net.URL

object SvgLoader {

    fun loadFromUrl(url: String, w: Int, h: Int): Bitmap? {
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            conn.connect()
            val svg = SVG.getFromInputStream(conn.inputStream)
            conn.disconnect()
            renderSvg(svg, w, h)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun renderSvg(svg: SVG, w: Int, h: Int): Bitmap {
        svg.setDocumentWidth(w.toFloat())
        svg.setDocumentHeight(h.toFloat())
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        svg.renderToCanvas(canvas)
        return bmp
    }
}
