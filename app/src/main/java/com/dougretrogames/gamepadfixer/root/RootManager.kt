package com.dougretrogames.gamepadfixer.root

import com.dougretrogames.gamepadfixer.model.RootResult
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * Handles all root-privileged shell operations:
 * - Checking su availability
 * - Installing .kl files to /system/usr/keylayout/
 * - Backing up existing .kl files
 * - Restoring backups
 */
class RootManager {

    companion object {
        const val KL_SYSTEM_PATH = "/system/usr/keylayout/"
        const val BACKUP_SUFFIX = ".bak"
    }

    /**
     * Checks whether the device has root access (su binary available).
     */
    fun checkRoot(): Boolean {
        return try {
            val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val output = proc.inputStream.bufferedReader().readText()
            proc.waitFor()
            output.contains("uid=0")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Executes a shell command with root privileges.
     * Returns RootResult with stdout output or error.
     */
    fun execRoot(command: String): RootResult<String> {
        return try {
            val proc = Runtime.getRuntime().exec("su")
            val writer = OutputStreamWriter(proc.outputStream)
            writer.write("$command\n")
            writer.write("exit\n")
            writer.flush()
            writer.close()

            val stdout = BufferedReader(InputStreamReader(proc.inputStream)).readText()
            val stderr = BufferedReader(InputStreamReader(proc.errorStream)).readText()
            val exitCode = proc.waitFor()

            if (exitCode == 0) {
                RootResult.Success(stdout.trim(), stdout.trim())
            } else {
                RootResult.Error(stderr.trim().ifEmpty { "Command failed with exit code $exitCode" })
            }
        } catch (e: Exception) {
            RootResult.Error(e.message ?: "Unknown error", e)
        }
    }

    /**
     * Backs up an existing .kl file in the system partition before replacing.
     * Creates a .bak copy next to the original file.
     */
    fun backupKlFile(fileName: String): RootResult<String> {
        val src = "$KL_SYSTEM_PATH$fileName"
        val dst = "$KL_SYSTEM_PATH${fileName}$BACKUP_SUFFIX"
        // Check if file exists first
        val checkResult = execRoot("[ -f '$src' ] && echo EXISTS || echo MISSING")
        if (checkResult is RootResult.Success && checkResult.data == "MISSING") {
            return RootResult.Success("No existing file to backup")
        }
        return execRoot("cp '$src' '$dst' && echo OK")
    }

    /**
     * Installs a .kl file from app's private storage to the system partition.
     * Remounts /system as rw before writing and ro afterwards.
     */
    fun installKlFile(srcPath: String, fileName: String): RootResult<String> {
        val dst = "$KL_SYSTEM_PATH$fileName"
        val commands = """
            mount -o remount,rw /system
            cp '$srcPath' '$dst'
            chmod 644 '$dst'
            chown root:root '$dst'
            mount -o remount,ro /system
            echo OK
        """.trimIndent()
        return execRoot(commands)
    }

    /**
     * Restores a previously backed up .kl file.
     */
    fun restoreBackup(fileName: String): RootResult<String> {
        val backup = "$KL_SYSTEM_PATH${fileName}$BACKUP_SUFFIX"
        val original = "$KL_SYSTEM_PATH$fileName"
        val commands = """
            mount -o remount,rw /system
            [ -f '$backup' ] && cp '$backup' '$original' && echo RESTORED || echo NO_BACKUP
            mount -o remount,ro /system
        """.trimIndent()
        return execRoot(commands)
    }

    /**
     * Lists all .kl files currently installed in the system keylayout directory.
     */
    fun listSystemKlFiles(): RootResult<List<String>> {
        return when (val result = execRoot("ls $KL_SYSTEM_PATH*.kl 2>/dev/null")) {
            is RootResult.Success -> {
                val files = result.data.lines().filter { it.isNotBlank() }
                RootResult.Success(files)
            }
            is RootResult.Error -> result
            RootResult.NoRoot -> RootResult.NoRoot
        }
    }

    /**
     * Reads the content of an installed .kl file from the system partition.
     */
    fun readSystemKlFile(fileName: String): RootResult<String> {
        return execRoot("cat $KL_SYSTEM_PATH$fileName")
    }
}
