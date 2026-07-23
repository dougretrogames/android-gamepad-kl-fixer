package com.dougretrogames.gamepadfixer.repository

import android.content.Context
import com.dougretrogames.gamepadfixer.generator.KlFileGenerator
import com.dougretrogames.gamepadfixer.model.GamepadDevice
import com.dougretrogames.gamepadfixer.model.KeyMapping
import com.dougretrogames.gamepadfixer.model.KlFile
import com.dougretrogames.gamepadfixer.model.RootOperationResult
import com.dougretrogames.gamepadfixer.root.RootManager
import java.io.File

/**
 * Single source of truth for all gamepad and .kl file data.
 *
 * Coordinates between [DeviceDiagnostics], [KlFileGenerator] and [RootManager].
 */
class GamepadRepository(private val context: Context) {

    private val rootManager = RootManager(context)

    // -----------------------------------------------------------------------
    // Device discovery
    // -----------------------------------------------------------------------

    fun getConnectedGamepads(): List<GamepadDevice> =
        DeviceDiagnostics.getConnectedGamepads()

    fun getAllInputDevices(): List<GamepadDevice> =
        DeviceDiagnostics.getAllInputDevices()

    // -----------------------------------------------------------------------
    // KL file generation
    // -----------------------------------------------------------------------

    /**
     * Generates the .kl file content for [device] and saves it to internal storage.
     *
     * @return The generated [KlFile] including its local [File] path.
     */
    fun generateAndSaveKlFile(
        device: GamepadDevice,
        customMappings: List<KeyMapping> = emptyList()
    ): Pair<KlFile, File> {
        val klFile = KlFileGenerator.generate(device, customMappings)
        val content = klFile.generateContent()
        val localFile = saveKlFileLocally(device.klFileName, content)
        return klFile to localFile
    }

    /**
     * Writes .kl content to the app's files directory.
     *
     * @param fileName e.g. "Vendor_045E_Product_028E.kl"
     * @param content  Text to write.
     * @return The [File] reference.
     */
    fun saveKlFileLocally(fileName: String, content: String): File {
        val dir = File(context.filesDir, "keylayouts")
        dir.mkdirs()
        val file = File(dir, fileName)
        file.writeText(content)
        return file
    }

    /**
     * Reads a previously saved local .kl file.
     *
     * @return File content or null if not found.
     */
    fun readLocalKlFile(fileName: String): String? {
        val file = File(context.filesDir, "keylayouts/$fileName")
        return if (file.exists()) file.readText() else null
    }

    /**
     * Lists all .kl files saved locally in the app's files directory.
     */
    fun listLocalKlFiles(): List<File> {
        val dir = File(context.filesDir, "keylayouts")
        return dir.listFiles { f -> f.name.endsWith(".kl") }?.toList() ?: emptyList()
    }

    // -----------------------------------------------------------------------
    // Root operations
    // -----------------------------------------------------------------------

    fun isRooted(): Boolean = rootManager.isRooted()

    fun backupKlFile(klFileName: String): RootOperationResult =
        rootManager.backupKlFile(klFileName)

    fun installKlFile(localFile: File, klFileName: String): RootOperationResult =
        rootManager.installKlFile(localFile, klFileName)

    fun restoreKlFile(klFileName: String): RootOperationResult =
        rootManager.restoreKlFile(klFileName)

    fun listSystemKlFiles(): RootOperationResult =
        rootManager.listSystemKlFiles()
}
