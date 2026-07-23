package com.dougretrogames.gamepadklfixer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dougretrogames.gamepadklfixer.device.DeviceScanner
import com.dougretrogames.gamepadklfixer.kl.KlFileStorage
import com.dougretrogames.gamepadklfixer.model.GamepadDevice
import kotlinx.coroutines.launch

class DeviceListViewModel(application: Application) : AndroidViewModel(application) {

    private val _devices = MutableLiveData<List<GamepadDevice>>(emptyList())
    val devices: LiveData<List<GamepadDevice>> = _devices

    private val _savedFiles = MutableLiveData<List<String>>(emptyList())
    val savedFiles: LiveData<List<String>> = _savedFiles

    fun refresh() {
        viewModelScope.launch {
            _devices.value = DeviceScanner.scanGamepads()
            _savedFiles.value = KlFileStorage.listSavedKlFiles(getApplication())
                .map { it.name }
        }
    }
}
