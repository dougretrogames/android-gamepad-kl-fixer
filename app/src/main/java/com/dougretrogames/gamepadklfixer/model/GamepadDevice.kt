package com.dougretrogames.gamepadklfixer.model

/**
 * Represents a detected input device (gamepad/joystick).
 *
 * @param id        Android internal device ID
 * @param name      Human-readable device name
 * @param vendorId  USB Vendor ID (decimal)
 * @param productId USB Product ID (decimal)
 * @param sources   Bitmask of InputDevice.SOURCE_* flags
 * @param descriptor Unique descriptor string from InputDevice
 */
data class GamepadDevice(
    val id: Int,
    val name: String,
    val vendorId: Int,
    val productId: Int,
    val sources: Int,
    val descriptor: String
) {
    /** Vendor ID formatted as 4-digit hex uppercase, e.g. "045E" */
    val vendorHex: String get() = "%04X".format(vendorId)

    /** Product ID formatted as 4-digit hex uppercase, e.g. "028E" */
    val productHex: String get() = "%04X".format(productId)

    /** Standard KL filename: Vendor_XXXX_Product_XXXX.kl */
    val klFileName: String get() = "Vendor_${vendorHex}_Product_${productHex}.kl"
}
