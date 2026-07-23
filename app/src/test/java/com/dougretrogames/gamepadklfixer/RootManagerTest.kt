package com.dougretrogames.gamepadklfixer

import com.dougretrogames.gamepadklfixer.root.RootManager
import org.junit.Assert.*
import org.junit.Test

class RootManagerTest {

    @Test
    fun `KL_SYSTEM_PATH is correct`() {
        assertEquals("/system/usr/keylayout", RootManager.KL_SYSTEM_PATH)
    }

    @Test
    fun `KL_BACKUP_SUFFIX is dot bak`() {
        assertEquals(".bak", RootManager.KL_BACKUP_SUFFIX)
    }

    @Test
    fun `RootResult default error is empty string`() {
        val result = RootManager.RootResult(success = true, output = "uid=0")
        assertEquals("", result.error)
    }

    @Test
    fun `RootResult success false when error present`() {
        val result = RootManager.RootResult(success = false, output = "", error = "Permission denied")
        assertFalse(result.success)
        assertTrue(result.error.contains("Permission denied"))
    }
}
