package com.dougretrogames.gamepadklfixer.device

import android.hardware.input.InputManager
import android.view.InputDevice
import com.dougretrogames.gamepadklfixer.model.GamepadDevice

/**
 * Scans all connected InputDevices and filters for gamepads/joysticks.
 */
object DeviceScanner {

    /**
     * Returns a list of all connected gamepad/joystick devices.
     * A device qualifies if it has SOURCE_GAMEPAD or SOURCE_JOYSTICK.
     */
    fun scanGamepads(): List<GamepadDevice> {
        val ids = InputDevice.getDeviceIds()
        return ids.mapNotNull { id ->
            val device = InputDevice.getDevice(id) ?: return@mapNotNull null
            if (!isGamepad(device)) return@mapNotNull null
            GamepadDevice(
                id = device.id,
                name = device.name ?: "Unknown Device",
                vendorId = device.vendorId,
                productId = device.productId,
                sources = device.sources,
                descriptor = device.descriptor
            )
        }
    }

    /**
     * Checks if a device is a gamepad or joystick.
     */
    fun isGamepad(device: InputDevice): Boolean {
        val sources = device.sources
        return (sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD)
            || (sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK)
    }

    /**
     * Returns a human-readable source description.
     */
    fun sourcesDescription(sources: Int): String {
        val flags = mutableListOf<String>()
        if (sources and InputDevice.SOURCE_GAMEPAD != 0) flags.add("GAMEPAD")
        if (sources and InputDevice.SOURCE_JOYSTICK != 0) flags.add("JOYSTICK")
        if (sources and InputDevice.SOURCE_KEYBOARD != 0) flags.add("KEYBOARD")
        if (sources and InputDevice.SOURCE_DPAD != 0) flags.add("DPAD")
        if (sources and InputDevice.SOURCE_TOUCHSCREEN != 0) flags.add("TOUCHSCREEN")
        if (sources and InputDevice.SOURCE_MOUSE != 0) flags.add("MOUSE")
        return if (flags.isEmpty()) "UNKNOWN(0x%08X)".format(sources) else flags.joinToString(" | ")
    }
}
