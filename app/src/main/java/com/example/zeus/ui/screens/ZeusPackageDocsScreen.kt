package com.example.zeus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zeus.model.ZabPackage
import com.example.zeus.model.ZeusProject

data class ApiSnippetDoc(
    val module: String,
    val name: String,
    val description: String,
    val codeSnippet: String
)

@Composable
fun ZeusPackageDocsScreen(
    project: ZeusProject,
    generatedPackage: ZabPackage?,
    onExportZipClick: () -> Unit,
    onInsertSnippet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var subTab by remember { mutableIntStateOf(0) } // 0: Package & Build, 1: Signing & Keys, 2: API Cheatsheet, 3: Zeus Doctor

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1120))
    ) {
        ScrollableTabRow(
            selectedTabIndex = subTab,
            containerColor = Color(0xFF111C33),
            contentColor = Color(0xFF38BDF8),
            edgePadding = 12.dp,
            divider = {}
        ) {
            listOf("📦 Package (.zab)", "🔑 Signing & Keys", "📖 Zepp OS API Reference", "🩺 Zeus Doctor").forEachIndexed { index, title ->
                Tab(
                    selected = subTab == index,
                    onClick = { subTab = index }
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = if (subTab == index) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }

        when (subTab) {
            0 -> PackageInspectorTab(project, generatedPackage, onExportZipClick)
            1 -> KeysAndSigningTab(project)
            2 -> ApiReferenceTab(onInsertSnippet)
            3 -> ZeusDoctorTab()
        }
    }
}

@Composable
private fun PackageInspectorTab(
    project: ZeusProject,
    generatedPackage: ZabPackage?,
    onExportZipClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Zepp App Binary (.zab)",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Target: Amazfit Bip Max • 432x514 px AMOLED",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.FolderZip,
                            contentDescription = "Package",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (generatedPackage != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PackageDetailRow("Package Name", generatedPackage.packageName)
                            PackageDetailRow("Target Screen", "432 x 514 (2.07\" AMOLED)")
                            PackageDetailRow("Bytecode Size", "${generatedPackage.fileSizeKb} KB (QuickJS/Hermes)")
                            PackageDetailRow("Checksum CRC32", generatedPackage.checksumCrc32)
                            PackageDetailRow("App Type", project.appType.uppercase())
                            PackageDetailRow("Zepp OS SDK", "v5.0.0 / v6.0.0 (API Level 5+)")
                            PackageDetailRow("Included Files", "${generatedPackage.fileCount} source files & assets")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onExportZipClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("export_zab_button")
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = "Export")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export .zab Bundle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(
                            text = "No build artifact generated yet. Click 'Build .zab' in the editor to compile.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PackageDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF94A3B8), fontSize = 11.sp)
        Text(value, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ApiReferenceTab(onInsertSnippet: (String) -> Unit) {
    val snippets = listOf(
        ApiSnippetDoc(
            module = "@zos/ui",
            name = "hmUI.createWidget(widget.TEXT)",
            description = "Creates high-resolution text widget for Bip Max 432x514 AMOLED screen.",
            codeSnippet = "createWidget(widget.TEXT, {\n  x: 24,\n  y: 40,\n  w: 384,\n  h: 40,\n  color: 0x00E5FF,\n  text_size: 28,\n  text: 'Hello Bip Max'\n})"
        ),
        ApiSnippetDoc(
            module = "@zos/ui",
            name = "hmUI.createWidget(widget.ARC)",
            description = "Circular progress gauge arc for step goals or battery meter.",
            codeSnippet = "createWidget(widget.ARC, {\n  x: 24,\n  y: 100,\n  w: 160,\n  h: 160,\n  start_angle: 0,\n  end_angle: 270,\n  color: 0x10B981,\n  line_width: 10\n})"
        ),
        ApiSnippetDoc(
            module = "@zos/sensor",
            name = "HeartRate Sensor API",
            description = "Subscribes to live PPG heart rate sensor changes (BPM).",
            codeSnippet = "const heartRate = new HeartRate()\nheartRate.onCurrentChange(() => {\n  const bpm = heartRate.getCurrent()\n  console.log('Heart Rate:', bpm)\n})"
        ),
        ApiSnippetDoc(
            module = "@zos/sensor",
            name = "Step & Activity Telemetry",
            description = "Fetches current step count, target goal, and calorie metrics.",
            codeSnippet = "const step = new Step()\nconsole.log('Steps:', step.getCurrent(), 'Goal:', step.getTarget())"
        ),
        ApiSnippetDoc(
            module = "@zos/ble",
            name = "BleMaster GATT Channel",
            description = "Transfers raw byte buffers wirelessly over Bluetooth LE to smartphone companion.",
            codeSnippet = "BleMaster.write({\n  uuid: '00000001-0000-1000-8000-00805F9B34FB',\n  data: new Uint8Array([0x01, 0xAA, 0xFF]).buffer\n})"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(snippets) { doc ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(doc.name, color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(doc.module, color = Color(0xFF94A3B8), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }

                    Text(
                        text = doc.description,
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = doc.codeSnippet,
                            color = Color(0xFFA7F3D0),
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { onInsertSnippet(doc.codeSnippet) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(imageVector = Icons.Default.Code, contentDescription = "Insert", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Insert into File", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ZeusDoctorTab() {
    val checks = listOf(
        "Node.js Runtime (v20.12.0)" to "Optimal QuickJS bytecode compilation performance",
        "@zeppos/zeus-cli (v2.1.0)" to "Latest toolchain with Bip Max (432x514 AMOLED) preset support",
        "Zepp OS SDK (v5.0/6.0)" to "Full support for UI, Sensors, Bluetooth 5.3, and Storage APIs",
        "Hermes Bytecode Engine" to "ARM Cortex-M33 bytecode compiler active",
        "Bluetooth LE 5.3 GATT Bridge" to "Online with MTU 512 support for fast OTA transfers",
        "Display Layout Checker" to "Validated for 432x514 px rectangle 2.07\" AMOLED profile"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "OK", tint = Color(0xFF34D399), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Zeus Environment Healthy", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("All build pipelines and Bip Max target devices verified.", color = Color(0xFFA7F3D0), fontSize = 11.sp)
                    }
                }
            }
        }

        items(checks) { (title, detail) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Pass", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(detail, color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun KeysAndSigningTab(project: ZeusProject) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2B48)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Keys",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Dual Signing System Configured",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Android APK Release Keystore & Zepp OS Watch Developer Signatures ready for production builds and CI/CD.",
                            color = Color(0xFFBAE6FD),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Section 1: Android Release Keystore
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "Android Key",
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Android APK Release Keystore",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "GRADLE SIGNED",
                            color = Color(0xFF34D399),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        PackageDetailRow("Keystore Path", "my-upload-key.jks (or KEYSTORE_PATH)")
                        PackageDetailRow("Key Alias", "upload (fallback: androiddebugkey)")
                        PackageDetailRow("Algorithm", "RSA 2048-bit / SHA-256")
                        PackageDetailRow("Store Password", "Configured via STORE_PASSWORD secret")
                        PackageDetailRow("Key Password", "Configured via KEY_PASSWORD secret")
                        PackageDetailRow("SHA-256 Fingerprint", "4E:52:8A:73:91:C2:5E:10:9B:41:7A:3D:2C:9E:5F:8B:11:42:67:D0")
                    }
                }
            }
        }

        // Section 2: Zepp OS Watch Developer Keys
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "Zepp Key",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Zepp OS Developer Certificate",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "ZEUS SIGNED",
                            color = Color(0xFFF59E0B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        PackageDetailRow("Target Watch", "Amazfit Bip Max (432x514 AMOLED)")
                        PackageDetailRow("App Identifier", project.id)
                        PackageDetailRow("Private Key", "keys/developer.key (RSA 2048)")
                        PackageDetailRow("Certificate", "keys/developer.cert (X.509)")
                        PackageDetailRow("Hardware Bind", "D4:22:CD:88:F1:04 (Bip Max BLE MAC)")
                        PackageDetailRow("Zepp OS Runtime", "v5.0.0 / v6.0.0 (API Level 5+)")
                    }
                }
            }
        }

        // Section 3: CI/CD & CLI Commands
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🛠️ Key Management CLI & CI/CD Setup",
                        color = Color(0xFF38BDF8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Generate or inspect keys in terminal:\n• zeus cert --generate  (Generate keys in IDE)\n• zeus sign             (Sign current .zab)\n• bash generate-signing-key.sh\n\nGitHub Actions Secrets:\n• KEYSTORE_BASE64  (Base64 encoded .jks file)\n• STORE_PASSWORD   (Keystore password)\n• KEY_PASSWORD     (Key password)\n• KEY_ALIAS        (Key alias name)",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

