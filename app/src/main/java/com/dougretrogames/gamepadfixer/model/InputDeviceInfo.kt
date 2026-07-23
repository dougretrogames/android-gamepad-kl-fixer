package com.dougretrogames.gamepadfixer.model

/**
 * Holds diagnostic information about a connected input device (gamepad/joystick).
 */
data class InputDeviceInfo(
    val id: Int,
    val name: String,
    val descriptor: String,
    val vendorId: Int,
    val productId: Int,
    val sources: Int,
    val hasVibrator: Boolean,
    val axes: List<AxisInfo>,
    val keys: List<KeyInfo>
) {
    /** Returns the formatted filename used by Android for .kl files. */
    fun klFileName(): String =
        "Vendor_%04x_Product_%04x.kl".format(vendorId, productId)

    /** Returns vendor ID formatted as 4-digit hex. */
    fun vendorHex(): String = "%04x".format(vendorId)

    /** Returns product ID formatted as 4-digit hex. */
    fun productHex(): String = "%04x".format(productId)
}

data class AxisInfo(
    val axisCode: Int,
    val axisName: String,
    val minValue: Float,
    val maxValue: Float,
    val flat: Float,
    val fuzz: Float
)

data class KeyInfo(
    val keyCode: Int,
    val keyName: String
)
