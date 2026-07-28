package com.dougretrogames.gamepadklfixer.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import com.dougretrogames.gamepadklfixer.R
import com.dougretrogames.gamepadklfixer.databinding.ActivityDeviceDetailBinding
import com.dougretrogames.gamepadklfixer.device.DeviceScanner
import com.dougretrogames.gamepadklfixer.model.KeyEventRecord
import com.dougretrogames.gamepadklfixer.ui.viewmodel.DeviceDetailViewModel
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

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
            if (data != null) {
                val capturedKeys = IntentCompat.getParcelableArrayListExtra(
                    data,
                    TestInputActivity.EXTRA_CAPTURED_KEYS,
                    KeyEventRecord::class.java
                )
                val capturedMotions = IntentCompat.getParcelableArrayListExtra(
                    data,
                    TestInputActivity.EXTRA_CAPTURED_MOTIONS,
                    com.dougretrogames.gamepadklfixer.model.MotionEventRecord::class.java
                )
                if (capturedKeys != null) {
                    viewModel.generatePreviewWithCapturedKeys(
                        capturedKeys,
                        capturedMotions ?: arrayListOf()
                    )
                }
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
            binding.tvKlPreview.setText(content)
        }

        viewModel.statusMessage.observe(this) { msg ->
            binding.tvStatus.text = msg
            binding.tvStatus.visibility = if (msg.isNotEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isRooted.observe(this) { rooted ->
            binding.btnInstall.isEnabled = rooted && !(viewModel.isLoading.value ?: false)
            binding.btnRestore.isEnabled = rooted && !(viewModel.isLoading.value ?: false)
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.btnInstall.isEnabled = (viewModel.isRooted.value ?: false) && !loading
            binding.btnRestore.isEnabled = (viewModel.isRooted.value ?: false) && !loading
            binding.btnSaveLocal.isEnabled = !loading
            binding.btnCheckRoot.isEnabled = !loading
        }

        viewModel.saveSuccess.observe(this) { success ->
            if (success) showRebootPrompt()
        }

        viewModel.installSuccess.observe(this) { success ->
            if (success) showRebootPrompt()
        }

        viewModel.restoreSuccess.observe(this) { success ->
            if (success) showRebootPrompt()
        }

        binding.btnCheckRoot.setOnClickListener { viewModel.checkRoot() }
        binding.profileGroup.setOnCheckedChangeListener { _, checkedId ->
            val profile = when (checkedId) {
                R.id.profileOfficial ->
                    com.dougretrogames.gamepadklfixer.kl.KlFileGenerator.Profile.OFFICIAL_SWITCH_PRO
                else ->
                    com.dougretrogames.gamepadklfixer.kl.KlFileGenerator.Profile.GENERIC
            }
            viewModel.setProfile(profile)
        }
        // Default selection matches the ViewModel default.
        binding.profileGeneric.isChecked = true
        binding.btnSaveLocal.setOnClickListener {
            viewModel.setKlPreview(binding.tvKlPreview.text.toString())
            viewModel.saveKlFile()
            Snackbar.make(binding.root, R.string.save_local, Snackbar.LENGTH_SHORT).show()
        }
        binding.btnInstall.setOnClickListener { confirmInstall() }
        binding.btnRestore.setOnClickListener { confirmRestore() }
        binding.btnTestInput.setOnClickListener {
            val intent = Intent(this, TestInputActivity::class.java)
                .putExtra(EXTRA_DEVICE_ID, deviceId)
            testInputLauncher.launch(intent)
        }
    }

    private fun confirmInstall() {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_install_title)
            .setMessage(R.string.confirm_install_message)
            .setPositiveButton(R.string.confirm_yes) { _, _ ->
                viewModel.setKlPreview(binding.tvKlPreview.text.toString())
                viewModel.installKlFile()
                Snackbar.make(binding.root, R.string.loading_install, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.confirm_no) { }
                    .show()
            }
            .setNegativeButton(R.string.confirm_no, null)
            .show()
    }

    private fun confirmRestore() {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_restore_title)
            .setMessage(R.string.confirm_restore_message)
            .setPositiveButton(R.string.confirm_yes) { _, _ ->
                viewModel.restoreKlFile()
                Snackbar.make(binding.root, R.string.loading_restore, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.confirm_no) { }
                    .show()
            }
            .setNegativeButton(R.string.confirm_no, null)
            .show()
    }

    private fun showRebootPrompt() {
        AlertDialog.Builder(this)
            .setTitle(R.string.reboot_title)
            .setMessage(R.string.reboot_message)
            .setPositiveButton(R.string.reboot_now) { _, _ ->
                lifecycleScope.launch {
                    val result = com.dougretrogames.gamepadklfixer.root.RootManager.reboot()
                    if (!result.success) {
                        android.widget.Toast.makeText(
                            this@DeviceDetailActivity,
                            getString(R.string.reboot_failed, result.error.ifEmpty { result.output }),
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .setNegativeButton(R.string.reboot_later, null)
            .setCancelable(true)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
