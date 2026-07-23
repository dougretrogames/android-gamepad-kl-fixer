package com.dougretrogames.gamepadfixer.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dougretrogames.gamepadfixer.databinding.ActivityKlFileBinding
import com.dougretrogames.gamepadfixer.model.InputDeviceInfo
import com.dougretrogames.gamepadfixer.viewmodel.KlFileViewModel
import com.google.android.material.snackbar.Snackbar

/**
 * Activity for generating, previewing, saving and installing .kl files.
 * Can be launched with a pre-selected InputDeviceInfo or standalone.
 */
class KlFileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKlFileBinding
    private lateinit var viewModel: KlFileViewModel

    companion object {
        private const val EXTRA_DEVICE = "extra_device"

        fun newIntent(context: Context, device: InputDeviceInfo): Intent {
            return Intent(context, KlFileActivity::class.java).apply {
                putExtra(EXTRA_DEVICE, device.id)
                putExtra("device_name", device.name)
                putExtra("vendor_id", device.vendorId)
                putExtra("product_id", device.productId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKlFileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "KL File Generator"

        viewModel = ViewModelProvider(this)[KlFileViewModel::class.java]

        // Pre-fill device info from intent if available
        val vendorId = intent.getIntExtra("vendor_id", 0)
        val productId = intent.getIntExtra("product_id", 0)
        val deviceName = intent.getStringExtra("device_name") ?: ""

        if (vendorId != 0 || productId != 0) {
            binding.etVendorId.setText("%04x".format(vendorId))
            binding.etProductId.setText("%04x".format(productId))
            binding.etDeviceName.setText(deviceName)
        }

        viewModel.checkRoot()

        binding.btnGenerate.setOnClickListener {
            val vendor = binding.etVendorId.text.toString().trim()
            val product = binding.etProductId.text.toString().trim()
            if (vendor.isBlank() || product.isBlank()) {
                Snackbar.make(binding.root, "Enter Vendor ID and Product ID", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val vendorInt = vendor.toInt(16)
            val productInt = product.toInt(16)
            val device = InputDeviceInfo(
                id = 0,
                name = binding.etDeviceName.text.toString(),
                descriptor = "",
                vendorId = vendorInt,
                productId = productInt,
                sources = 0,
                hasVibrator = false,
                axes = emptyList(),
                keys = emptyList()
            )
            viewModel.generateKlFromDevice(device)
        }

        binding.btnSave.setOnClickListener {
            viewModel.saveToPrivateStorage(this)
        }

        binding.btnInstall.setOnClickListener {
            viewModel.installToSystem(this)
        }

        binding.btnRestore.setOnClickListener {
            viewModel.restoreBackup()
        }

        viewModel.klContent.observe(this) { content ->
            binding.etKlContent.setText(content)
        }

        viewModel.statusMessage.observe(this) { msg ->
            if (msg.isNotBlank()) {
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.isRooted.observe(this) { isRooted ->
            when (isRooted) {
                true -> binding.tvRootStatus.text = "\u2705 Root available"
                false -> binding.tvRootStatus.text = "\u274C No root access"
                null -> binding.tvRootStatus.text = "Checking root..."
            }
            binding.btnInstall.isEnabled = isRooted == true
            binding.btnRestore.isEnabled = isRooted == true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
