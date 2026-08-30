package com.example.zeus.engine

import com.example.zeus.engine.bundler.ZeusPackageBuilder
import com.example.zeus.engine.parser.ZeppJsParser
import com.example.zeus.engine.parser.ZeppParsedProgram
import com.example.zeus.model.ZabPackage
import com.example.zeus.model.ZeusProject

data class CompilationResult(
    val isSuccess: Boolean,
    val zabPackage: ZabPackage?,
    val logs: List<String>,
    val durationMs: Long,
    val memoryUsageKb: Int,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val parsedPrograms: List<ZeppParsedProgram> = emptyList()
)

object ZeusCompiler {

    fun compile(project: ZeusProject, isRelease: Boolean = false): CompilationResult {
        val startTime = System.currentTimeMillis()
        val logs = mutableListOf<String>()
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val parsedPrograms = mutableListOf<ZeppParsedProgram>()

        logs.add("🚀 [Zeus CLI v2.1.0] Initializing Zepp OS build toolchain...")
        logs.add("📦 Project: ${project.name} (${project.appType})")
        logs.add("🎯 Target: Amazfit Bip Max (Display: ${project.targetResolution} AMOLED, Zepp OS ${project.zeppOsVersion})")
        logs.add("⚙️ Mode: ${if (isRelease) "RELEASE (Optimized & Minified)" else "DEBUG (Fast incremental)"}")

        // 1. Verify and parse app.json manifest
        val appJsonFile = project.files.find { it.name == "app.json" }
        if (appJsonFile == null) {
            errors.add("Error: Missing 'app.json' configuration file in project root.")
            logs.add("❌ [Fatal] app.json not found.")
            return CompilationResult(
                isSuccess = false,
                zabPackage = null,
                logs = logs,
                durationMs = (System.currentTimeMillis() - startTime).coerceAtLeast(1),
                memoryUsageKb = 0,
                errors = errors
            )
        }

        logs.add("🔍 Parsing & validating app.json schema...")
        val (manifestOk, manifestIssues) = ZeppJsParser.validateManifest(appJsonFile.content)
        if (!manifestOk) {
            manifestIssues.forEach { issue ->
                if (issue.startsWith("Warning")) {
                    warnings.add(issue)
                    logs.add("⚠️ $issue")
                } else {
                    errors.add(issue)
                    logs.add("❌ $issue")
                }
            }
        } else {
            logs.add("✓ Manifest valid: target 'bip_max' (${project.targetResolution} AMOLED, 302 PPI).")
        }

        // 2. Lexical & AST Parse of JavaScript modules
        val jsFiles = project.files.filter { it.name.endsWith(".js") }
        logs.add("🔨 Parsing and analyzing ${jsFiles.size} JavaScript modules...")

        var totalWidgetsFound = 0
        var totalTokens = 0

        for (file in jsFiles) {
            val parsed = ZeppJsParser.parse(file.name, file.content)
            parsedPrograms.add(parsed)
            totalWidgetsFound += parsed.widgets.size
            totalTokens += parsed.tokenCount

            logs.add("  • [JS AST] ${file.path} (${parsed.linesOfCode} lines, ${parsed.tokenCount} tokens, ${parsed.widgets.size} widgets declared)")

            // Append parser errors
            parsed.errors.forEach { err ->
                val msg = "[${file.name}:${err.line}:${err.column}] ${err.message}"
                errors.add(msg)
                logs.add("  ❌ $msg")
            }

            // Append parser warnings
            parsed.warnings.forEach { warn ->
                val msg = "[${file.name}:${warn.line}:${warn.column}] ${warn.message}"
                warnings.add(msg)
                logs.add("  ⚠️ $msg")
            }
        }

        if (errors.isNotEmpty()) {
            logs.add("❌ Build FAILED with ${errors.size} error(s).")
            return CompilationResult(
                isSuccess = false,
                zabPackage = null,
                logs = logs,
                durationMs = (System.currentTimeMillis() - startTime).coerceAtLeast(1),
                memoryUsageKb = 0,
                errors = errors,
                warnings = warnings,
                parsedPrograms = parsedPrograms
            )
        }

        // 3. Real .zab Binary ZIP Generation & Archiving
        logs.add("⚡ Executing QuickJS Bytecode Compiler & Binary Packager...")
        val zab = ZeusPackageBuilder.buildZabPackage(project, isRelease)

        val memoryEstimateKb = (zab.fileSizeKb * 2.8 + 48).toInt()
        logs.add("📦 Generated Zepp OS Package (.zab): ${zab.fileSizeKb} KB (${zab.fileCount} archive entries)")
        logs.add("🔐 CRC32 Checksum: ${zab.checksumCrc32} | SHA-256: ${zab.sha256Digest.take(16)}...")
        logs.add("📊 Estimated Bip Max RAM consumption: ~${memoryEstimateKb} KB (Safe limit: 1024 KB)")

        logs.add("✅ Build SUCCESSFUL!")
        logs.add("🎁 Binary Artifact: dist/${zab.packageName}")
        logs.add("✨ Ready for deployment to Bip Max watch via 'zeus bridge --install'")

        val duration = (System.currentTimeMillis() - startTime).coerceAtLeast(1)

        return CompilationResult(
            isSuccess = true,
            zabPackage = zab,
            logs = logs,
            durationMs = duration,
            memoryUsageKb = memoryEstimateKb,
            errors = errors,
            warnings = warnings,
            parsedPrograms = parsedPrograms
        )
    }
}

