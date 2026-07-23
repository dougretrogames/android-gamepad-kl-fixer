package com.dougretrogames.gamepadfixer.model

/**
 * Represents a single key or axis mapping line in a .kl file.
 *
 * @property scanCode    Raw Linux scan code (decimal) or axis name
 * @property keyLabel    Android KeyEvent key name, e.g. "BUTTON_A"
 * @property flags       Optional flags, e.g. "WAKE" or empty string
 * @property isAxis      True when this entry is an AXIS mapping (not KEY)
 */
data class KeyMapping(
    val scanCode: String,
    val keyLabel: String,
    val flags: String = "",
    val isAxis: Boolean = false
) {
    /**
     * Serializes the mapping to a .kl file line.
     * Examples:
     *   key 304   BUTTON_A
     *   key 304   BUTTON_A   WAKE
     *   axis 0x00 X
     */
    fun toKlLine(): String {
        val prefix = if (isAxis) "axis" else "key"
        return if (flags.isBlank()) "$prefix $scanCode  $keyLabel"
        else "$prefix $scanCode  $keyLabel  $flags"
    }
}
