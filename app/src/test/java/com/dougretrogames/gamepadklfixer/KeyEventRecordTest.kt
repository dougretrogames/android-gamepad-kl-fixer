package com.dougretrogames.gamepadklfixer

import android.view.KeyEvent
import com.dougretrogames.gamepadklfixer.model.KeyEventRecord
import org.junit.Assert.*
import org.junit.Test

class KeyEventRecordTest {

    @Test
    fun `actionName returns DOWN for ACTION_DOWN`() {
        val record = KeyEventRecord(96, 0x130, KeyEvent.ACTION_DOWN, 1, 0)
        assertEquals("DOWN", record.actionName)
    }

    @Test
    fun `actionName returns UP for ACTION_UP`() {
        val record = KeyEventRecord(96, 0x130, KeyEvent.ACTION_UP, 1, 0)
        assertEquals("UP", record.actionName)
    }

    @Test
    fun `actionName returns UNKNOWN for unknown action`() {
        val record = KeyEventRecord(96, 0x130, 99, 1, 0)
        assertTrue(record.actionName.startsWith("UNKNOWN"))
    }

    @Test
    fun `keyCodeName is not empty`() {
        val record = KeyEventRecord(96, 0x130, KeyEvent.ACTION_DOWN, 1, 0)
        assertTrue(record.keyCodeName.isNotEmpty())
    }
}
