package com.dougretrogames.gamepadklfixer.ui.viewmodel

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dougretrogames.gamepadklfixer.model.KeyEventRecord
import com.dougretrogames.gamepadklfixer.model.MotionEventRecord

class TestInputViewModel : ViewModel() {

    private val _keyEvents = MutableLiveData<List<KeyEventRecord>>(emptyList())
    val keyEvents: LiveData<List<KeyEventRecord>> = _keyEvents

    private val _motionEvents = MutableLiveData<List<MotionEventRecord>>(emptyList())
    val motionEvents: LiveData<List<MotionEventRecord>> = _motionEvents

    private val _lastAxis = MutableLiveData<Map<Int, Float>>(emptyMap())
    val lastAxis: LiveData<Map<Int, Float>> = _lastAxis

    fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return true
        val record = KeyEventRecord(
            keyCode = event.keyCode,
            scanCode = event.scanCode,
            action = event.action,
            deviceId = event.deviceId,
            metaState = event.metaState
        )
        val current = _keyEvents.value?.toMutableList() ?: mutableListOf()
        current.add(0, record)
        if (current.size > 50) current.removeAt(current.size - 1)
        _keyEvents.value = current
        return true
    }

    fun onMotionEvent(event: MotionEvent): Boolean {
        val record = MotionEventRecord.fromMotionEvent(event)
        _lastAxis.value = record.axisValues
        val current = _motionEvents.value?.toMutableList() ?: mutableListOf()
        current.add(0, record)
        if (current.size > 30) current.removeAt(current.size - 1)
        _motionEvents.value = current
        return true
    }

    fun clear() {
        _keyEvents.value = emptyList()
        _motionEvents.value = emptyList()
        _lastAxis.value = emptyMap()
    }

    fun getCapturedKeys(): List<KeyEventRecord> = _keyEvents.value ?: emptyList()
}
