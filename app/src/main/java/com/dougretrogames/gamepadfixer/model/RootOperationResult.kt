package com.dougretrogames.gamepadfixer.model

/**
 * Sealed result type for root shell operations.
 */
sealed class RootOperationResult {
    /** Operation succeeded. [output] holds stdout if any. */
    data class Success(val output: String = "") : RootOperationResult()

    /** Operation failed. [error] holds the reason. */
    data class Failure(val error: String) : RootOperationResult()

    /** Device is not rooted or `su` is unavailable. */
    object NoRoot : RootOperationResult()
}
