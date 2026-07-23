package com.dougretrogames.gamepadklfixer.ui

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dougretrogames.gamepadklfixer.databinding.ActivityTestInputBinding
import com.dougretrogames.gamepadklfixer.ui.adapter.KeyEventAdapter
import com.dougretrogames.gamepadklfixer.ui.viewmodel.TestInputViewModel
import com.dougretrogames.gamepadklfixer.device.DeviceScanner
import android.view.InputDevice

class TestInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestInputBinding
    private val viewModel: TestInputViewModel by viewModels()
    private lateinit var keyAdapter: KeyEventAdapter
    private var targetDeviceId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(com.dougretrogames.gamepadklfixer.R.string.test_input)

        targetDeviceId = intent.getIntExtra(DeviceDetailActivity.EXTRA_DEVICE_ID, -1)

        keyAdapter = KeyEventAdapter()
        binding.recyclerKeyEvents.apply {
            layoutManager = LinearLayoutManager(this@TestInputActivity)
            adapter = keyAdapter
        }

        viewModel.keyEvents.observe(this) { events ->
            keyAdapter.submitList(events)
        }

        viewModel.lastAxis.observe(this) { axes ->
            val sb = StringBuilder()
            axes.forEach { (axis, value) ->
                sb.appendLine("${android.view.MotionEvent.axisToString(axis)}: ${"%+.3f".format(value)}")
            }
            binding.tvAxisValues.text = if (sb.isEmpty()) "No axis movement detected" else sb.toString()
        }

        binding.btnClear.setOnClickListener { viewModel.clear() }
        binding.btnGenerateKl.setOnClickListener { generateKlFromCapture() }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (isGamepadEvent(event)) {
            return viewModel.onKeyEvent(event)
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (isGamepadEvent(event)) {
            return viewModel.onKeyEvent(event)
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK
            || event.source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD) {
            return viewModel.onMotionEvent(event)
        }
        return super.onGenericMotionEvent(event)
    }

    private fun isGamepadEvent(event: KeyEvent): Boolean {
        val device = InputDevice.getDevice(event.deviceId) ?: return false
        return DeviceScanner.isGamepad(device)
    }

    private fun generateKlFromCapture() {
        val captured = viewModel.getCapturedKeys()
        val resultIntent = Intent().apply {
            putExtra("captured_key_count", captured.size)
        }
        // Pass back to DeviceDetailActivity via shared ViewModel or Intent
        // For now, show a toast with the count
        android.widget.Toast.makeText(
            this,
            "Captured ${captured.size} key events. Go back to generate KL.",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
