package com.dougretrogames.gamepadfixer.model

/**
 * Represents a connected input device (gamepad/joystick).
 *
 * @property id        Android InputDevice ID
 * @property name      Human-readable device name
 * @property vendorId  USB Vendor ID (0x0000–0xFFFF)
 * @property productId USB Product ID (0x0000–0xFFFF)
 * @property sources   Bitmask of InputDevice source flags
 * @property descriptor Unique descriptor string from the system
 */
data class GamepadDevice(
    val id: Int,
    val name: String,
    val vendorId: Int,
    val productId: Int,
    val sources: Int,
    val descriptor: String
) {
    /** Formats vendorId as 4-digit uppercase hex, e.g. "045E" */
    val vendorHex: String get() = vendorId.toString(16).padStart(4, '0').uppercase()

    /** Formats productId as 4-digit uppercase hex, e.g. "028E" */
    val productHex: String get() = productId.toString(16).padStart(4, '0').uppercase()

    /** KL file name format expected by Android: Vendor_XXXX_Product_XXXX.kl */
    val klFileName: String get() = "Vendor_${vendorHex}_Product_${productHex}.kl"

    /** True when both VID and PID are non-zero (real USB/BT gamepad) */
    val hasValidIds: Boolean get() = vendorId != 0 && productId != 0
}
