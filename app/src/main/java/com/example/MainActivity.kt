package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ZeusIdeTheme
import com.example.zeus.ui.ZeusViewModel
import com.example.zeus.ui.screens.BipMaxWatchSimulator
import com.example.zeus.ui.screens.NewProjectDialog
import com.example.zeus.ui.screens.ZeusBleBridgeScreen
import com.example.zeus.ui.screens.ZeusCodeEditor
import com.example.zeus.ui.screens.ZeusPackageDocsScreen
import com.example.zeus.ui.screens.ZeusTerminalScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: ZeusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZeusIdeTheme {
                ZeusIdeApp(viewModel = viewModel)
            }
        }
    }
}

enum class ZeusIdeTab(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    EDITOR("Editor", Icons.Default.Code, "tab_editor"),
    SIMULATOR("Bip Max", Icons.Default.Watch, "tab_simulator"),
    TERMINAL("Terminal", Icons.Default.Terminal, "tab_terminal"),
    BRIDGE("BLE Bridge", Icons.Default.Bluetooth, "tab_bridge"),
    PACKAGE("Package & Docs", Icons.Default.Widgets, "tab_package")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZeusIdeApp(viewModel: ZeusViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showProjectDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.clickable { showProjectDropdown = true }
                    ) {
                        Text(
                            text = "⚡ Zeus IDE",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${uiState.activeProject.name} (432x514)",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        DropdownMenu(
                            expanded = showProjectDropdown,
                            onDismissRequest = { showProjectDropdown = false },
                            modifier = Modifier.background(Color(0xFF161B22))
                        ) {
                            uiState.projects.forEach { project ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(project.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("${project.template.title} • 432x514", color = Color(0xFF38BDF8), fontSize = 10.sp)
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectProject(project)
                                        showProjectDropdown = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "New", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("+ Create New Project", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    showProjectDropdown = false
                                    viewModel.showNewProjectDialog(true)
                                }
                            )
                        }
                    }
                },
                actions = {
                    // Quick Run Build button in Top Bar
                    IconButton(
                        onClick = { viewModel.compileActiveProject(isRelease = false) },
                        modifier = Modifier.testTag("topbar_run_build_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Build .zab",
                            tint = Color(0xFF10B981)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.showNewProjectDialog(true) },
                        modifier = Modifier.testTag("topbar_new_project_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Project",
                            tint = Color(0xFF00E5FF)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0D1117)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF161B22),
                contentColor = Color(0xFF38BDF8)
            ) {
                ZeusIdeTab.entries.forEachIndexed { index, tab ->
                    val isSelected = uiState.currentViewTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setViewTab(index) },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00E5FF),
                            selectedTextColor = Color(0xFF00E5FF),
                            unselectedIconColor = Color(0xFF8B949E),
                            unselectedTextColor = Color(0xFF8B949E),
                            indicatorColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0D1117)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentViewTab) {
                0 -> ZeusCodeEditor(
                    project = uiState.activeProject,
                    activeFile = uiState.activeFile,
                    onFileSelected = { viewModel.selectFile(it) },
                    onContentChange = { viewModel.updateActiveFileContent(it) },
                    onSaveClick = { viewModel.saveActiveFile() },
                    onRunBuildClick = { viewModel.compileActiveProject(isRelease = false) },
                    onInsertSnippet = { viewModel.insertCodeSnippet(it) }
                )

                1 -> BipMaxWatchSimulator(
                    project = uiState.activeProject,
                    sensorState = uiState.sensorState,
                    onHeartRateChange = { viewModel.updateSensorHeartRate(it) },
                    onStepsChange = { viewModel.updateSensorSteps(it) },
                    onBatteryChange = { viewModel.updateSensorBattery(it) },
                    onWeatherChange = { weather, temp -> viewModel.updateSensorWeather(weather, temp) },
                    onDeployClick = { viewModel.deployToBipMaxOverBle() }
                )

                2 -> ZeusTerminalScreen(
                    logs = uiState.terminalLogs,
                    onExecuteCommand = { viewModel.executeTerminalCommand(it) }
                )

                3 -> ZeusBleBridgeScreen(
                    project = uiState.activeProject,
                    isScanning = uiState.isBleScanning,
                    isConnected = uiState.isBleConnected,
                    connectedDeviceName = uiState.connectedDeviceName,
                    connectedDeviceMac = uiState.connectedDeviceMac,
                    isDeploying = uiState.isDeployingOverBle,
                    deployProgress = uiState.deployProgress,
                    generatedPackage = uiState.generatedPackage,
                    onScanClick = { viewModel.scanBleDevices() },
                    onConnectClick = { name, mac -> viewModel.connectBleDevice(name, mac) },
                    onDisconnectClick = { viewModel.disconnectBleDevice() },
                    onDeployClick = { viewModel.deployToBipMaxOverBle() }
                )

                4 -> ZeusPackageDocsScreen(
                    project = uiState.activeProject,
                    generatedPackage = uiState.generatedPackage,
                    onExportZipClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Exported ${uiState.generatedPackage?.packageName ?: "package.zab"} to Downloads")
                        }
                    },
                    onInsertSnippet = { snippet ->
                        viewModel.insertCodeSnippet(snippet)
                        viewModel.setViewTab(0)
                    }
                )
            }
        }
    }

    if (uiState.showNewProjectDialog) {
        NewProjectDialog(
            onDismiss = { viewModel.showNewProjectDialog(false) },
            onCreateProject = { template, name ->
                viewModel.createNewProject(template, name)
            }
        )
    }
}
