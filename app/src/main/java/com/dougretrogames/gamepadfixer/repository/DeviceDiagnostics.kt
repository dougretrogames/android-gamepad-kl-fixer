package com.dougretrogames.gamepadfixer.repository

import android.hardware.input.InputManager
import android.view.InputDevice
import com.dougretrogames.gamepadfixer.model.GamepadDevice

/**
 * Scans the system for connected input devices and filters gamepads/joysticks.
 *
 * Uses [InputManager] and [InputDevice] APIs available since API 9,
 * fully compatible with minSdk 21.
 */
object DeviceDiagnostics {

    /**
     * Returns the list of all connected gamepad/joystick input devices.
     *
     * A device is included when it reports any of:
     * - SOURCE_GAMEPAD
     * - SOURCE_JOYSTICK
     * - SOURCE_DPAD (physical D-Pad from a controller)
     */
    fun getConnectedGamepads(): List<GamepadDevice> {
        val gamepadSources = InputDevice.SOURCE_GAMEPAD or
                InputDevice.SOURCE_JOYSTICK or
                InputDevice.SOURCE_DPAD

        return InputDevice.getDeviceIds()
            .mapNotNull { id -> InputDevice.getDevice(id) }
            .filter { device ->
                // Exclude virtual devices (keyboards, touchscreens, etc.)
                !device.isVirtual && (device.sources and gamepadSources) != 0
            }
            .map { device ->
                GamepadDevice(
                    id = device.id,
                    name = device.name ?: "Unknown",
                    vendorId = device.vendorId,
                    productId = device.productId,
                    sources = device.sources,
                    descriptor = device.descriptor
                )
            }
    }

    /**
     * Returns all input devices including non-gamepads (for full diagnostic view).
     */
    fun getAllInputDevices(): List<GamepadDevice> {
        return InputDevice.getDeviceIds()
            .mapNotNull { id -> InputDevice.getDevice(id) }
            .filter { !it.isVirtual }
            .map { device ->
                GamepadDevice(
                    id = device.id,
                    name = device.name ?: "Unknown",
                    vendorId = device.vendorId,
                    productId = device.productId,
                    sources = device.sources,
                    descriptor = device.descriptor
                )
            }
    }

    /**
     * Finds a device by its Android InputDevice ID.
     */
    fun getDeviceById(deviceId: Int): GamepadDevice? {
        val device = InputDevice.getDevice(deviceId) ?: return null
        return GamepadDevice(
            id = device.id,
            name = device.name ?: "Unknown",
            vendorId = device.vendorId,
            productId = device.productId,
            sources = device.sources,
            descriptor = device.descriptor
        )
    }

    /**
     * Returns a human-readable string describing the sources bitmask.
     */
    fun describeSources(sources: Int): String {
        val parts = mutableListOf<String>()
        if (sources and InputDevice.SOURCE_GAMEPAD != 0) parts.add("GAMEPAD")
        if (sources and InputDevice.SOURCE_JOYSTICK != 0) parts.add("JOYSTICK")
        if (sources and InputDevice.SOURCE_DPAD != 0) parts.add("DPAD")
        if (sources and InputDevice.SOURCE_KEYBOARD != 0) parts.add("KEYBOARD")
        if (sources and InputDevice.SOURCE_TOUCHSCREEN != 0) parts.add("TOUCHSCREEN")
        if (sources and InputDevice.SOURCE_MOUSE != 0) parts.add("MOUSE")
        return if (parts.isEmpty()) "UNKNOWN(0x${sources.toString(16)})"
        else parts.joinToString(", ")
    }
}
