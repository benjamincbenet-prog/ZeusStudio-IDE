package com.example.zeus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zeus.model.ZeusFile
import com.example.zeus.model.ZeusProject

@Composable
fun ZeusCodeEditor(
    project: ZeusProject,
    activeFile: ZeusFile?,
    onFileSelected: (ZeusFile) -> Unit,
    onContentChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onRunBuildClick: () -> Unit,
    onInsertSnippet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        // File Tabs Bar
        ScrollableTabRow(
            selectedTabIndex = project.files.indexOfFirst { it.id == activeFile?.id }.coerceAtLeast(0),
            containerColor = Color(0xFF161B22),
            contentColor = Color(0xFF38BDF8),
            edgePadding = 8.dp,
            divider = {}
        ) {
            project.files.forEach { file ->
                val isSelected = file.id == activeFile?.id
                Tab(
                    selected = isSelected,
                    onClick = { onFileSelected(file) },
                    modifier = Modifier.testTag("tab_${file.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (file.name.endsWith(".json")) "⚙️" else if (file.name.endsWith(".js")) "⚡" else "📄",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = file.name + if (file.isModified) " •" else "",
                            color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF8B949E),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Action Toolbar & Snippet Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F242C))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Snippets Quick-Inserter
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Snippets:",
                    color = Color(0xFF8B949E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(end = 2.dp)
                )

                SnippetChip("+ TEXT") {
                    onInsertSnippet("createWidget(widget.TEXT, {\n  x: 20, y: 100, w: 280, h: 30,\n  color: 0x00E5FF, text_size: 20,\n  text: 'Zepp OS Bip Max'\n})")
                }
                SnippetChip("+ ARC") {
                    onInsertSnippet("createWidget(widget.ARC, {\n  x: 50, y: 150, w: 100, h: 100,\n  start_angle: 0, end_angle: 270,\n  color: 0x10B981, line_width: 8\n})")
                }
                SnippetChip("+ BUTTON") {
                    onInsertSnippet("createWidget(widget.BUTTON, {\n  x: 30, y: 260, w: 260, h: 60,\n  radius: 16, normal_color: 0x0284C7,\n  text: 'TAP ACTION', text_size: 18,\n  click_func: () => { /* action */ }\n})")
                }
                SnippetChip("+ HEART SENSOR") {
                    onInsertSnippet("const hr = new HeartRate()\nhr.onCurrentChange(() => {\n  console.log('Current BPM:', hr.getCurrent())\n})")
                }
                SnippetChip("+ BLE SEND") {
                    onInsertSnippet("BleMaster.write({\n  uuid: '00000001-0000-1000-8000-00805F9B34FB',\n  data: new Uint8Array([0x01, 0x02, 0x03]).buffer\n})")
                }
            }

            // Save & Build Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onSaveClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21262D)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("save_file_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF58A6FF)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", fontSize = 11.sp, color = Color.White)
                }

                Button(
                    onClick = onRunBuildClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF238636)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("run_build_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Build",
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Build .zab", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Code Editor Canvas (Line Numbers + Text Area)
        val content = activeFile?.content ?: "// Select a file to view or edit"
        val lines = content.lines()
        val lineCount = lines.size.coerceAtLeast(1)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .background(Color(0xFF0D1117))
        ) {
            // Line numbers gutter
            Column(
                modifier = Modifier
                    .background(Color(0xFF161B22))
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                for (i in 1..lineCount) {
                    Text(
                        text = "$i",
                        color = Color(0xFF484F58),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(1.dp).background(Color(0xFF30363D)))

            // Editor Text Input
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
                    .padding(start = 12.dp, top = 12.dp, end = 24.dp, bottom = 24.dp)
            ) {
                BasicTextField(
                    value = content,
                    onValueChange = { onContentChange(it) },
                    textStyle = TextStyle(
                        color = Color(0xFFE6EDF3),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    ),
                    cursorBrush = SolidColor(Color(0xFF58A6FF)),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("code_editor_input")
                )
            }
        }
    }
}

@Composable
private fun SnippetChip(text: String, onClick: () -> Unit) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = { Text(text, fontSize = 10.sp, color = Color(0xFF79C0FF)) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color(0xFF21262D)
        ),
        shape = RoundedCornerShape(4.dp)
    )
}
