package com.dougretrogames.gamepadfixer.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dougretrogames.gamepadfixer.adapter.DeviceAdapter
import com.dougretrogames.gamepadfixer.databinding.ActivityDiagnosticsBinding
import com.dougretrogames.gamepadfixer.viewmodel.DiagnosticsViewModel

/**
 * Displays all connected input devices with their Vendor ID, Product ID, axes and buttons.
 */
class DiagnosticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiagnosticsBinding
    private lateinit var viewModel: DiagnosticsViewModel
    private lateinit var adapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiagnosticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Device Diagnostics"

        viewModel = ViewModelProvider(this)[DiagnosticsViewModel::class.java]

        adapter = DeviceAdapter { device ->
            // Launch KlFileActivity pre-loaded with this device
            val intent = KlFileActivity.newIntent(this, device)
            startActivity(intent)
        }

        binding.recyclerDevices.layoutManager = LinearLayoutManager(this)
        binding.recyclerDevices.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadDevices()
        }

        binding.toggleGamepadOnly.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.loadGamepadDevices() else viewModel.loadDevices()
        }

        viewModel.devices.observe(this) { devices ->
            adapter.submitList(devices)
            binding.tvEmpty.visibility = if (devices.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.loading.observe(this) { loading ->
            binding.swipeRefresh.isRefreshing = loading
        }

        viewModel.loadDevices()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
