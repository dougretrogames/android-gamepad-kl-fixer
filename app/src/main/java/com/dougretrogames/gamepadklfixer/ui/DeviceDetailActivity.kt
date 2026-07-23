package com.dougretrogames.gamepadklfixer.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dougretrogames.gamepadklfixer.R
import com.dougretrogames.gamepadklfixer.databinding.ActivityDeviceDetailBinding
import com.dougretrogames.gamepadklfixer.device.DeviceScanner
import com.dougretrogames.gamepadklfixer.ui.viewmodel.DeviceDetailViewModel

class DeviceDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DEVICE_ID = "extra_device_id"
    }

    private lateinit var binding: ActivityDeviceDetailBinding
    private val viewModel: DeviceDetailViewModel by viewModels()

    private val testInputLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val capturedKeys = data?.getParcelableArrayListExtra<com.dougretrogames.gamepadklfixer.model.KeyEventRecord>(
                TestInputActivity.EXTRA_CAPTURED_KEYS
            )
            if (capturedKeys != null) {
                viewModel.generatePreviewWithCapturedKeys(capturedKeys)
            }
        }
    }

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
            binding.tvVendorId.text = getString(R.string.vendor_id_label, device.vendorHex, device.vendorId)
            binding.tvProductId.text = getString(R.string.product_id_label, device.productHex, device.productId)
            binding.tvKlFilename.text = getString(R.string.kl_file_label, device.klFileName)
            binding.tvSources.text = getString(R.string.sources_label, DeviceScanner.sourcesDescription(device.sources))
            binding.tvDescriptor.text = getString(R.string.descriptor_label, device.descriptor)
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
            val intent = Intent(this, TestInputActivity::class.java)
                .putExtra(EXTRA_DEVICE_ID, deviceId)
            testInputLauncher.launch(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
