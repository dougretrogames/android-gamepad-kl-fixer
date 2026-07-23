package com.dougretrogames.gamepadfixer.viewmodel

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dougretrogames.gamepadfixer.model.InputDeviceInfo
import com.dougretrogames.gamepadfixer.repository.InputDeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DiagnosticsViewModel(
    private val repository: InputDeviceRepository = InputDeviceRepository()
) : ViewModel() {

    private val _devices = MutableLiveData<List<InputDeviceInfo>>(emptyList())
    val devices: LiveData<List<InputDeviceInfo>> = _devices

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun loadDevices() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            val deviceList = repository.getAllDevices()
            _devices.postValue(deviceList)
            _loading.postValue(false)
        }
    }

    fun loadGamepadDevices() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            val deviceList = repository.getGamepadDevices()
            _devices.postValue(deviceList)
            _loading.postValue(false)
        }
    }
}
