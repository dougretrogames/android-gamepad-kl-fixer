package com.dougretrogames.gamepadfixer.model

/**
 * Represents a single key or axis mapping entry in a .kl file.
 */
sealed class KlMapping {
    abstract fun toKlLine(): String

    data class KeyMapping(
        val scanCode: Int,
        val keyLabel: String,
        val flags: String = ""
    ) : KlMapping() {
        override fun toKlLine(): String {
            val flagPart = if (flags.isNotBlank()) " $flags" else ""
            return "key %d   %s%s".format(scanCode, keyLabel, flagPart)
        }
    }

    data class AxisMapping(
        val rawAxis: String,
        val androidAxis: String,
        val flat: Float? = null
    ) : KlMapping() {
        override fun toKlLine(): String {
            val flatPart = if (flat != null) " flat %.1f".format(flat) else ""
            return "axis %s   %s%s".format(rawAxis, androidAxis, flatPart)
        }
    }
}
