package com.dougretrogames.gamepadfixer.root

import com.dougretrogames.gamepadfixer.model.RootOperationResult
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

/**
 * Low-level helper that opens a `su` shell and executes arbitrary commands.
 *
 * All methods are blocking – call from a background thread / coroutine.
 */
object RootShell {

    /**
     * Tests whether `su` is available and grants access.
     * @return true if the device is rooted and su responds.
     */
    fun isRooted(): Boolean {
        return try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val reader = BufferedReader(InputStreamReader(p.inputStream))
            val output = reader.readLine() ?: ""
            p.waitFor()
            output.contains("uid=0")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Executes a list of shell commands as root in a single `su` session.
     *
     * @param commands List of shell commands to execute sequentially.
     * @return [RootOperationResult.Success] with combined stdout,
     *         [RootOperationResult.Failure] on non-zero exit or exception,
     *         [RootOperationResult.NoRoot] when su is unavailable.
     */
    fun execute(commands: List<String>): com.dougretrogames.gamepadfixer.model.RootOperationResult {
        if (!isRooted()) return com.dougretrogames.gamepadfixer.model.RootOperationResult.NoRoot
        return try {
            val process = Runtime.getRuntime().exec("su")
            val stdin = DataOutputStream(process.outputStream)
            commands.forEach { cmd ->
                stdin.writeBytes(cmd + "\n")
                stdin.flush()
            }
            stdin.writeBytes("exit\n")
            stdin.flush()

            val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText()
            val exit = process.waitFor()

            if (exit == 0) {
                com.dougretrogames.gamepadfixer.model.RootOperationResult.Success(stdout.trim())
            } else {
                com.dougretrogames.gamepadfixer.model.RootOperationResult.Failure(
                    "Exit $exit: ${stderr.trim().ifEmpty { stdout.trim() }}"
                )
            }
        } catch (e: Exception) {
            com.dougretrogames.gamepadfixer.model.RootOperationResult.Failure(e.message ?: "Unknown error")
        }
    }

    /** Convenience overload for a single command. */
    fun execute(command: String) = execute(listOf(command))
}
