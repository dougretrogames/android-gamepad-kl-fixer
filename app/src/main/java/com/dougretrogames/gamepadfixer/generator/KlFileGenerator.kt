package com.dougretrogames.gamepadfixer.generator

import android.view.KeyEvent
import android.view.MotionEvent
import com.dougretrogames.gamepadfixer.model.GamepadDevice
import com.dougretrogames.gamepadfixer.model.KeyMapping
import com.dougretrogames.gamepadfixer.model.KlFile

/**
 * Generates .kl (KeyLayout) file content for a given [GamepadDevice].
 *
 * The generator combines:
 * 1. A comprehensive set of default gamepad key/axis mappings.
 * 2. Any additional mappings recorded by the user via [KeyEventCaptureHelper].
 */
object KlFileGenerator {

    /**
     * Default key mappings used for a standard gamepad.
     * Scan codes match the common Linux HID gamepad driver (xpad/generic-usb-hid).
     */
    val defaultKeyMappings: List<KeyMapping> = listOf(
        // Face buttons
        KeyMapping("304", "BUTTON_A", "WAKE"),
        KeyMapping("305", "BUTTON_B", "WAKE"),
        KeyMapping("307", "BUTTON_X", "WAKE"),
        KeyMapping("308", "BUTTON_Y", "WAKE"),
        // Shoulder buttons
        KeyMapping("310", "BUTTON_L1", "WAKE"),
        KeyMapping("311", "BUTTON_R1", "WAKE"),
        KeyMapping("312", "BUTTON_L2", "WAKE"),
        KeyMapping("313", "BUTTON_R2", "WAKE"),
        // Thumbsticks
        KeyMapping("317", "BUTTON_THUMBL", "WAKE"),
        KeyMapping("318", "BUTTON_THUMBR", "WAKE"),
        // Menu buttons
        KeyMapping("314", "BUTTON_SELECT", "WAKE"),
        KeyMapping("315", "BUTTON_START", "WAKE"),
        KeyMapping("316", "BUTTON_MODE", "WAKE"),
        // D-Pad (hat switch)
        KeyMapping("0x10", "DPAD_UP",    isAxis = true),
        KeyMapping("0x11", "DPAD_LEFT",  isAxis = true)
    )

    /**
     * Default axis mappings for a standard gamepad.
     */
    val defaultAxisMappings: List<KeyMapping> = listOf(
        KeyMapping("0x00", "X",     isAxis = true),
        KeyMapping("0x01", "Y",     isAxis = true),
        KeyMapping("0x02", "Z",     isAxis = true),
        KeyMapping("0x05", "RZ",    isAxis = true),
        KeyMapping("0x09", "BRAKE", isAxis = true),
        KeyMapping("0x0c", "GAS",   isAxis = true)
    )

    /**
     * Generates a [KlFile] for [device] merging default mappings with [customMappings].
     *
     * Custom mappings override defaults with the same scan code.
     *
     * @param device        Target gamepad device.
     * @param customMappings Additional/override mappings from user input capture.
     * @return [KlFile] ready to be written to disk.
     */
    fun generate(device: GamepadDevice, customMappings: List<KeyMapping> = emptyList()): KlFile {
        val defaults = (defaultKeyMappings + defaultAxisMappings).associateBy { it.scanCode }.toMutableMap()
        customMappings.forEach { defaults[it.scanCode] = it }
        return KlFile(device = device, mappings = defaults.values.toList())
    }

    /**
     * Converts a captured [KeyEvent] into a [KeyMapping] entry.
     *
     * @param event   The raw KeyEvent from the gamepad.
     * @return [KeyMapping] or null if the event does not carry a scan code.
     */
    fun fromKeyEvent(event: KeyEvent): KeyMapping? {
        val scanCode = event.scanCode
        if (scanCode == 0) return null
        val keyName = KeyEvent.keyCodeToString(event.keyCode).removePrefix("KEYCODE_")
        return KeyMapping(scanCode = scanCode.toString(), keyLabel = keyName, flags = "WAKE")
    }

    /**
     * Converts a captured [MotionEvent] axis value into axis [KeyMapping] entries.
     *
     * Only axes with an absolute value > 0.1 are considered active.
     *
     * @param event  The raw MotionEvent from the gamepad.
     * @return List of [KeyMapping] for each triggered axis.
     */
    fun fromMotionEvent(event: MotionEvent): List<KeyMapping> {
        val axes = listOf(
            MotionEvent.AXIS_X    to "X",
            MotionEvent.AXIS_Y    to "Y",
            MotionEvent.AXIS_Z    to "Z",
            MotionEvent.AXIS_RZ   to "RZ",
            MotionEvent.AXIS_LTRIGGER to "BRAKE",
            MotionEvent.AXIS_RTRIGGER to "GAS",
            MotionEvent.AXIS_HAT_X    to "HAT_X",
            MotionEvent.AXIS_HAT_Y    to "HAT_Y"
        )
        return axes
            .filter { (axis, _) -> Math.abs(event.getAxisValue(axis)) > 0.1f }
            .map { (axis, label) ->
                KeyMapping(
                    scanCode = "0x${axis.toString(16).padStart(2, '0')}",
                    keyLabel = label,
                    isAxis = true
                )
            }
    }
}
