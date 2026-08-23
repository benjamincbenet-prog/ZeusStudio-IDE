package com.example.zeus.engine

/**
 * Handler for `zeus doctor`.
 *
 * Performs real diagnostics based on runtime context rather than hardcoded success.
 * Each check emits a PASS (✓), WARN (▲), or FAIL (✖) line with a concise remediation hint.
 */
internal object DoctorCommandHandler {

    private enum class CheckStatus { PASS, WARN, FAIL }

    private data class Check(val label: String, val status: CheckStatus, val detail: String)

    fun execute(args: List<String>, context: DispatcherContext): CommandResult {
        val events = mutableListOf<CommandEvent>()
        events += CommandEvent.Started("doctor")
        events += CommandEvent.Stream("🩺 Running zeus doctor diagnostics…")

        val checks = mutableListOf<Check>()

        // 1. Bluetooth hardware availability
        checks += if (context.isBluetoothAvailable) {
            Check("Bluetooth hardware", CheckStatus.PASS, "adapter present")
        } else {
            Check(
                "Bluetooth hardware", CheckStatus.FAIL,
                "No Bluetooth adapter detected — BLE features unavailable"
            )
        }

        // 2. Bluetooth enabled
        checks += when {
            !context.isBluetoothAvailable ->
                Check("Bluetooth enabled", CheckStatus.FAIL, "N/A — hardware missing")
            context.isBluetoothEnabled ->
                Check("Bluetooth enabled", CheckStatus.PASS, "adapter is ON")
            else ->
                Check(
                    "Bluetooth enabled", CheckStatus.WARN,
                    "Bluetooth is OFF — enable in device settings to use BLE bridge"
                )
        }

        // 3. BLE permissions
        checks += if (context.hasBlePermissions) {
            Check("BLE permissions", CheckStatus.PASS, "BLUETOOTH_SCAN + BLUETOOTH_CONNECT granted")
        } else {
            Check(
                "BLE permissions", CheckStatus.FAIL,
                "BLUETOOTH_SCAN / BLUETOOTH_CONNECT not granted — grant in App Settings → Permissions"
            )
        }

        // 4. Build toolchain
        checks += if (ZeusCompiler.isToolchainConfigured()) {
            Check("Build toolchain", CheckStatus.PASS, "compiler ready")
        } else {
            Check(
                "Build toolchain", CheckStatus.FAIL,
                "No Zepp OS build toolchain configured — set toolchain path in settings"
            )
        }

        // 5. Signing / config prerequisites (check if project has an app ID)
        checks += if (context.project.appId.isNotBlank()) {
            Check("Project app ID", CheckStatus.PASS, "appId = ${context.project.appId}")
        } else {
            Check(
                "Project app ID", CheckStatus.WARN,
                "No app ID configured in project — set a Zepp OS app ID before signing"
            )
        }

        // 6. Device connection
        checks += when {
            context.isBleConnected && context.connectedDeviceMac != null ->
                Check(
                    "Device connection", CheckStatus.PASS,
                    "Connected to ${context.connectedDeviceName ?: "device"} (${context.connectedDeviceMac})"
                )
            !context.isBluetoothAvailable || !context.isBluetoothEnabled ->
                Check("Device connection", CheckStatus.FAIL, "Bluetooth unavailable — cannot connect")
            else ->
                Check(
                    "Device connection", CheckStatus.WARN,
                    "No device connected — use 'zeus bridge --scan' to find and connect a watch"
                )
        }

        // Emit checks
        for (check in checks) {
            val icon = when (check.status) {
                CheckStatus.PASS -> "✓"
                CheckStatus.WARN -> "▲"
                CheckStatus.FAIL -> "✖"
            }
            events += CommandEvent.Stream("  $icon ${check.label}: ${check.detail}", isError = check.status == CheckStatus.FAIL)
        }

        val failures = checks.count { it.status == CheckStatus.FAIL }
        val warnings = checks.count { it.status == CheckStatus.WARN }

        return when {
            failures > 0 -> {
                val summary = "$failures check(s) failed, $warnings warning(s) — see details above"
                events += CommandEvent.Stream("❌ Doctor found $failures failure(s) and $warnings warning(s).", isError = true)
                events += CommandEvent.Completed(exitCode = 1, summary = summary, errorCategory = ErrorCategory.DEPENDENCY_MISSING)
                CommandResult(events, 1, summary, ErrorCategory.DEPENDENCY_MISSING)
            }
            warnings > 0 -> {
                val summary = "All checks passed with $warnings warning(s)"
                events += CommandEvent.Stream("▲ Doctor completed with $warnings warning(s).")
                events += CommandEvent.Completed(exitCode = 0, summary = summary)
                CommandResult(events, 0, summary)
            }
            else -> {
                val summary = "All ${checks.size} checks passed"
                events += CommandEvent.Stream("🎉 $summary — environment is ready for Zepp OS development!")
                events += CommandEvent.Completed(exitCode = 0, summary = summary)
                CommandResult(events, 0, summary)
            }
        }
    }
}
