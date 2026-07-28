package com.dougretrogames.gamepadklfixer.kl

import android.view.KeyEvent
import android.view.MotionEvent
import com.dougretrogames.gamepadklfixer.model.GamepadDevice
import com.dougretrogames.gamepadklfixer.model.KeyEventRecord
import com.dougretrogames.gamepadklfixer.model.MotionEventRecord

/**
 * Generates .kl (KeyLayout) file content for a given gamepad device.
 *
 * The KeyLayout file format is a plain-text file used by Android's input system
 * to map scan codes to key codes. Files are placed at:
 *   /system/usr/keylayout/Vendor_XXXX_Product_XXXX.kl
 *
 * Reference: https://source.android.com/docs/core/interaction/input/key-layout-files
 */
object KlFileGenerator {

    /**
     * Mapping profiles supported by the generator.
     *
     * Each profile defines a set of key/axis mappings that the .kl file will use
     * as the baseline. The user can pick the one that best matches the device.
     */
    enum class Profile {
        OFFICIAL_SWITCH_PRO,
        GENERIC
    }

    /**
     * Default standard gamepad key mapping for the official profile (Switch Pro-style).
     * Uses Android KeyEvent.scanCode values as reported by an original controller.
     *
     * NOTE: L2/R2 are intentionally omitted from this key list. They are emitted
     * dynamically as either `key` (digital) or `axis` (analog) depending on how
     * the device reports them — see [buildKlContent].
     */
    private val OFFICIAL_KEY_MAPPINGS: List<Pair<Int, String>> = listOf(
        96  to "BUTTON_B",
        97  to "BUTTON_A",
        99  to "BUTTON_Y",
        100 to "BUTTON_X",
        102 to "BUTTON_L1",
        103 to "BUTTON_R1",
        106 to "BUTTON_THUMBL",
        107 to "BUTTON_THUMBR",
        108 to "BUTTON_START",
        109 to "BUTTON_SELECT"
    )

    /**
     * Default standard gamepad key mapping for a typical generic USB gamepad.
     * Uses Android KeyEvent.scanCode values commonly reported by generic devices.
     */
    private val GENERIC_KEY_MAPPINGS: List<Pair<Int, String>> = listOf(
        188 to "BUTTON_X",
        189 to "BUTTON_A",
        190 to "BUTTON_B",
        191 to "BUTTON_Y",
        194 to "BUTTON_L1",
        195 to "BUTTON_R1",
        196 to "BUTTON_SELECT",
        197 to "BUTTON_START",
        198 to "BUTTON_THUMBL",
        199 to "BUTTON_THUMBR"
    )

    /**
     * L2/R2 scan codes used when the device reports them as digital buttons.
     * Index 0 = L2, index 1 = R2.
     */
    private fun l2r2KeyMappings(profile: Profile): List<Pair<Int, String>> = when (profile) {
        Profile.OFFICIAL_SWITCH_PRO -> listOf(
            104 to "BUTTON_L2",
            105 to "BUTTON_R2"
        )
        Profile.GENERIC -> listOf(
            192 to "BUTTON_L2",
            193 to "BUTTON_R2"
        )
    }

    /**
     * Axis mappings emitted for every profile (sticks, hat, triggers).
     */
    private val DEFAULT_AXIS_MAPPINGS: List<Triple<String, String, String>> = listOf(
        Triple("ABS_X", "X", "flat 4096"),
        Triple("ABS_Y", "Y", "flat 4096"),
        Triple("ABS_Z", "Z", "flat 4096"),
        Triple("ABS_RZ", "RZ", "flat 4096"),
        Triple("ABS_HAT0X", "HAT_X", ""),
        Triple("ABS_HAT0Y", "HAT_Y", "")
    )

    /**
     * Axis mappings emitted for L2/R2 when the device reports them as analog axes.
     */
    private val L2R2_AXIS_MAPPINGS: List<Triple<String, String, String>> = listOf(
        Triple("ABS_BRAKE", "LTRIGGER", ""),
        Triple("ABS_GAS", "RTRIGGER", "")
    )

    /**
     * Generates KL file content using the selected profile's default mappings.
     *
     * L2/R2 default to [L2r2Mode.BOTH] (button + axis lines): when the user has
     * captured motion data, the analog lines are kept and the button lines are
     * dropped; when only key events were captured (or nothing), the button lines
     * are kept and the analog lines are dropped. This matches the "auto-detect"
     * behavior described in the spec.
     *
     * @param device  The gamepad device to generate for
     * @param profile The mapping profile to use
     * @return String content of the .kl file
     */
    fun generateDefault(device: GamepadDevice, profile: Profile = Profile.GENERIC): String {
        val keyMappings = baseKeyMappings(profile)
        return buildKlContent(
            device = device,
            keyMappings = keyMappings,
            axisMappings = DEFAULT_AXIS_MAPPINGS,
            capturedKeys = emptyList(),
            capturedMotions = emptyList(),
            l2r2Mode = L2r2Mode.AUTO,
            profile = profile
        )
    }

    /**
     * Generates KL file content from captured key/motion events during the test
     * session. The L2/R2 representation is auto-detected:
     *  - if any captured MotionEvent has non-zero AXIS_BRAKE/AXIS_GAS, L2/R2 are
     *    emitted as `axis ... LTRIGGER/RTRIGGER` only;
     *  - otherwise they are emitted as `key ... BUTTON_L2/R2` only.
     *
     * @param device          The gamepad device
     * @param capturedKeys    List of KeyEventRecord from live capture session
     * @param capturedMotions List of MotionEventRecord from live capture session
     * @param profile         The mapping profile whose defaults are used as the baseline
     * @return String content of the .kl file
     */
    fun generateFromCapture(
        device: GamepadDevice,
        capturedKeys: List<KeyEventRecord>,
        capturedMotions: List<MotionEventRecord> = emptyList(),
        profile: Profile = Profile.GENERIC
    ): String {
        val keyMappings = baseKeyMappings(profile)
        return buildKlContent(
            device = device,
            keyMappings = keyMappings,
            axisMappings = DEFAULT_AXIS_MAPPINGS,
            capturedKeys = capturedKeys,
            capturedMotions = capturedMotions,
            l2r2Mode = L2r2Mode.AUTO,
            profile = profile
        )
    }

    private fun baseKeyMappings(profile: Profile): List<Pair<Int, String>> = when (profile) {
        Profile.OFFICIAL_SWITCH_PRO -> OFFICIAL_KEY_MAPPINGS
        Profile.GENERIC -> GENERIC_KEY_MAPPINGS
    }

    /**
     * How L2/R2 should be represented in the generated .kl.
     */
    private enum class L2r2Mode {
        /** Emit axis lines only (analog triggers). */
        AXIS_ONLY,
        /** Emit key lines only (digital triggers). */
        KEY_ONLY,
        /** Decide based on captured MotionEvent data. */
        AUTO
    }

    private fun buildKlContent(
        device: GamepadDevice,
        keyMappings: List<Pair<Int, String>>,
        axisMappings: List<Triple<String, String, String>>,
        capturedKeys: List<KeyEventRecord>,
        capturedMotions: List<MotionEventRecord>,
        l2r2Mode: L2r2Mode,
        profile: Profile
    ): String {
        // Decide L2/R2 representation.
        val triggerAxesPresent = capturedMotions.any { motion ->
            motion.axisValues.keys.any { axis ->
                axis == MotionEvent.AXIS_BRAKE || axis == MotionEvent.AXIS_GAS
            }
        }
        val l2r2AsAxis = when (l2r2Mode) {
            L2r2Mode.AXIS_ONLY -> true
            L2r2Mode.KEY_ONLY -> false
            L2r2Mode.AUTO -> triggerAxesPresent
        }

        val sb = StringBuilder()

        sb.appendLine("# KeyLayout file generated by GamepadKLFixer")
        sb.appendLine("# Device: ${device.name}")
        sb.appendLine("# Vendor ID : ${device.vendorHex} (${device.vendorId})")
        sb.appendLine("# Product ID: ${device.productHex} (${device.productId})")
        sb.appendLine("# Generated : ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())}")
        sb.appendLine("# Reference : https://source.android.com/docs/core/interaction/input/key-layout-files")
        sb.appendLine()

        sb.appendLine("# ----- Key Mappings -----")
        // Add captured keys first (with WAKE flag).
        val capturedScanCodes = mutableSetOf<Int>()
        for (record in capturedKeys.filter { it.action == android.view.KeyEvent.ACTION_DOWN }.distinctBy { it.scanCode }) {
            if (record.scanCode > 0) {
                val keyName = KeyEvent.keyCodeToString(record.keyCode).removePrefix("KEYCODE_")
                sb.appendLine("key ${record.scanCode}   $keyName   WAKE")
                capturedScanCodes.add(record.scanCode)
            }
        }

        // Add base defaults for any scan codes not captured.
        for ((scanCode, keyName) in keyMappings) {
            if (scanCode !in capturedScanCodes) {
                sb.appendLine("key $scanCode   $keyName   WAKE")
            }
        }

        // L2/R2 as digital buttons (only when not representing them as axes).
        if (!l2r2AsAxis) {
            for ((scanCode, keyName) in l2r2KeyMappings(profile)) {
                if (scanCode !in capturedScanCodes) {
                    sb.appendLine("key $scanCode   $keyName")
                }
            }
        }
        sb.appendLine()

        sb.appendLine("# ----- Axis Mappings -----")
        for ((absAxis, motionAxis, opts) in axisMappings) {
            sb.appendLine(
                if (opts.isEmpty()) "axis $absAxis   $motionAxis"
                else "axis $absAxis   $motionAxis   $opts"
            )
        }

        // L2/R2 as analog axes.
        if (l2r2AsAxis) {
            for ((absAxis, motionAxis, opts) in L2R2_AXIS_MAPPINGS) {
                sb.appendLine(
                    if (opts.isEmpty()) "axis $absAxis   $motionAxis"
                    else "axis $absAxis   $motionAxis   $opts"
                )
            }
        }

        return sb.toString()
    }

    /**
     * Returns the axis name string for a MotionEvent axis integer constant.
     */
    fun axisName(axis: Int): String = MotionEvent.axisToString(axis)

    /**
     * Validates that the KL content has at least one key or axis mapping.
     */
    fun validate(content: String): Boolean {
        return content.lines().any { line ->
            val trimmed = line.trim()
            !trimmed.startsWith("#") && (trimmed.startsWith("key ") || trimmed.startsWith("axis "))
        }
    }
}
