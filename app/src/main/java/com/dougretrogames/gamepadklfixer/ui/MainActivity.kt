package com.dougretrogames.gamepadklfixer.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dougretrogames.gamepadklfixer.R
import com.dougretrogames.gamepadklfixer.databinding.ActivityMainBinding
import com.dougretrogames.gamepadklfixer.model.GamepadDevice
import com.dougretrogames.gamepadklfixer.ui.adapter.DeviceAdapter
import com.dougretrogames.gamepadklfixer.ui.viewmodel.DeviceListViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: DeviceListViewModel by viewModels()
    private lateinit var adapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        adapter = DeviceAdapter { device -> openDeviceDetail(device) }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            adapter = this@MainActivity.adapter
        }

        binding.fabRefresh.setOnClickListener { viewModel.refresh() }

        viewModel.devices.observe(this) { devices ->
            adapter.submitList(devices)
            binding.tvEmptyState.visibility =
                if (devices.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }

        viewModel.refresh()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> { viewModel.refresh(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openDeviceDetail(device: GamepadDevice) {
        val intent = Intent(this, DeviceDetailActivity::class.java).apply {
            putExtra(DeviceDetailActivity.EXTRA_DEVICE_ID, device.id)
        }
        startActivity(intent)
    }
}
