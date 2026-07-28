package com.dougretrogames.gamepadklfixer

import android.view.KeyEvent
import android.view.MotionEvent
import com.dougretrogames.gamepadklfixer.kl.KlFileGenerator
import com.dougretrogames.gamepadklfixer.model.GamepadDevice
import com.dougretrogames.gamepadklfixer.model.KeyEventRecord
import com.dougretrogames.gamepadklfixer.model.MotionEventRecord
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
                action = KeyEvent.ACTION_DOWN,
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
        val name1 = testDevice.klFileName
        val name2 = testDevice.klFileName
        assertEquals(name1, name2)
    }

    // ---- Profile-specific tests ----

    @Test
    fun `OFFICIAL profile uses scan codes 96 97 99 100 for face buttons`() {
        val content = KlFileGenerator.generateDefault(
            testDevice,
            KlFileGenerator.Profile.OFFICIAL_SWITCH_PRO
        )
        assertTrue("Should contain key 96 BUTTON_B", content.contains("key 96   BUTTON_B"))
        assertTrue("Should contain key 97 BUTTON_A", content.contains("key 97   BUTTON_A"))
        assertTrue("Should contain key 99 BUTTON_Y", content.contains("key 99   BUTTON_Y"))
        assertTrue("Should contain key 100 BUTTON_X", content.contains("key 100   BUTTON_X"))
    }

    @Test
    fun `GENERIC profile uses scan codes 189 190 191 188 for face buttons`() {
        val content = KlFileGenerator.generateDefault(
            testDevice,
            KlFileGenerator.Profile.GENERIC
        )
        assertTrue("Should contain key 189 BUTTON_A", content.contains("key 189   BUTTON_A"))
        assertTrue("Should contain key 190 BUTTON_B", content.contains("key 190   BUTTON_B"))
        assertTrue("Should contain key 191 BUTTON_Y", content.contains("key 191   BUTTON_Y"))
        assertTrue("Should contain key 188 BUTTON_X", content.contains("key 188   BUTTON_X"))
    }

    @Test
    fun `default profile without capture treats L2 R2 as digital buttons`() {
        val content = KlFileGenerator.generateDefault(testDevice)
        assertTrue("Should contain key 192 BUTTON_L2 (generic L2)", content.contains("key 192   BUTTON_L2"))
        assertTrue("Should contain key 193 BUTTON_R2 (generic R2)", content.contains("key 193   BUTTON_R2"))
        assertFalse("Should NOT contain LTRIGGER axis when not captured", content.contains("LTRIGGER"))
        assertFalse("Should NOT contain RTRIGGER axis when not captured", content.contains("RTRIGGER"))
    }

    @Test
    fun `captured motion event with AXIS_BRAKE promotes L2 R2 to analog axes`() {
        val motions = listOf(
            MotionEventRecord(
                deviceId = 1,
                source = 0,
                axisValues = mapOf(MotionEvent.AXIS_BRAKE to 0.7f)
            )
        )
        val content = KlFileGenerator.generateFromCapture(testDevice, emptyList(), motions)
        assertTrue("Should contain LTRIGGER axis", content.contains("LTRIGGER"))
        assertTrue("Should contain RTRIGGER axis", content.contains("RTRIGGER"))
        assertFalse("Should NOT contain key BUTTON_L2 when axis captured", content.contains("BUTTON_L2"))
        assertFalse("Should NOT contain key BUTTON_R2 when axis captured", content.contains("BUTTON_R2"))
    }

    @Test
    fun `OFFICIAL profile default with no capture includes key 104 105 for L2 R2`() {
        val content = KlFileGenerator.generateDefault(
            testDevice,
            KlFileGenerator.Profile.OFFICIAL_SWITCH_PRO
        )
        assertTrue("Should contain key 104 BUTTON_L2 (official L2)", content.contains("key 104   BUTTON_L2"))
        assertTrue("Should contain key 105 BUTTON_R2 (official R2)", content.contains("key 105   BUTTON_R2"))
        assertFalse("Should NOT contain LTRIGGER axis when not captured", content.contains("LTRIGGER"))
    }
}
