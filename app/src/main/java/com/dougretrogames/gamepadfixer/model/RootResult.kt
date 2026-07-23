package com.dougretrogames.gamepadfixer.model

/**
 * Result wrapper for root shell operations.
 */
sealed class RootResult<out T> {
    data class Success<T>(val data: T, val output: String = "") : RootResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : RootResult<Nothing>()
    object NoRoot : RootResult<Nothing>()
}
