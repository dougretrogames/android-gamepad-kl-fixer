package com.dougretrogames.gamepadfixer.viewmodel

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class CapturedEvent(
    val type: String,
    val description: String,
    val rawCode: Int,
    val value: Float = 0f
)

class KeyEventCaptureViewModel : ViewModel() {

    private val _capturedEvents = MutableLiveData<List<CapturedEvent>>(emptyList())
    val capturedEvents: LiveData<List<CapturedEvent>> = _capturedEvents

    private val _capturedKeyCodes = mutableSetOf<Int>()
    val capturedKeyCodes: Set<Int> get() = _capturedKeyCodes.toSet()

    private val _capturedAxes = mutableMapOf<Int, Float>()
    val capturedAxes: Map<Int, Float> get() = _capturedAxes.toMap()

    private val _isCapturing = MutableLiveData(false)
    val isCapturing: LiveData<Boolean> = _isCapturing

    fun startCapture() {
        _capturedEvents.value = emptyList()
        _capturedKeyCodes.clear()
        _capturedAxes.clear()
        _isCapturing.value = true
    }

    fun stopCapture() {
        _isCapturing.value = false
    }

    fun onKeyEvent(event: KeyEvent) {
        if (_isCapturing.value != true) return
        if (event.action != KeyEvent.ACTION_DOWN) return

        _capturedKeyCodes.add(event.keyCode)
        val desc = "KEY_DOWN: ${KeyEvent.keyCodeToString(event.keyCode)} (code=${event.keyCode}, scan=${event.scanCode})"
        addEvent(CapturedEvent("KEY", desc, event.keyCode))
    }

    fun onMotionEvent(event: MotionEvent) {
        if (_isCapturing.value != true) return
        val sb = StringBuilder("MOTION:")
        for (i in 0 until MotionEvent.getAxisCount()) {
            val axis = MotionEvent.axisFromString(MotionEvent.axisToString(i)) 
            if (axis < 0) continue
            val value = event.getAxisValue(axis)
            if (kotlin.math.abs(value) > 0.1f) {
                _capturedAxes[axis] = value
                sb.append(" ${MotionEvent.axisToString(axis)}=${".2f".format(value)}")
            }
        }
        if (sb.length > 7) {
            addEvent(CapturedEvent("MOTION", sb.toString(), 0, 0f))
        }
    }

    private fun addEvent(event: CapturedEvent) {
        val current = _capturedEvents.value.orEmpty().toMutableList()
        current.add(0, event)
        if (current.size > 100) current.removeAt(current.size - 1)
        _capturedEvents.postValue(current)
    }

    fun clearEvents() {
        _capturedEvents.value = emptyList()
        _capturedKeyCodes.clear()
        _capturedAxes.clear()
    }
}
