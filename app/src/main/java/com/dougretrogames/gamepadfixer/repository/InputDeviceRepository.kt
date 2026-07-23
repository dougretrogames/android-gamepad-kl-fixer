package com.dougretrogames.gamepadfixer.repository

import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import com.dougretrogames.gamepadfixer.model.AxisInfo
import com.dougretrogames.gamepadfixer.model.InputDeviceInfo
import com.dougretrogames.gamepadfixer.model.KeyInfo

/**
 * Repository responsible for enumerating and diagnosing connected input devices.
 */
class InputDeviceRepository {

    /**
     * Returns all connected gamepad/joystick devices.
     */
    fun getGamepadDevices(): List<InputDeviceInfo> {
        val deviceIds = InputDevice.getDeviceIds()
        return deviceIds.mapNotNull { id ->
            val device = InputDevice.getDevice(id) ?: return@mapNotNull null
            if (!isGamepad(device)) return@mapNotNull null
            buildDeviceInfo(device)
        }
    }

    /**
     * Returns all input devices regardless of type.
     */
    fun getAllDevices(): List<InputDeviceInfo> {
        val deviceIds = InputDevice.getDeviceIds()
        return deviceIds.mapNotNull { id ->
            val device = InputDevice.getDevice(id) ?: return@mapNotNull null
            if (device.isVirtual) return@mapNotNull null
            buildDeviceInfo(device)
        }
    }

    private fun isGamepad(device: InputDevice): Boolean {
        val sources = device.sources
        return (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
            (sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
    }

    private fun buildDeviceInfo(device: InputDevice): InputDeviceInfo {
        val axes = buildAxesList(device)
        val keys = buildKeysList(device)
        return InputDeviceInfo(
            id = device.id,
            name = device.name ?: "Unknown",
            descriptor = device.descriptor ?: "",
            vendorId = device.vendorId,
            productId = device.productId,
            sources = device.sources,
            hasVibrator = device.vibrator?.hasVibrator() ?: false,
            axes = axes,
            keys = keys
        )
    }

    private fun buildAxesList(device: InputDevice): List<AxisInfo> {
        val result = mutableListOf<AxisInfo>()
        val motionRanges = device.motionRanges
        for (range in motionRanges) {
            result.add(
                AxisInfo(
                    axisCode = range.axis,
                    axisName = MotionEvent.axisToString(range.axis),
                    minValue = range.min,
                    maxValue = range.max,
                    flat = range.flat,
                    fuzz = range.fuzz
                )
            )
        }
        return result
    }

    private fun buildKeysList(device: InputDevice): List<KeyInfo> {
        val result = mutableListOf<KeyInfo>()
        // Common gamepad key codes to check
        val commonKeyCodes = listOf(
            KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_BUTTON_Y,
            KeyEvent.KEYCODE_BUTTON_L1,
            KeyEvent.KEYCODE_BUTTON_R1,
            KeyEvent.KEYCODE_BUTTON_L2,
            KeyEvent.KEYCODE_BUTTON_R2,
            KeyEvent.KEYCODE_BUTTON_THUMBL,
            KeyEvent.KEYCODE_BUTTON_THUMBR,
            KeyEvent.KEYCODE_BUTTON_START,
            KeyEvent.KEYCODE_BUTTON_SELECT,
            KeyEvent.KEYCODE_BUTTON_MODE,
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_CENTER
        )
        val supported = device.hasKeys(*commonKeyCodes.toIntArray())
        commonKeyCodes.forEachIndexed { index, keyCode ->
            if (supported[index]) {
                result.add(
                    KeyInfo(
                        keyCode = keyCode,
                        keyName = KeyEvent.keyCodeToString(keyCode)
                    )
                )
            }
        }
        return result
    }
}
