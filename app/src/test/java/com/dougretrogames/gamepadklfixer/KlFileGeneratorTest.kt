package com.dougretrogames.gamepadklfixer

import com.dougretrogames.gamepadklfixer.kl.KlFileGenerator
import com.dougretrogames.gamepadklfixer.model.GamepadDevice
import com.dougretrogames.gamepadklfixer.model.KeyEventRecord
import org.junit.Assert.*
import org.junit.Test

class KlFileGeneratorTest {

    private val testDevice = GamepadDevice(
        id = 1,
        name = "Test Gamepad",
        vendorId = 0x045E,
        productId = 0x028E,
        sources = 0,
        descriptor = "test_descriptor"
    )

    @Test
    fun `vendorHex formats correctly`() {
        assertEquals("045E", testDevice.vendorHex)
    }

    @Test
    fun `productHex formats correctly`() {
        assertEquals("028E", testDevice.productHex)
    }

    @Test
    fun `klFileName has correct format`() {
        assertEquals("Vendor_045E_Product_028E.kl", testDevice.klFileName)
    }

    @Test
    fun `generateDefault produces valid KL content`() {
        val content = KlFileGenerator.generateDefault(testDevice)
        assertTrue("Should contain device name", content.contains("Test Gamepad"))
        assertTrue("Should contain vendor hex", content.contains("045E"))
        assertTrue("Should contain product hex", content.contains("028E"))
        assertTrue("Should have key mapping", content.contains("key "))
        assertTrue("Should have axis mapping", content.contains("axis "))
    }

    @Test
    fun `generateDefault content passes validation`() {
        val content = KlFileGenerator.generateDefault(testDevice)
        assertTrue(KlFileGenerator.validate(content))
    }

    @Test
    fun `generateFromCapture includes captured scan codes`() {
        val captured = listOf(
            KeyEventRecord(
                keyCode = 96, // KEYCODE_BUTTON_A
                scanCode = 0x130,
                action = android.view.KeyEvent.ACTION_DOWN,
                deviceId = 1,
                metaState = 0
            )
        )
        val content = KlFileGenerator.generateFromCapture(testDevice, captured)
        assertTrue("Should contain captured key", content.contains("0x130") || content.contains("304"))
    }

    @Test
    fun `validate returns false for empty content`() {
        assertFalse(KlFileGenerator.validate("# comment only\n"))
    }

    @Test
    fun `validate returns false for blank string`() {
        assertFalse(KlFileGenerator.validate(""))
    }

    @Test
    fun `vendorId zero formats as 0000`() {
        val device = testDevice.copy(vendorId = 0)
        assertEquals("0000", device.vendorHex)
    }

    @Test
    fun `productId max value formats correctly`() {
        val device = testDevice.copy(productId = 0xFFFF)
        assertEquals("FFFF", device.productHex)
        assertEquals("Vendor_045E_Product_FFFF.kl", device.klFileName)
    }

    @Test
    fun `klFileName does not change for same device`() {
        // KL filename must be deterministic
        val name1 = testDevice.klFileName
        val name2 = testDevice.klFileName
        assertEquals(name1, name2)
    }
}
