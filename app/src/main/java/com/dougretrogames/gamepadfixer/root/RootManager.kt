package com.dougretrogames.gamepadfixer.root

import android.content.Context
import com.dougretrogames.gamepadfixer.model.RootOperationResult
import java.io.File

/**
 * High-level root operations for .kl file management.
 *
 * All public methods are suspend-friendly (blocking) — call from IO dispatcher.
 */
class RootManager(private val context: Context) {

    companion object {
        private const val KL_SYSTEM_DIR = "/system/usr/keylayout"
        private const val KL_BACKUP_DIR = "/sdcard/GamepadKlFixer/backup"
    }

    /** @return true if `su` is available on this device. */
    fun isRooted(): Boolean = RootShell.isRooted()

    /**
     * Copies a .kl file from internal storage to the system keylayout directory.
     *
     * Steps performed:
     * 1. mount -o remount,rw /system
     * 2. cp <src> /system/usr/keylayout/<filename>
     * 3. chmod 644 <destination>
     * 4. mount -o remount,ro /system
     *
     * @param localFile  File in app's internal storage ready to be installed.
     * @param klFileName Destination filename, e.g. "Vendor_045E_Product_028E.kl"
     */
    fun installKlFile(localFile: File, klFileName: String): RootOperationResult {
        val dest = "$KL_SYSTEM_DIR/$klFileName"
        val src = localFile.absolutePath
        val commands = listOf(
            "mount -o remount,rw /system 2>/dev/null || true",
            "cp '$src' '$dest'",
            "chmod 644 '$dest'",
            "chown root:root '$dest'",
            "mount -o remount,ro /system 2>/dev/null || true"
        )
        return RootShell.execute(commands)
    }

    /**
     * Backs up the existing .kl file from /system to /sdcard/GamepadKlFixer/backup/.
     *
     * @param klFileName Filename to back up.
     */
    fun backupKlFile(klFileName: String): RootOperationResult {
        val src = "$KL_SYSTEM_DIR/$klFileName"
        val backupPath = "$KL_BACKUP_DIR/$klFileName"
        val commands = listOf(
            "mkdir -p '$KL_BACKUP_DIR'",
            "[ -f '$src' ] && cp '$src' '$backupPath' || echo 'no_original'"
        )
        return RootShell.execute(commands)
    }

    /**
     * Restores a previously backed-up .kl file back to /system.
     *
     * @param klFileName Filename to restore.
     */
    fun restoreKlFile(klFileName: String): RootOperationResult {
        val backupPath = "$KL_BACKUP_DIR/$klFileName"
        val dest = "$KL_SYSTEM_DIR/$klFileName"
        val commands = listOf(
            "[ -f '$backupPath' ] || { echo 'backup_not_found'; exit 1; }",
            "mount -o remount,rw /system 2>/dev/null || true",
            "cp '$backupPath' '$dest'",
            "chmod 644 '$dest'",
            "chown root:root '$dest'",
            "mount -o remount,ro /system 2>/dev/null || true"
        )
        return RootShell.execute(commands)
    }

    /**
     * Lists all .kl files present in /system/usr/keylayout.
     *
     * @return Success with newline-separated filenames, or Failure/NoRoot.
     */
    fun listSystemKlFiles(): RootOperationResult {
        return RootShell.execute("ls '$KL_SYSTEM_DIR'/*.kl 2>/dev/null || echo 'none'")
    }

    /**
     * Reads a .kl file from /system and returns its content as a string.
     *
     * @param klFileName Filename to read.
     */
    fun readSystemKlFile(klFileName: String): RootOperationResult {
        val path = "$KL_SYSTEM_DIR/$klFileName"
        return RootShell.execute("cat '$path'")
    }

    /**
     * Writes content directly to a .kl file in /system.
     * Content is written via a temp file in the app's cache dir.
     *
     * @param klFileName Filename to write.
     * @param content    Text content for the .kl file.
     */
    fun writeSystemKlFile(klFileName: String, content: String): RootOperationResult {
        val tmpFile = File(context.cacheDir, klFileName)
        return try {
            tmpFile.writeText(content)
            installKlFile(tmpFile, klFileName)
        } finally {
            tmpFile.delete()
        }
    }
}
