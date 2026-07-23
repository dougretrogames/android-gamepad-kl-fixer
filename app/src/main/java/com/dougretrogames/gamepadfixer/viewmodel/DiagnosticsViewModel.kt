package com.dougretrogames.gamepadfixer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dougretrogames.gamepadfixer.model.GamepadDevice
import com.dougretrogames.gamepadfixer.repository.DeviceDiagnostics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for [DiagnosticsActivity].
 *
 * Provides all input device details for the diagnostic screen.
 */
class DiagnosticsViewModel(application: Application) : AndroidViewModel(application) {

    private val _allDevices = MutableLiveData<List<GamepadDevice>>(emptyList())
    val allDevices: LiveData<List<GamepadDevice>> = _allDevices

    private val _selectedDevice = MutableLiveData<GamepadDevice?>(null)
    val selectedDevice: LiveData<GamepadDevice?> = _selectedDevice

    init {
        loadAllDevices()
    }

    fun loadAllDevices() {
        viewModelScope.launch(Dispatchers.IO) {
            _allDevices.postValue(DeviceDiagnostics.getAllInputDevices())
        }
    }

    fun selectDevice(device: GamepadDevice) {
        _selectedDevice.value = device
    }

    fun describeSelectedSources(): String {
        val device = _selectedDevice.value ?: return ""
        return DeviceDiagnostics.describeSources(device.sources)
    }
}
