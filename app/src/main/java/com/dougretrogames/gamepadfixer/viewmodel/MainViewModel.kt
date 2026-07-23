package com.dougretrogames.gamepadfixer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dougretrogames.gamepadfixer.model.GamepadDevice
import com.dougretrogames.gamepadfixer.repository.GamepadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for [MainActivity].
 *
 * Exposes connected gamepads and root availability via LiveData.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GamepadRepository(application)

    private val _gamepads = MutableLiveData<List<GamepadDevice>>(emptyList())
    val gamepads: LiveData<List<GamepadDevice>> = _gamepads

    private val _isRooted = MutableLiveData<Boolean>(false)
    val isRooted: LiveData<Boolean> = _isRooted

    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage

    init {
        refresh()
    }

    /** Rescans connected devices and checks root availability. */
    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val devices = repository.getConnectedGamepads()
            val rooted = repository.isRooted()
            _gamepads.postValue(devices)
            _isRooted.postValue(rooted)
            _statusMessage.postValue(
                if (devices.isEmpty()) "No gamepad detected. Connect a controller."
                else "${devices.size} gamepad(s) detected."
            )
        }
    }

    fun getRepository(): GamepadRepository = repository
}
