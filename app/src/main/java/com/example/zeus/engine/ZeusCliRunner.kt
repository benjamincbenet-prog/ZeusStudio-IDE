package com.example.zeus.engine

import com.example.zeus.data.ZeusTemplatesRepository
import com.example.zeus.model.ZeusLogEntry
import com.example.zeus.model.ZeusProject
import com.example.zeus.model.ZeusTemplate

sealed class CliCommandResult {
    data class Output(val logs: List<ZeusLogEntry>, val updatedProject: ZeusProject? = null) : CliCommandResult()
    data class NewProjectCreated(val newProject: ZeusProject, val logs: List<ZeusLogEntry>) : CliCommandResult()
    data class ClearTerminal(val welcomeLogs: List<ZeusLogEntry>) : CliCommandResult()
}

object ZeusCliRunner {

    fun execute(
        commandLine: String,
        currentProject: ZeusProject,
        isDevRunning: Boolean,
        isBleConnected: Boolean
    ): CliCommandResult {
        val trimmed = commandLine.trim()
        if (trimmed.isEmpty()) {
            return CliCommandResult.Output(emptyList())
        }

        val parts = trimmed.split("\\s+".toRegex())
        val first = parts.getOrNull(0)?.lowercase() ?: ""

        // Handle both "zeus <cmd>" and "<cmd>"
        val (cmd, args) = if (first == "zeus") {
            val sub = parts.getOrNull(1)?.lowercase() ?: ""
            sub to parts.drop(2)
        } else {
            first to parts.drop(1)
        }

        val logs = mutableListOf<ZeusLogEntry>()

        fun log(msg: String, level: ZeusLogEntry.LogLevel = ZeusLogEntry.LogLevel.INFO, tag: String = "zeus") {
            logs.add(ZeusLogEntry(level = level, tag = tag, message = msg))
        }

        when (cmd) {
            "help", "--help", "-h", "" -> {
                log("⚡ Zeus CLI v2.1.0 - Zepp OS & Amazfit Bip Max Toolchain", ZeusLogEntry.LogLevel.ZEUS)
                log("Usage: zeus <command> [options]\n")
                log("Commands:")
                log("  create <name>        Create a new Zepp OS project for Bip Max")
                log("  build [--release]    Compile and package .zab for 432x514 display")
                log("  dev [--watch]        Start live preview server with hot reload")
                log("  preview              Launch interactive Bip Max watch simulator")
                log("  bridge [--install]   Manage BLE 5.3 wireless bridge to Bip Max watch")
                log("  doctor               Inspect development environment & SDK status")
                log("  lint                 Run static code analysis on JS / app.json")
                log("  status               Display active project & target configuration")
                log("  clear                Clear terminal output buffer")
                log("  version              Display Zeus CLI and Zepp OS toolchain version")
                return CliCommandResult.Output(logs)
            }

            "version", "--version", "-v" -> {
                log("@zeppos/zeus-cli v2.1.0", ZeusLogEntry.LogLevel.ZEUS)
                log("Node.js runtime: v20.12.0 • QuickJS Bytecode Compiler v2.4")
                log("Target profile: Amazfit Bip Max (Zepp OS v5.0/6.0 / 432x514 px AMOLED)")
                return CliCommandResult.Output(logs)
            }

            "create" -> {
                val projectName = args.firstOrNull()?.takeIf { !it.startsWith("-") } ?: "bip-max-app-${System.currentTimeMillis() % 1000}"
                val templateArg = args.find { it.startsWith("--template=") }?.substringAfter("=")
                    ?: if (args.contains("--template") && args.indexOf("--template") + 1 < args.size) {
                        args[args.indexOf("--template") + 1]
                    } else "digital"

                val template = when {
                    templateArg.contains("fitness") || templateArg.contains("hiit") -> ZeusTemplate.BIP_MAX_FITNESS_TRACKER
                    templateArg.contains("weather") -> ZeusTemplate.BIP_MAX_WEATHER_WIDGET
                    templateArg.contains("ble") || templateArg.contains("remote") -> ZeusTemplate.BIP_MAX_BLE_CONTROLLER
                    templateArg.contains("analog") -> ZeusTemplate.BIP_MAX_MINIMAL_ANALOG
                    else -> ZeusTemplate.BIP_MAX_DIGITAL_PRO
                }

                log("🚀 Initializing new Zepp OS project: '$projectName'...", ZeusLogEntry.LogLevel.ZEUS)
                log("📋 Selected Template: ${template.title} (${template.subtitle})")
                log("📐 Screen Target: Amazfit Bip Max (432x514 px AMOLED)")

                val newProj = ZeusTemplatesRepository.createProject(template, projectName)
                for (f in newProj.files) {
                    log("  + Created ${f.path}")
                }
                log("✅ Project '$projectName' created successfully! Run 'zeus dev' or 'zeus build'.", ZeusLogEntry.LogLevel.SUCCESS)
                return CliCommandResult.NewProjectCreated(newProj, logs)
            }

            "build" -> {
                val isRelease = args.contains("--release") || args.contains("-r")
                log("🔨 Running 'zeus build' for target: bip_max (${currentProject.targetResolution})...", ZeusLogEntry.LogLevel.ZEUS)

                val result = ZeusCompiler.compile(currentProject, isRelease)
                for (l in result.logs) {
                    when {
                        l.contains("❌") || l.contains("Error") -> log(l, ZeusLogEntry.LogLevel.ERROR)
                        l.contains("⚠️") || l.contains("Warning") -> log(l, ZeusLogEntry.LogLevel.WARNING)
                        l.contains("✅") || l.contains("✓") -> log(l, ZeusLogEntry.LogLevel.SUCCESS)
                        else -> log(l, ZeusLogEntry.LogLevel.INFO)
                    }
                }

                val updated = currentProject.copy(
                    lastBuiltTimestamp = System.currentTimeMillis(),
                    lastBuildSuccess = result.isSuccess
                )
                return CliCommandResult.Output(logs, updated)
            }

            "dev" -> {
                log("⚡ Starting Zeus Dev Server for Bip Max Simulator...", ZeusLogEntry.LogLevel.ZEUS)
                log("📡 Live preview link: http://localhost:8080 (Hot-Reload Active)")
                log("👁️ Watching project files for changes...")
                log("✓ Canvas synced with active Zepp OS JavaScript bundle.", ZeusLogEntry.LogLevel.SUCCESS)
                val updated = currentProject.copy(isDevServerRunning = true)
                return CliCommandResult.Output(logs, updated)
            }

            "preview" -> {
                log("🖥️ Launching interactive Amazfit Bip Max Simulator (432x514 2.07\" HD AMOLED 3,000 Nits)...", ZeusLogEntry.LogLevel.ZEUS)
                log("✓ Touch events, Digital Clock, Heart Rate, and Sensor emulators active.", ZeusLogEntry.LogLevel.SUCCESS)
                return CliCommandResult.Output(logs)
            }

            "bridge" -> {
                if (args.contains("--scan") || args.contains("-s")) {
                    log("🔍 Scanning for nearby Amazfit Bip Max smartwatches via BLE 5.3...", ZeusLogEntry.LogLevel.BLE)
                    log("  [FOUND] Amazfit Bip Max (ID: A2286, RSSI: -54 dBm, MAC: D4:22:CD:88:F1:04)", ZeusLogEntry.LogLevel.BLE)
                    log("  [FOUND] Amazfit Bip 5 (ID: A2215, RSSI: -72 dBm, MAC: 8C:DE:52:11:AB:29)", ZeusLogEntry.LogLevel.BLE)
                    log("✓ Scan complete. Use 'zeus bridge --connect' to pair.", ZeusLogEntry.LogLevel.SUCCESS)
                } else if (args.contains("--install") || args.contains("-i")) {
                    log("📲 Initiating BLE OTA Package Transfer to Bip Max...", ZeusLogEntry.LogLevel.BLE)
                    val result = ZeusCompiler.compile(currentProject)
                    if (result.isSuccess && result.zabPackage != null) {
                        log("📦 Transmitting ${result.zabPackage.packageName} (${result.zabPackage.fileSizeKb} KB)...", ZeusLogEntry.LogLevel.BLE)
                        log("  [1/3] Establishing GATT Data Channel...")
                        log("  [2/3] Streaming packet chunks (MTU: 512 bytes)... [100%]")
                        log("  [3/3] Verifying CRC32 checksum ${result.zabPackage.checksumCrc32}...")
                        log("✅ App installed and launched on Bip Max smartwatch!", ZeusLogEntry.LogLevel.SUCCESS)
                    } else {
                        log("❌ Package build failed. Fix compilation errors before deploying.", ZeusLogEntry.LogLevel.ERROR)
                    }
                } else if (args.contains("--logs") || args.contains("-l")) {
                    log("📋 Streaming live logcat from Amazfit Bip Max:", ZeusLogEntry.LogLevel.BLE)
                    log("[BipMax/OS] ZeppOS kernel v5.2.0 booted (Free Heap: 1240 KB)")
                    log("[BipMax/App] Initializing ${currentProject.name}...")
                    log("[BipMax/Sensor] BioTracker 5.0 HeartRate sensor registered @ 1Hz")
                    log("[BipMax/UI] Rendered 6 widgets on 432x514 viewport (Frame time: 8.4ms)")
                } else {
                    log("🔗 Zeus BLE Bridge Status: ${if (isBleConnected) "CONNECTED to Amazfit Bip Max (D4:22:CD:88:F1:04)" else "DISCONNECTED"}", ZeusLogEntry.LogLevel.BLE)
                    log("Options: --scan, --connect, --install, --logs")
                }
                return CliCommandResult.Output(logs)
            }

            "doctor" -> {
                log("🩺 Running Zeus Doctor diagnostic checks...", ZeusLogEntry.LogLevel.ZEUS)
                log("✓ Node.js: v20.12.0 (OK)", ZeusLogEntry.LogLevel.SUCCESS)
                log("✓ @zeppos/zeus-cli: v2.1.0 (Latest)", ZeusLogEntry.LogLevel.SUCCESS)
                log("✓ Zepp OS SDK: v5.0/6.0 (Target: Bip Max 432x514 AMOLED)", ZeusLogEntry.LogLevel.SUCCESS)
                log("✓ Bluetooth LE 5.3 Controller: Available & Active", ZeusLogEntry.LogLevel.SUCCESS)
                log("✓ QuickJS Bytecode Compiler: Ready", ZeusLogEntry.LogLevel.SUCCESS)
                log("✓ Android Emulator / Device Bridge: Online", ZeusLogEntry.LogLevel.SUCCESS)
                log("🎉 Everything is set up properly for Zepp OS development!", ZeusLogEntry.LogLevel.SUCCESS)
                return CliCommandResult.Output(logs)
            }

            "lint" -> {
                log("🔎 Running static code analysis on ${currentProject.files.size} project files...", ZeusLogEntry.LogLevel.ZEUS)
                val jsFiles = currentProject.files.filter { it.name.endsWith(".js") }
                var issues = 0
                for (f in jsFiles) {
                    if (!f.content.contains("import")) {
                        log("⚠️ ${f.path}: No ES module imports found. Consider using modern '@zos/*' modular imports.", ZeusLogEntry.LogLevel.WARNING)
                        issues++
                    }
                    if (f.content.contains("console.log")) {
                        log("ℹ️ ${f.path}: console.log found. Prefer '@zos/utils' log.getLogger for production builds.")
                    }
                }
                if (issues == 0) {
                    log("✅ Clean! 0 errors, 0 warnings in ${currentProject.name}.", ZeusLogEntry.LogLevel.SUCCESS)
                } else {
                    log("⚠️ Lint finished with $issues suggestion(s).", ZeusLogEntry.LogLevel.WARNING)
                }
                return CliCommandResult.Output(logs)
            }

            "status" -> {
                log("📊 Active Project: ${currentProject.name}", ZeusLogEntry.LogLevel.ZEUS)
                log("  • Type: ${currentProject.appType}")
                log("  • Target Device: Amazfit Bip Max")
                log("  • Screen Resolution: ${currentProject.targetResolution} (2.07\" AMOLED)")
                log("  • Zepp OS Version: ${currentProject.zeppOsVersion}")
                log("  • Source Files: ${currentProject.files.size}")
                log("  • Dev Server: ${if (isDevRunning) "RUNNING (Port 8080)" else "STOPPED"}")
                log("  • BLE Bridge: ${if (isBleConnected) "CONNECTED" else "DISCONNECTED"}")
                return CliCommandResult.Output(logs)
            }

            "clear", "cls" -> {
                return CliCommandResult.ClearTerminal(
                    listOf(
                        ZeusLogEntry(
                            level = ZeusLogEntry.LogLevel.ZEUS,
                            tag = "zeus",
                            message = "⚡ Zeus CLI v2.1.0 (Zepp OS / Amazfit Bip Max 432x514) - Type 'zeus help' for command list."
                        )
                    )
                )
            }

            else -> {
                log("❌ Unknown command: '$cmd'. Type 'zeus help' for available commands.", ZeusLogEntry.LogLevel.ERROR)
                return CliCommandResult.Output(logs)
            }
        }
    }
}
