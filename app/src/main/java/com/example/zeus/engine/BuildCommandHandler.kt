package com.example.zeus.engine

/**
 * Handler for the `zeus build [--release]` command.
 *
 * Success only when a real build artifact exists. The underlying [ZeusCompiler]
 * already returns `isSuccess = false` when no toolchain is configured, so this
 * handler simply propagates that truthfully.
 */
internal object BuildCommandHandler {

    fun execute(args: List<String>, context: DispatcherContext): CommandResult {
        val isRelease = args.contains("--release") || args.contains("-r")
        val events = mutableListOf<CommandEvent>()
        val cmd = "build"

        events += CommandEvent.Started(cmd)
        events += CommandEvent.Stream("🔨 zeus build — project: '${context.project.name}' target: bip_max (${context.project.targetResolution}) mode: ${if (isRelease) "release" else "debug"}")

        val result = ZeusCompiler.compile(context.project, isRelease)

        for (line in result.logs) {
            val isErr = line.contains("Error") || line.contains("❌")
            events += CommandEvent.Stream(line, isError = isErr)
        }
        for (err in result.errors) {
            events += CommandEvent.Stream(err, isError = true)
        }
        for (warn in result.warnings) {
            events += CommandEvent.Stream(warn, isError = false)
        }

        return if (result.isSuccess && result.zabPackage != null) {
            val pkg = result.zabPackage
            events += CommandEvent.Stream("✅ Build succeeded: ${pkg.packageName} (${pkg.fileSizeKb} KB, CRC32: ${pkg.checksumCrc32})")
            events += CommandEvent.Completed(exitCode = 0, summary = "Build succeeded: ${pkg.packageName}")
            CommandResult(events, 0, "Build succeeded: ${pkg.packageName}")
        } else {
            val summary = result.errors.firstOrNull() ?: "Build failed: toolchain not configured"
            events += CommandEvent.Stream(
                "❌ Build failed. Remediation: configure a real Zepp OS toolchain path in settings and retry.",
                isError = true
            )
            events += CommandEvent.Completed(
                exitCode = 1,
                summary = summary,
                errorCategory = ErrorCategory.BACKEND_NOT_CONFIGURED
            )
            CommandResult(events, 1, summary, ErrorCategory.BACKEND_NOT_CONFIGURED)
        }
    }
}
