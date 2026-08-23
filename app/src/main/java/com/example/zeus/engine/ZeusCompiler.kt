package com.example.zeus.engine

import com.example.zeus.model.ZabPackage
import com.example.zeus.model.ZeusProject

data class CompilationResult(
    val isSuccess: Boolean,
    val zabPackage: ZabPackage?,
    val logs: List<String>,
    val durationMs: Long,
    val memoryUsageKb: Int,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

object ZeusCompiler {

    /**
     * Lightweight check for toolchain availability.
     * Returns true only when a real build backend is configured and can produce artifacts.
     * Currently always false — no real Zepp OS toolchain is bundled with the IDE.
     */
    fun isToolchainConfigured(): Boolean = false

    fun compile(project: ZeusProject, isRelease: Boolean = false): CompilationResult {
        val startTime = System.currentTimeMillis()
        val logs = mutableListOf<String>()
        val errors = mutableListOf<String>()

        logs.add("Zeus Compiler: build requested for '${project.name}' (mode: ${if (isRelease) "release" else "debug"})")
        logs.add("No real Zepp OS build toolchain is configured on this device.")
        logs.add("A real build backend (zeus-cli binary or remote build service) must be set up before builds can succeed.")
        logs.add("Configure a toolchain path in settings and retry.")

        errors.add("Build toolchain not configured. Cannot produce a real artifact.")

        return CompilationResult(
            isSuccess = false,
            zabPackage = null,
            logs = logs,
            durationMs = System.currentTimeMillis() - startTime,
            memoryUsageKb = 0,
            errors = errors
        )
    }
}
