package com.example.zeus.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zeus.data.ZeusTemplatesRepository
import com.example.zeus.engine.CliCommandResult
import com.example.zeus.engine.CompilationResult
import com.example.zeus.engine.ZeusCliRunner
import com.example.zeus.engine.ZeusCompiler
import com.example.zeus.model.FileType
import com.example.zeus.model.SensorSimulationState
import com.example.zeus.model.ZabPackage
import com.example.zeus.model.ZeusFile
import com.example.zeus.model.ZeusLogEntry
import com.example.zeus.model.ZeusProject
import com.example.zeus.model.ZeusTemplate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ZeusUiState(
    val projects: List<ZeusProject> = emptyList(),
    val activeProject: ZeusProject,
    val activeFile: ZeusFile?,
    val terminalLogs: List<ZeusLogEntry> = emptyList(),
    val isBuilding: Boolean = false,
    val lastBuildResult: CompilationResult? = null,
    val generatedPackage: ZabPackage? = null,
    val sensorState: SensorSimulationState = SensorSimulationState(),
    val isDevServerRunning: Boolean = false,
    val isBleScanning: Boolean = false,
    val isBleConnected: Boolean = false,
    val connectedDeviceName: String? = null,
    val connectedDeviceMac: String? = null,
    val isDeployingOverBle: Boolean = false,
    val deployProgress: Float = 0f,
    val currentViewTab: Int = 0, // 0: Editor & Files, 1: Bip Max Simulator, 2: Terminal, 3: BLE Bridge, 4: Package & Docs
    val showNewProjectDialog: Boolean = false,
    val showApiReferenceDialog: Boolean = false,
    val showExportPackageDialog: Boolean = false,
    val statusMessage: String? = null
)

class ZeusViewModel : ViewModel() {

    private val initialProject = ZeusTemplatesRepository.createProject(ZeusTemplate.BIP_MAX_DIGITAL_PRO)

    private val _uiState = MutableStateFlow(
        ZeusUiState(
            projects = listOf(initialProject),
            activeProject = initialProject,
            activeFile = initialProject.files.firstOrNull { it.name == "index.js" } ?: initialProject.files.firstOrNull(),
            terminalLogs = listOf(
                ZeusLogEntry(
                    level = ZeusLogEntry.LogLevel.ZEUS,
                    tag = "zeus",
                    message = "⚡ Zeus CLI IDE v2.1.0 initialized for Amazfit Bip Max (432x514 AMOLED)."
                ),
                ZeusLogEntry(
                    level = ZeusLogEntry.LogLevel.INFO,
                    tag = "zeus",
                    message = "📦 Active project: '${initialProject.name}'. Target: Amazfit Bip Max (Zepp OS 5.0/6.0)."
                ),
                ZeusLogEntry(
                    level = ZeusLogEntry.LogLevel.INFO,
                    tag = "zeus",
                    message = "💡 Type 'zeus help' or click action chips below to execute commands."
                )
            )
        )
    )
    val uiState: StateFlow<ZeusUiState> = _uiState.asStateFlow()

    init {
        // Run an initial build to verify project and generate package
        viewModelScope.launch {
            compileActiveProject(isSilent = true)
        }
    }

    fun selectProject(project: ZeusProject) {
        _uiState.update { state ->
            state.copy(
                activeProject = project,
                activeFile = project.files.firstOrNull { it.name == "index.js" } ?: project.files.firstOrNull()
            )
        }
        addTerminalLog("Switched active project to '${project.name}'", ZeusLogEntry.LogLevel.INFO)
    }

    fun createNewProject(template: ZeusTemplate, name: String) {
        val newProj = ZeusTemplatesRepository.createProject(template, name)
        _uiState.update { state ->
            state.copy(
                projects = state.projects + newProj,
                activeProject = newProj,
                activeFile = newProj.files.firstOrNull { it.name == "index.js" } ?: newProj.files.firstOrNull(),
                showNewProjectDialog = false
            )
        }
        addTerminalLog("Created new Zepp OS project '${newProj.name}' (${template.title})", ZeusLogEntry.LogLevel.SUCCESS)
        compileActiveProject(isSilent = true)
    }

    fun selectFile(file: ZeusFile) {
        _uiState.update { state ->
            val updatedActiveProject = state.activeProject.copy(activeFileId = file.id)
            state.copy(
                activeProject = updatedActiveProject,
                activeFile = file
            )
        }
    }

    fun updateActiveFileContent(newContent: String) {
        val currentFile = _uiState.value.activeFile ?: return
        val updatedFile = currentFile.copy(content = newContent, isModified = true)

        _uiState.update { state ->
            val updatedFiles = state.activeProject.files.map {
                if (it.id == updatedFile.id) updatedFile else it
            }
            val updatedProject = state.activeProject.copy(files = updatedFiles)
            state.copy(
                activeProject = updatedProject,
                activeFile = updatedFile
            )
        }
    }

    fun saveActiveFile() {
        val currentFile = _uiState.value.activeFile ?: return
        val savedFile = currentFile.copy(isModified = false)

        _uiState.update { state ->
            val updatedFiles = state.activeProject.files.map {
                if (it.id == savedFile.id) savedFile else it
            }
            val updatedProject = state.activeProject.copy(files = updatedFiles)
            state.copy(
                activeProject = updatedProject,
                activeFile = savedFile,
                statusMessage = "Saved ${savedFile.name}"
            )
        }
    }

    fun addNewFile(fileName: String, fileType: FileType, initialContent: String = "") {
        val newFile = ZeusFile(
            name = fileName,
            path = fileName,
            content = initialContent,
            fileType = fileType
        )
        _uiState.update { state ->
            val updatedFiles = state.activeProject.files + newFile
            val updatedProject = state.activeProject.copy(files = updatedFiles)
            state.copy(
                activeProject = updatedProject,
                activeFile = newFile
            )
        }
        addTerminalLog("Created file '$fileName'", ZeusLogEntry.LogLevel.INFO)
    }

    fun compileActiveProject(isRelease: Boolean = false, isSilent: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBuilding = true) }
            if (!isSilent) {
                addTerminalLog("🔨 Building project '${_uiState.value.activeProject.name}' for Bip Max (432x514)...", ZeusLogEntry.LogLevel.ZEUS)
            }

            delay(300) // Simulated compile cycle
            val result = ZeusCompiler.compile(_uiState.value.activeProject, isRelease)

            if (!isSilent) {
                for (l in result.logs) {
                    when {
                        l.contains("❌") || l.contains("Error") -> addTerminalLog(l, ZeusLogEntry.LogLevel.ERROR)
                        l.contains("⚠️") || l.contains("Warning") -> addTerminalLog(l, ZeusLogEntry.LogLevel.WARNING)
                        l.contains("✅") || l.contains("✓") -> addTerminalLog(l, ZeusLogEntry.LogLevel.SUCCESS)
                        else -> addTerminalLog(l, ZeusLogEntry.LogLevel.INFO)
                    }
                }
            }

            _uiState.update { state ->
                state.copy(
                    isBuilding = false,
                    lastBuildResult = result,
                    generatedPackage = result.zabPackage ?: state.generatedPackage,
                    statusMessage = if (result.isSuccess) "Build succeeded! (.zab ready)" else "Build failed"
                )
            }
        }
    }

    fun executeTerminalCommand(cmd: String) {
        val trimmed = cmd.trim()
        if (trimmed.isEmpty()) return

        addTerminalLog("zeus > $trimmed", ZeusLogEntry.LogLevel.ZEUS)

        val result = ZeusCliRunner.execute(
            commandLine = trimmed,
            currentProject = _uiState.value.activeProject,
            isDevRunning = _uiState.value.isDevServerRunning,
            isBleConnected = _uiState.value.isBleConnected
        )

        when (result) {
            is CliCommandResult.Output -> {
                _uiState.update { state ->
                    state.copy(
                        terminalLogs = state.terminalLogs + result.logs,
                        activeProject = result.updatedProject ?: state.activeProject
                    )
                }
            }
            is CliCommandResult.NewProjectCreated -> {
                _uiState.update { state ->
                    state.copy(
                        projects = state.projects + result.newProject,
                        activeProject = result.newProject,
                        activeFile = result.newProject.files.firstOrNull { it.name == "index.js" } ?: result.newProject.files.firstOrNull(),
                        terminalLogs = state.terminalLogs + result.logs
                    )
                }
            }
            is CliCommandResult.ClearTerminal -> {
                _uiState.update { state ->
                    state.copy(terminalLogs = result.welcomeLogs)
                }
            }
        }
    }

    fun toggleDevServer() {
        val newStatus = !_uiState.value.isDevServerRunning
        _uiState.update { it.copy(isDevServerRunning = newStatus) }
        if (newStatus) {
            addTerminalLog("⚡ Zeus Live Dev Server listening on port 8080. Hot reload active.", ZeusLogEntry.LogLevel.SUCCESS)
        } else {
            addTerminalLog("Dev server stopped.", ZeusLogEntry.LogLevel.INFO)
        }
    }

    // BLE Wireless Bridge
    fun scanBleDevices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBleScanning = true) }
            addTerminalLog("🔍 Scanning for nearby Amazfit Bip Max watches...", ZeusLogEntry.LogLevel.BLE)
            delay(1200)
            _uiState.update {
                it.copy(
                    isBleScanning = false,
                    statusMessage = "Found 2 Amazfit devices"
                )
            }
            addTerminalLog("  [FOUND] Amazfit Bip Max (ID: A2286, RSSI: -52 dBm)", ZeusLogEntry.LogLevel.BLE)
        }
    }

    fun connectBleDevice(name: String = "Amazfit Bip Max", mac: String = "D4:22:CD:88:F1:04") {
        viewModelScope.launch {
            addTerminalLog("🔗 Connecting to $name ($mac)...", ZeusLogEntry.LogLevel.BLE)
            delay(800)
            _uiState.update {
                it.copy(
                    isBleConnected = true,
                    connectedDeviceName = name,
                    connectedDeviceMac = mac,
                    statusMessage = "Connected to $name"
                )
            }
            addTerminalLog("✅ GATT connection established. Zepp OS Debugger bridge online.", ZeusLogEntry.LogLevel.SUCCESS)
        }
    }

    fun disconnectBleDevice() {
        _uiState.update {
            it.copy(
                isBleConnected = false,
                connectedDeviceName = null,
                connectedDeviceMac = null,
                statusMessage = "Disconnected"
            )
        }
        addTerminalLog("BLE link disconnected.", ZeusLogEntry.LogLevel.INFO)
    }

    fun deployToBipMaxOverBle() {
        viewModelScope.launch {
            if (!_uiState.value.isBleConnected) {
                connectBleDevice()
                delay(600)
            }

            _uiState.update { it.copy(isDeployingOverBle = true, deployProgress = 0f) }
            addTerminalLog("📲 Transmitting package to Amazfit Bip Max via BLE OTA...", ZeusLogEntry.LogLevel.BLE)

            for (p in 1..10) {
                delay(150)
                _uiState.update { it.copy(deployProgress = p / 10f) }
            }

            _uiState.update {
                it.copy(
                    isDeployingOverBle = false,
                    deployProgress = 1f,
                    statusMessage = "Package installed on Bip Max!"
                )
            }
            addTerminalLog("✅ Package deployed & running on Bip Max (432x514 screen)!", ZeusLogEntry.LogLevel.SUCCESS)
        }
    }

    // Sensor Simulation adjustments
    fun updateSensorHeartRate(bpm: Int) {
        _uiState.update { it.copy(sensorState = it.sensorState.copy(heartRateBpm = bpm)) }
    }

    fun updateSensorSteps(steps: Int) {
        _uiState.update { it.copy(sensorState = it.sensorState.copy(steps = steps)) }
    }

    fun updateSensorBattery(percent: Int) {
        _uiState.update { it.copy(sensorState = it.sensorState.copy(batteryPercent = percent)) }
    }

    fun updateSensorWeather(weather: String, temp: Int) {
        _uiState.update {
            it.copy(sensorState = it.sensorState.copy(weatherCondition = weather, temperatureCelsius = temp))
        }
    }

    fun insertCodeSnippet(snippet: String) {
        val currentContent = _uiState.value.activeFile?.content ?: ""
        val updated = "$currentContent\n\n$snippet"
        updateActiveFileContent(updated)
        _uiState.update { it.copy(statusMessage = "Inserted API snippet") }
    }

    fun setViewTab(tabIndex: Int) {
        _uiState.update { it.copy(currentViewTab = tabIndex) }
    }

    fun showNewProjectDialog(show: Boolean) {
        _uiState.update { it.copy(showNewProjectDialog = show) }
    }

    fun showApiReferenceDialog(show: Boolean) {
        _uiState.update { it.copy(showApiReferenceDialog = show) }
    }

    fun showExportPackageDialog(show: Boolean) {
        _uiState.update { it.copy(showExportPackageDialog = show) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    private fun addTerminalLog(msg: String, level: ZeusLogEntry.LogLevel = ZeusLogEntry.LogLevel.INFO, tag: String = "zeus") {
        _uiState.update {
            it.copy(
                terminalLogs = it.terminalLogs + ZeusLogEntry(level = level, tag = tag, message = msg)
            )
        }
    }
}
