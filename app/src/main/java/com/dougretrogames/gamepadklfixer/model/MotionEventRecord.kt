package com.dougretrogames.gamepadklfixer.model

import android.view.MotionEvent

/**
 * Captures axis values from a MotionEvent for diagnostics.
 */
data class MotionEventRecord(
    val deviceId: Int,
    val source: Int,
    val axisValues: Map<Int, Float>,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        val GAMEPAD_AXES = listOf(
            MotionEvent.AXIS_X,
            MotionEvent.AXIS_Y,
            MotionEvent.AXIS_Z,
            MotionEvent.AXIS_RZ,
            MotionEvent.AXIS_HAT_X,
            MotionEvent.AXIS_HAT_Y,
            MotionEvent.AXIS_LTRIGGER,
            MotionEvent.AXIS_RTRIGGER,
            MotionEvent.AXIS_THROTTLE,
            MotionEvent.AXIS_RUDDER,
            MotionEvent.AXIS_WHEEL,
            MotionEvent.AXIS_GAS,
            MotionEvent.AXIS_BRAKE
        )

        fun fromMotionEvent(event: MotionEvent): MotionEventRecord {
            val values = GAMEPAD_AXES.associateWith { axis ->
                event.getAxisValue(axis)
            }.filter { (_, v) -> v != 0f }
            return MotionEventRecord(
                deviceId = event.deviceId,
                source = event.source,
                axisValues = values
            )
        }
    }
}
