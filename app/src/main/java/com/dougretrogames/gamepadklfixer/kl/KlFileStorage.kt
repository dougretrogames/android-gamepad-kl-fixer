package com.dougretrogames.gamepadklfixer.kl

import android.content.Context
import com.dougretrogames.gamepadklfixer.model.GamepadDevice
import java.io.File

/**
 * Handles saving and loading .kl files to/from the app's private storage.
 * Files are stored in Context.filesDir/keylayout/
 */
object KlFileStorage {

    private const val KL_DIR = "keylayout"

    fun getKlDirectory(context: Context): File {
        return File(context.filesDir, KL_DIR).also { it.mkdirs() }
    }

    /**
     * Saves generated KL content to the app private storage.
     * @return The File that was written
     */
    fun saveKlFile(context: Context, device: GamepadDevice, content: String): File {
        val dir = getKlDirectory(context)
        val file = File(dir, device.klFileName)
        file.writeText(content, Charsets.UTF_8)
        return file
    }

    /**
     * Reads a saved KL file from app private storage.
     */
    fun readKlFile(context: Context, klFileName: String): String? {
        val file = File(getKlDirectory(context), klFileName)
        return if (file.exists()) file.readText(Charsets.UTF_8) else null
    }

    /**
     * Lists all .kl files saved in app private storage.
     */
    fun listSavedKlFiles(context: Context): List<File> {
        return getKlDirectory(context).listFiles { f -> f.extension == "kl" }?.toList() ?: emptyList()
    }

    /**
     * Deletes a KL file from app private storage.
     */
    fun deleteKlFile(context: Context, klFileName: String): Boolean {
        return File(getKlDirectory(context), klFileName).delete()
    }
}
