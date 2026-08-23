package com.example.zeus.engine

import com.example.zeus.model.ZeusProject

/**
 * Parses a raw terminal command line into tokens, respecting simple quoted strings.
 *
 * Examples:
 *   "zeus build --release"  -> ["zeus", "build", "--release"]
 *   "zeus create \"my app\"" -> ["zeus", "create", "my app"]
 */
internal fun parseCommandLine(input: String): List<String> {
    val tokens = mutableListOf<String>()
    val current = StringBuilder()
    var inQuote = false
    var quoteChar = ' '
    for (ch in input.trim()) {
        when {
            inQuote -> {
                if (ch == quoteChar) {
                    inQuote = false
                } else {
                    current.append(ch)
                }
            }
            ch == '"' || ch == '\'' -> {
                inQuote = true
                quoteChar = ch
            }
            ch.isWhitespace() -> {
                if (current.isNotEmpty()) {
                    tokens.add(current.toString())
                    current.clear()
                }
            }
            else -> current.append(ch)
        }
    }
    if (current.isNotEmpty()) tokens.add(current.toString())
    return tokens
}

/**
 * Command dispatcher: routes parsed commands to dedicated handlers and
 * returns a structured [CommandResult] with event sequence.
 *
 * Runtime context (Bluetooth state, connected device, etc.) is provided via
 * [DispatcherContext] so handlers can make truthful decisions.
 */
object CommandDispatcher {

    /**
     * Executes [commandLine] in the given [context].
     *
     * Returns a [CommandResult] whose [CommandResult.events] list always begins with
     * [CommandEvent.Started] and ends with [CommandEvent.Completed].
     */
    fun dispatch(commandLine: String, context: DispatcherContext): CommandResult {
        val tokens = parseCommandLine(commandLine)
        if (tokens.isEmpty()) {
            return emptyResult()
        }

        val first = tokens[0].lowercase()
        val (cmd, args) = if (first == "zeus") {
            val sub = tokens.getOrNull(1)?.lowercase() ?: ""
            sub to tokens.drop(2)
        } else {
            first to tokens.drop(1)
        }

        return when (cmd) {
            "build" -> BuildCommandHandler.execute(args, context)
            "bridge" -> BridgeCommandHandler.execute(args, context)
            "doctor" -> DoctorCommandHandler.execute(args, context)
            // Remaining commands are still handled by ZeusCliRunner for backward compat.
            // Unknown commands get an explicit non-zero exit and actionable help.
            else -> unknownCommand(cmd)
        }
    }

    private fun emptyResult(): CommandResult = CommandResult(emptyList(), 0, "")

    private fun unknownCommand(cmd: String): CommandResult {
        val events = mutableListOf<CommandEvent>()
        events += CommandEvent.Started(cmd)
        events += CommandEvent.Stream(
            "Unknown command: '$cmd'. Run 'zeus help' for the list of available commands.",
            isError = true
        )
        events += CommandEvent.Completed(
            exitCode = 1,
            summary = "Unknown command: '$cmd'",
            errorCategory = ErrorCategory.INVALID_ARGS
        )
        return CommandResult(events, 1, "Unknown command: '$cmd'", ErrorCategory.INVALID_ARGS)
    }
}

/**
 * Runtime context passed into the dispatcher so handlers can make truthful decisions.
 */
data class DispatcherContext(
    /** The active project being built or installed. */
    val project: ZeusProject,
    /** Whether there is a real BLE device currently connected. */
    val isBleConnected: Boolean,
    /** MAC address of the connected device, if any. */
    val connectedDeviceMac: String?,
    /** Name of the connected device, if any. */
    val connectedDeviceName: String?,
    /** Whether the Android Bluetooth adapter is enabled. */
    val isBluetoothEnabled: Boolean,
    /** Whether the host device has a Bluetooth adapter at all. */
    val isBluetoothAvailable: Boolean,
    /** Whether BLUETOOTH_SCAN / BLUETOOTH_CONNECT runtime permissions are granted. */
    val hasBlePermissions: Boolean,
    /** The artifact from a prior build that can be installed. Null when none. */
    val lastBuildArtifact: com.example.zeus.model.ZabPackage?
)
