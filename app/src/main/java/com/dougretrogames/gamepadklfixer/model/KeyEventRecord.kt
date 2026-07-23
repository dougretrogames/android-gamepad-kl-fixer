package com.dougretrogames.gamepadklfixer.model

import android.os.Parcelable
import android.view.KeyEvent
import kotlinx.parcelize.Parcelize

/**
 * Captures a snapshot of a KeyEvent for logging/diagnostics.
 */
@Parcelize
data class KeyEventRecord(
    val keyCode: Int,
    val scanCode: Int,
    val action: Int,
    val deviceId: Int,
    val metaState: Int,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {
    val keyCodeName: String get() = KeyEvent.keyCodeToString(keyCode)
    val actionName: String get() = when (action) {
        KeyEvent.ACTION_DOWN -> "DOWN"
        KeyEvent.ACTION_UP -> "UP"
        KeyEvent.ACTION_MULTIPLE -> "MULTIPLE"
        else -> "UNKNOWN($action)"
    }
}
