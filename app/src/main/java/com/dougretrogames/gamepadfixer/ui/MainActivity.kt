package com.dougretrogames.gamepadfixer.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dougretrogames.gamepadfixer.databinding.ActivityMainBinding

/**
 * Main entry point. Provides navigation to the three main features:
 * 1. Device Diagnostics
 * 2. Key/Motion Event Capture
 * 3. KL File Generator & Installer
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(com.dougretrogames.gamepadfixer.R.string.app_name)

        binding.btnDiagnostics.setOnClickListener {
            startActivity(Intent(this, DiagnosticsActivity::class.java))
        }

        binding.btnCaptureEvents.setOnClickListener {
            startActivity(Intent(this, KeyEventCaptureActivity::class.java))
        }

        binding.btnKlFile.setOnClickListener {
            startActivity(Intent(this, KlFileActivity::class.java))
        }
    }
}
