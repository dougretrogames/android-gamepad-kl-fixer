package com.dougretrogames.gamepadklfixer.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dougretrogames.gamepadklfixer.databinding.ActivityDeviceDetailBinding
import com.dougretrogames.gamepadklfixer.device.DeviceScanner
import com.dougretrogames.gamepadklfixer.ui.viewmodel.DeviceDetailViewModel

class DeviceDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DEVICE_ID = "extra_device_id"
    }

    private lateinit var binding: ActivityDeviceDetailBinding
    private val viewModel: DeviceDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val deviceId = intent.getIntExtra(EXTRA_DEVICE_ID, -1)
        if (deviceId == -1) { finish(); return }

        viewModel.loadDevice(deviceId)

        viewModel.device.observe(this) { device ->
            if (device == null) return@observe
            supportActionBar?.title = device.name
            binding.tvDeviceName.text = device.name
            binding.tvVendorId.text = "Vendor ID: 0x${device.vendorHex} (${device.vendorId})"
            binding.tvProductId.text = "Product ID: 0x${device.productHex} (${device.productId})"
            binding.tvKlFilename.text = "KL File: ${device.klFileName}"
            binding.tvSources.text = "Sources: ${DeviceScanner.sourcesDescription(device.sources)}"
            binding.tvDescriptor.text = "Descriptor: ${device.descriptor}"
        }

        viewModel.klPreview.observe(this) { content ->
            binding.tvKlPreview.text = content
        }

        viewModel.statusMessage.observe(this) { msg ->
            binding.tvStatus.text = msg
            binding.tvStatus.visibility = if (msg.isNotEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isRooted.observe(this) { rooted ->
            binding.btnInstall.isEnabled = rooted
            binding.btnRestore.isEnabled = rooted
        }

        binding.btnCheckRoot.setOnClickListener { viewModel.checkRoot() }
        binding.btnSaveLocal.setOnClickListener { viewModel.saveKlFile() }
        binding.btnInstall.setOnClickListener { viewModel.installKlFile() }
        binding.btnRestore.setOnClickListener { viewModel.restoreKlFile() }
        binding.btnTestInput.setOnClickListener {
            startActivity(
                Intent(this, TestInputActivity::class.java)
                    .putExtra(EXTRA_DEVICE_ID, deviceId)
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
