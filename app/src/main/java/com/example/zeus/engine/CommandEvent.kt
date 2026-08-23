package com.example.zeus.engine

/**
 * Typed failure reason/category for terminal command execution.
 */
enum class ErrorCategory {
    INVALID_ARGS,
    DEPENDENCY_MISSING,
    PERMISSION_DENIED,
    DEVICE_NOT_FOUND,
    BACKEND_NOT_CONFIGURED,
    IO_ERROR,
    NOT_IMPLEMENTED,
    TIMEOUT
}

/**
 * Structured execution events emitted by a command handler.
 */
sealed class CommandEvent {
    /** Emitted when a command begins executing. */
    data class Started(val command: String, val timestamp: Long = System.currentTimeMillis()) : CommandEvent()

    /** A streaming output line (stdout or stderr level). */
    data class Stream(
        val message: String,
        val isError: Boolean = false,
        val timestamp: Long = System.currentTimeMillis()
    ) : CommandEvent()

    /** Emitted when the command finishes. exitCode 0 = success. */
    data class Completed(
        val exitCode: Int,
        val summary: String,
        val errorCategory: ErrorCategory? = null,
        val timestamp: Long = System.currentTimeMillis()
    ) : CommandEvent()
}

/** Full result of running a command, including the ordered event sequence. */
data class CommandResult(
    val events: List<CommandEvent>,
    val exitCode: Int,
    val summary: String,
    val errorCategory: ErrorCategory? = null
)
