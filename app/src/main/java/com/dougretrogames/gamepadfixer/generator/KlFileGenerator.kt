package com.dougretrogames.gamepadfixer.generator

import android.view.KeyEvent
import android.view.MotionEvent
import com.dougretrogames.gamepadfixer.model.InputDeviceInfo
import com.dougretrogames.gamepadfixer.model.KlFile
import com.dougretrogames.gamepadfixer.model.KlMapping

/**
 * Generates .kl key layout files based on InputDeviceInfo and captured events.
 * Provides sensible defaults for common gamepad axes and buttons.
 */
class KlFileGenerator {

    /**
     * Generates a KlFile with default mappings derived from device diagnostics.
     */
    fun generateFromDevice(device: InputDeviceInfo): KlFile {
        val klFile = KlFile(
            fileName = device.klFileName(),
            vendorId = device.vendorId,
            productId = device.productId
        )

        // Add key mappings for detected buttons
        for (key in device.keys) {
            val label = androidKeyCodeToKlLabel(key.keyCode)
            if (label != null) {
                // Scan codes for standard HID gamepad buttons start at 0x130
                val scanCode = keyCodeToHidScanCode(key.keyCode)
                klFile.mappings.add(KlMapping.KeyMapping(scanCode, label))
            }
        }

        // Add axis mappings for detected axes
        for (axis in device.axes) {
            val androidAxisLabel = motionAxisToKlLabel(axis.axisCode)
            if (androidAxisLabel != null) {
                klFile.mappings.add(
                    KlMapping.AxisMapping(
                        rawAxis = motionAxisToHidName(axis.axisCode),
                        androidAxis = androidAxisLabel,
                        flat = if (axis.flat > 0) axis.flat else null
                    )
                )
            }
        }

        return klFile
    }

    /**
     * Generates a KlFile with mappings from a set of captured KeyEvents and MotionEvents.
     */
    fun generateFromCapturedEvents(
        device: InputDeviceInfo,
        capturedKeyCodes: Set<Int>,
        capturedAxes: Map<Int, Float>
    ): KlFile {
        val klFile = KlFile(
            fileName = device.klFileName(),
            vendorId = device.vendorId,
            productId = device.productId
        )

        for (keyCode in capturedKeyCodes) {
            val label = androidKeyCodeToKlLabel(keyCode) ?: KeyEvent.keyCodeToString(keyCode)
            val scanCode = keyCodeToHidScanCode(keyCode)
            klFile.mappings.add(KlMapping.KeyMapping(scanCode, label))
        }

        for ((axisCode, flatVal) in capturedAxes) {
            val androidAxisLabel = motionAxisToKlLabel(axisCode) ?: continue
            klFile.mappings.add(
                KlMapping.AxisMapping(
                    rawAxis = motionAxisToHidName(axisCode),
                    androidAxis = androidAxisLabel,
                    flat = if (flatVal > 0) flatVal else null
                )
            )
        }

        return klFile
    }

    // --- Mapping tables ---

    private fun androidKeyCodeToKlLabel(keyCode: Int): String? = when (keyCode) {
        KeyEvent.KEYCODE_BUTTON_A -> "BUTTON_A"
        KeyEvent.KEYCODE_BUTTON_B -> "BUTTON_B"
        KeyEvent.KEYCODE_BUTTON_X -> "BUTTON_X"
        KeyEvent.KEYCODE_BUTTON_Y -> "BUTTON_Y"
        KeyEvent.KEYCODE_BUTTON_L1 -> "BUTTON_L1"
        KeyEvent.KEYCODE_BUTTON_R1 -> "BUTTON_R1"
        KeyEvent.KEYCODE_BUTTON_L2 -> "BUTTON_L2"
        KeyEvent.KEYCODE_BUTTON_R2 -> "BUTTON_R2"
        KeyEvent.KEYCODE_BUTTON_THUMBL -> "BUTTON_THUMBL"
        KeyEvent.KEYCODE_BUTTON_THUMBR -> "BUTTON_THUMBR"
        KeyEvent.KEYCODE_BUTTON_START -> "BUTTON_START"
        KeyEvent.KEYCODE_BUTTON_SELECT -> "BUTTON_SELECT"
        KeyEvent.KEYCODE_BUTTON_MODE -> "BUTTON_MODE"
        KeyEvent.KEYCODE_DPAD_UP -> "DPAD_UP"
        KeyEvent.KEYCODE_DPAD_DOWN -> "DPAD_DOWN"
        KeyEvent.KEYCODE_DPAD_LEFT -> "DPAD_LEFT"
        KeyEvent.KEYCODE_DPAD_RIGHT -> "DPAD_RIGHT"
        KeyEvent.KEYCODE_DPAD_CENTER -> "DPAD_CENTER"
        else -> null
    }

    private fun keyCodeToHidScanCode(keyCode: Int): Int = when (keyCode) {
        KeyEvent.KEYCODE_BUTTON_A -> 0x130
        KeyEvent.KEYCODE_BUTTON_B -> 0x131
        KeyEvent.KEYCODE_BUTTON_X -> 0x133
        KeyEvent.KEYCODE_BUTTON_Y -> 0x134
        KeyEvent.KEYCODE_BUTTON_L1 -> 0x136
        KeyEvent.KEYCODE_BUTTON_R1 -> 0x137
        KeyEvent.KEYCODE_BUTTON_L2 -> 0x138
        KeyEvent.KEYCODE_BUTTON_R2 -> 0x139
        KeyEvent.KEYCODE_BUTTON_SELECT -> 0x13A
        KeyEvent.KEYCODE_BUTTON_START -> 0x13B
        KeyEvent.KEYCODE_BUTTON_THUMBL -> 0x13D
        KeyEvent.KEYCODE_BUTTON_THUMBR -> 0x13E
        KeyEvent.KEYCODE_BUTTON_MODE -> 0x13C
        KeyEvent.KEYCODE_DPAD_UP -> 0x67
        KeyEvent.KEYCODE_DPAD_DOWN -> 0x6C
        KeyEvent.KEYCODE_DPAD_LEFT -> 0x69
        KeyEvent.KEYCODE_DPAD_RIGHT -> 0x6A
        KeyEvent.KEYCODE_DPAD_CENTER -> 0x160
        else -> keyCode
    }

    private fun motionAxisToKlLabel(axisCode: Int): String? = when (axisCode) {
        MotionEvent.AXIS_X -> "X"
        MotionEvent.AXIS_Y -> "Y"
        MotionEvent.AXIS_Z -> "Z"
        MotionEvent.AXIS_RZ -> "RZ"
        MotionEvent.AXIS_HAT_X -> "HAT_X"
        MotionEvent.AXIS_HAT_Y -> "HAT_Y"
        MotionEvent.AXIS_LTRIGGER -> "LTRIGGER"
        MotionEvent.AXIS_RTRIGGER -> "RTRIGGER"
        MotionEvent.AXIS_THROTTLE -> "THROTTLE"
        MotionEvent.AXIS_RUDDER -> "RUDDER"
        MotionEvent.AXIS_RX -> "RX"
        MotionEvent.AXIS_RY -> "RY"
        else -> null
    }

    private fun motionAxisToHidName(axisCode: Int): String = when (axisCode) {
        MotionEvent.AXIS_X -> "ABS_X"
        MotionEvent.AXIS_Y -> "ABS_Y"
        MotionEvent.AXIS_Z -> "ABS_Z"
        MotionEvent.AXIS_RZ -> "ABS_RZ"
        MotionEvent.AXIS_HAT_X -> "ABS_HAT0X"
        MotionEvent.AXIS_HAT_Y -> "ABS_HAT0Y"
        MotionEvent.AXIS_LTRIGGER -> "ABS_BRAKE"
        MotionEvent.AXIS_RTRIGGER -> "ABS_GAS"
        MotionEvent.AXIS_THROTTLE -> "ABS_THROTTLE"
        MotionEvent.AXIS_RUDDER -> "ABS_RUDDER"
        MotionEvent.AXIS_RX -> "ABS_RX"
        MotionEvent.AXIS_RY -> "ABS_RY"
        else -> "ABS_MISC"
    }
}
