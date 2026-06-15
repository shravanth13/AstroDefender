package com.astrodefender.ui

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.astrodefender.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPlay.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Settings")
                .setMessage("No settings available yet.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.btnHelp.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("How to Play")
                .setMessage("Use the joystick to rotate your ship.\nTap FIRE to shoot lasers.\nDestroy asteroids to score points.\nLarge asteroids split into smaller ones.\nYou have 3 lives. Good luck!")
                .setPositiveButton("Got it", null)
                .show()
        }
    }
}
