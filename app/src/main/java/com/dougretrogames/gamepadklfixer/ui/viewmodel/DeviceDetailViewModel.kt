package com.dougretrogames.gamepadklfixer.ui.viewmodel

import android.app.Application
import android.view.InputDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dougretrogames.gamepadklfixer.kl.KlFileGenerator
import com.dougretrogames.gamepadklfixer.kl.KlFileStorage
import com.dougretrogames.gamepadklfixer.model.GamepadDevice
import com.dougretrogames.gamepadklfixer.root.RootManager
import kotlinx.coroutines.launch

class DeviceDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _device = MutableLiveData<GamepadDevice?>(null)
    val device: LiveData<GamepadDevice?> = _device

    private val _klPreview = MutableLiveData<String>("")
    val klPreview: LiveData<String> = _klPreview

    private val _statusMessage = MutableLiveData<String>("")
    val statusMessage: LiveData<String> = _statusMessage

    private val _isRooted = MutableLiveData<Boolean>(false)
    val isRooted: LiveData<Boolean> = _isRooted

    fun loadDevice(deviceId: Int) {
        val inputDevice = InputDevice.getDevice(deviceId) ?: return
        val gamepad = GamepadDevice(
            id = inputDevice.id,
            name = inputDevice.name ?: "Unknown",
            vendorId = inputDevice.vendorId,
            productId = inputDevice.productId,
            sources = inputDevice.sources,
            descriptor = inputDevice.descriptor
        )
        _device.value = gamepad
        generatePreview(gamepad)
    }

    fun setDevice(device: GamepadDevice) {
        _device.value = device
        generatePreview(device)
    }

    private fun generatePreview(device: GamepadDevice) {
        val content = KlFileGenerator.generateDefault(device)
        _klPreview.value = content
    }

    fun checkRoot() {
        viewModelScope.launch {
            val result = RootManager.checkRootAccess()
            _isRooted.value = result.success && result.output.contains("uid=0")
            _statusMessage.value = if (_isRooted.value == true)
                "✅ Root access confirmed"
            else
                "❌ Root not available: ${result.error.ifEmpty { result.output }}"
        }
    }

    fun saveKlFile() {
        val dev = _device.value ?: return
        val content = _klPreview.value ?: return
        viewModelScope.launch {
            try {
                val file = KlFileStorage.saveKlFile(getApplication(), dev, content)
                _statusMessage.value = "✅ Saved to app storage: ${file.name}"
            } catch (e: Exception) {
                _statusMessage.value = "❌ Save failed: ${e.message}"
            }
        }
    }

    fun installKlFile() {
        val dev = _device.value ?: return
        val content = _klPreview.value ?: return
        viewModelScope.launch {
            try {
                val file = KlFileStorage.saveKlFile(getApplication(), dev, content)
                // Backup existing
                val backupResult = RootManager.backupKlFile(dev.klFileName)
                // Install
                val installResult = RootManager.installKlFile(file, dev.klFileName)
                _statusMessage.value = if (installResult.success) {
                    "✅ Installed: ${dev.klFileName}\nBackup: ${backupResult.output}"
                } else {
                    "❌ Install failed: ${installResult.error}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "❌ Error: ${e.message}"
            }
        }
    }

    fun restoreKlFile() {
        val dev = _device.value ?: return
        viewModelScope.launch {
            val result = RootManager.restoreKlFile(dev.klFileName)
            _statusMessage.value = if (result.success) {
                "✅ Restored: ${dev.klFileName}"
            } else {
                "❌ Restore failed: ${result.error.ifEmpty { result.output }}"
            }
        }
    }
}
