package com.dougretrogames.gamepadklfixer

import com.dougretrogames.gamepadklfixer.model.GamepadDevice
import org.junit.Assert.*
import org.junit.Test

class GamepadDeviceTest {

    @Test
    fun `vendorHex pads with leading zeros`() {
        val device = GamepadDevice(1, "Test", 0x001A, 0x0002, 0, "desc")
        assertEquals("001A", device.vendorHex)
        assertEquals("0002", device.productHex)
    }

    @Test
    fun `klFileName format matches Android system convention`() {
        val device = GamepadDevice(1, "Xbox Controller", 0x045E, 0x02FD, 0, "desc")
        // Android expects: Vendor_XXXX_Product_XXXX.kl
        val expected = "Vendor_045E_Product_02FD.kl"
        assertEquals(expected, device.klFileName)
    }

    @Test
    fun `data class equality works correctly`() {
        val a = GamepadDevice(1, "Pad", 0x045E, 0x028E, 0, "d")
        val b = GamepadDevice(1, "Pad", 0x045E, 0x028E, 0, "d")
        assertEquals(a, b)
    }

    @Test
    fun `copy preserves unchanged fields`() {
        val original = GamepadDevice(1, "Pad", 0x045E, 0x028E, 5, "d")
        val copy = original.copy(name = "New Pad")
        assertEquals(original.vendorId, copy.vendorId)
        assertEquals(original.productId, copy.productId)
        assertEquals("New Pad", copy.name)
    }
}
