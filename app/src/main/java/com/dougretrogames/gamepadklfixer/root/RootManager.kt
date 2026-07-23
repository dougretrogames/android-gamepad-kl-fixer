package com.dougretrogames.gamepadklfixer.root

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * Handles root-level operations required to install .kl files into the system partition.
 *
 * KL files must reside in /system/usr/keylayout/ and the partition must be
 * remounted read-write to write them. This class encapsulates all `su` interactions.
 *
 * IMPORTANT: These operations require root access (su). On non-rooted devices,
 * all methods will return failure results.
 */
object RootManager {

    const val KL_SYSTEM_PATH = "/system/usr/keylayout"
    const val KL_BACKUP_SUFFIX = ".bak"

    data class RootResult(
        val success: Boolean,
        val output: String,
        val error: String = ""
    )

    /**
     * Checks whether root (su) access is available on this device.
     * Runs `su -c id` and checks for uid=0.
     */
    suspend fun checkRootAccess(): RootResult = withContext(Dispatchers.IO) {
        runSuCommand("id")
    }

    /**
     * Lists all .kl files currently installed in the system keylayout directory.
     */
    suspend fun listInstalledKlFiles(): RootResult = withContext(Dispatchers.IO) {
        runSuCommand("ls -la $KL_SYSTEM_PATH/*.kl 2>/dev/null || echo '(none)'")
    }

    /**
     * Creates a backup of an existing .kl file before replacing it.
     * Backup is stored alongside the original with .bak extension.
     *
     * @param klFileName e.g. "Vendor_045E_Product_028E.kl"
     */
    suspend fun backupKlFile(klFileName: String): RootResult = withContext(Dispatchers.IO) {
        val source = "$KL_SYSTEM_PATH/$klFileName"
        val backup = "$source$KL_BACKUP_SUFFIX"
        runSuCommand("[ -f '$source' ] && cp '$source' '$backup' && echo 'backup_ok' || echo 'no_original'")
    }

    /**
     * Installs a .kl file from the app's private storage into /system/usr/keylayout/.
     * Remounts /system as read-write, copies the file, sets correct permissions,
     * then remounts read-only again.
     *
     * @param sourceFile  The local File object pointing to the generated .kl file
     * @param klFileName  Target filename in the system directory
     */
    suspend fun installKlFile(sourceFile: File, klFileName: String): RootResult =
        withContext(Dispatchers.IO) {
            val dest = "$KL_SYSTEM_PATH/$klFileName"
            val srcPath = sourceFile.absolutePath
            val commands = """
                mount -o remount,rw /system
                cp '$srcPath' '$dest'
                chmod 644 '$dest'
                chown root:root '$dest'
                mount -o remount,ro /system
                echo 'install_ok'
            """.trimIndent()
            runSuCommand(commands)
        }

    /**
     * Restores a previously backed-up .kl file.
     *
     * @param klFileName e.g. "Vendor_045E_Product_028E.kl"
     */
    suspend fun restoreKlFile(klFileName: String): RootResult = withContext(Dispatchers.IO) {
        val dest = "$KL_SYSTEM_PATH/$klFileName"
        val backup = "$dest$KL_BACKUP_SUFFIX"
        val commands = """
            mount -o remount,rw /system
            [ -f '$backup' ] && cp '$backup' '$dest' && echo 'restore_ok' || echo 'no_backup'
            chmod 644 '$dest'
            mount -o remount,ro /system
        """.trimIndent()
        runSuCommand(commands)
    }

    /**
     * Removes a .kl file from the system partition.
     *
     * @param klFileName Filename to remove
     */
    suspend fun removeKlFile(klFileName: String): RootResult = withContext(Dispatchers.IO) {
        val target = "$KL_SYSTEM_PATH/$klFileName"
        val commands = """
            mount -o remount,rw /system
            rm -f '$target'
            mount -o remount,ro /system
            echo 'remove_ok'
        """.trimIndent()
        runSuCommand(commands)
    }

    /**
     * Executes one or more shell commands as root via `su -c`.
     * Multiple commands can be separated by newlines.
     */
    private fun runSuCommand(commands: String): RootResult {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su"))
            val writer = OutputStreamWriter(process.outputStream)
            writer.write(commands)
            writer.write("\nexit\n")
            writer.flush()
            writer.close()

            val stdout = process.inputStream.bufferedReader().readText()
            val stderr = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            RootResult(
                success = exitCode == 0 && !stdout.contains("Permission denied"),
                output = stdout.trim(),
                error = stderr.trim()
            )
        } catch (e: Exception) {
            RootResult(success = false, output = "", error = e.message ?: "Unknown error")
        }
    }
}
