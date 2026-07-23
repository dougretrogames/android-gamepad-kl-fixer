package com.dougretrogames.gamepadfixer.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dougretrogames.gamepadfixer.adapter.EventAdapter
import com.dougretrogames.gamepadfixer.databinding.ActivityKeyEventCaptureBinding
import com.dougretrogames.gamepadfixer.viewmodel.KeyEventCaptureViewModel

/**
 * Activity that captures raw KeyEvent and MotionEvent from connected gamepads.
 * Users can start/stop capture and then export the captured input to the KL Generator.
 */
class KeyEventCaptureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKeyEventCaptureBinding
    private lateinit var viewModel: KeyEventCaptureViewModel
    private lateinit var adapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeyEventCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Capture Input Events"

        viewModel = ViewModelProvider(this)[KeyEventCaptureViewModel::class.java]
        adapter = EventAdapter()

        binding.recyclerEvents.layoutManager = LinearLayoutManager(this)
        binding.recyclerEvents.adapter = adapter

        binding.btnStartCapture.setOnClickListener {
            viewModel.startCapture()
        }

        binding.btnStopCapture.setOnClickListener {
            viewModel.stopCapture()
        }

        binding.btnClear.setOnClickListener {
            viewModel.clearEvents()
        }

        viewModel.capturedEvents.observe(this) { events ->
            adapter.submitList(events)
            if (events.isNotEmpty()) binding.recyclerEvents.scrollToPosition(0)
        }

        viewModel.isCapturing.observe(this) { capturing ->
            binding.btnStartCapture.isEnabled = !capturing
            binding.btnStopCapture.isEnabled = capturing
            binding.tvCaptureStatus.text = if (capturing) "\uD83D\uDD34 Capturing..." else "\u23F9 Stopped"
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        event?.let { viewModel.onKeyEvent(it) }
        return super.onKeyDown(keyCode, event)
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        event?.let { viewModel.onMotionEvent(it) }
        return super.onGenericMotionEvent(event)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
