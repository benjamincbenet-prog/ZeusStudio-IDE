package com.example.zeus.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.CornerRadius
import com.example.zeus.engine.parser.ZeppJsParser
import com.example.zeus.engine.parser.ZeppWidgetType
import com.example.zeus.engine.runtime.ZeppRuntimeInterpreter
import com.example.zeus.model.SensorSimulationState
import com.example.zeus.model.ZeusProject
import com.example.zeus.model.ZeusTemplate
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BipMaxWatchSimulator(
    project: ZeusProject,
    sensorState: SensorSimulationState,
    onHeartRateChange: (Int) -> Unit,
    onStepsChange: (Int) -> Unit,
    onBatteryChange: (Int) -> Unit,
    onWeatherChange: (String, Int) -> Unit,
    onDeployClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTimeString by remember { mutableStateOf("") }
    var currentSecString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }
    var interactiveCounter by remember { mutableIntStateOf(0) }
    var isWorkoutPaused by remember { mutableStateOf(false) }
    var runtimeMode by remember { mutableStateOf(0) } // 0: Live JS AST Runtime, 1: High-Fidelity View

    val activeJsFile = remember(project.files, project.activeFileId) {
        project.files.firstOrNull { it.id == project.activeFileId && it.name.endsWith(".js") }
            ?: project.files.firstOrNull { it.name.endsWith(".js") }
    }

    val parsedJsProgram = remember(activeJsFile?.content) {
        if (activeJsFile != null) {
            ZeppJsParser.parse(activeJsFile.name, activeJsFile.content)
        } else null
    }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val secFormat = SimpleDateFormat(":ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE • MMM dd", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTimeString = timeFormat.format(now)
            currentSecString = secFormat.format(now)
            currentDateString = dateFormat.format(now).uppercase()
            delay(1000)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000 / sensorState.heartRateBpm.coerceIn(40, 200), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartPulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Device header banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Amazfit Bip Max • 2.07\" HD AMOLED (3,000 Nits)",
                        color = Color(0xFF38BDF8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Resolution: 432 x 514 px (302 PPI) • Zepp OS ${project.zeppOsVersion} • 550 mAh",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = runtimeMode == 0,
                        onClick = { runtimeMode = 0 },
                        label = { Text("Live AST (${parsedJsProgram?.widgets?.size ?: 0} W)", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0284C7),
                            selectedLabelColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FilterChip(
                        selected = runtimeMode == 1,
                        onClick = { runtimeMode = 1 },
                        label = { Text("Preset UI", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF475569),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Physical Watch Frame Container (Simulates Amazfit Bip Max)
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Watch body casing (Dark Titanium Bezel)
            Box(
                modifier = Modifier
                    .width(260.dp)
                    .height(310.dp)
                    .shadow(16.dp, RoundedCornerShape(44.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF2D3748),
                                Color(0xFF1A202C),
                                Color(0xFF0F172A)
                            )
                        ),
                        shape = RoundedCornerShape(44.dp)
                    )
                    .border(2.5.dp, Color(0xFF475569), RoundedCornerShape(44.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Screen Glass Frame (432x514 ratio: ~0.84)
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .height(262.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFF050811))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(32.dp))
                        .testTag("bip_max_display_canvas")
                ) {
                    if (runtimeMode == 0 && parsedJsProgram != null && parsedJsProgram.widgets.isNotEmpty()) {
                        DynamicZeppWidgetCanvas(
                            widgets = parsedJsProgram.widgets,
                            sensorState = sensorState,
                            currentTimeString = currentTimeString.ifEmpty { "10:09" }
                        )
                    } else {
                        // Watch Content based on Project Template
                        when (project.template) {
                            ZeusTemplate.BIP_MAX_DIGITAL_PRO -> {
                                DigitalProWatchFaceView(
                                    time = currentTimeString.ifEmpty { "10:09" },
                                    sec = currentSecString.ifEmpty { ":42" },
                                    date = currentDateString.ifEmpty { "MON • OCT 24" },
                                    sensorState = sensorState,
                                    pulseScale = pulseScale
                                )
                            }
                            ZeusTemplate.BIP_MAX_FITNESS_TRACKER -> {
                                FitnessTrackerView(
                                    sensorState = sensorState,
                                    isPaused = isWorkoutPaused,
                                    onTogglePause = { isWorkoutPaused = !isWorkoutPaused }
                                )
                            }
                            ZeusTemplate.BIP_MAX_WEATHER_WIDGET -> {
                                WeatherWidgetView(sensorState = sensorState)
                            }
                            ZeusTemplate.BIP_MAX_BLE_CONTROLLER -> {
                                BleRemoteView(
                                    interactiveCounter = interactiveCounter,
                                    onButtonClick = { interactiveCounter++ }
                                )
                            }
                            ZeusTemplate.BIP_MAX_MINIMAL_ANALOG -> {
                                MinimalAnalogView(
                                    date = currentDateString.ifEmpty { "OCT 24" }
                                )
                            }
                        }
                    }
                }
            }

            // Physical Crown / Side Button
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
                    .width(8.dp)
                    .height(36.dp)
                    .background(Color(0xFF64748B), RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .clickable {
                        // Crown press toggles or wakes
                    }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sensor Simulation Sandbox
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Sensor Sandbox",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sensor Simulation Sandbox",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "Real-time Telemetry",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Heart Rate Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("♥ Heart Rate (BPM)", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("${sensorState.heartRateBpm} BPM", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = sensorState.heartRateBpm.toFloat(),
                    onValueChange = { onHeartRateChange(it.toInt()) },
                    valueRange = 45f..185f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFEF4444),
                        activeTrackColor = Color(0xFFEF4444)
                    ),
                    modifier = Modifier.testTag("hr_slider")
                )

                // Steps Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🏃 Step Count", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("${sensorState.steps} / 10,000", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = sensorState.steps.toFloat(),
                    onValueChange = { onStepsChange(it.toInt()) },
                    valueRange = 500f..18000f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF10B981),
                        activeTrackColor = Color(0xFF10B981)
                    ),
                    modifier = Modifier.testTag("steps_slider")
                )

                // Battery Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("⚡ Battery Level", color = Color(0xFF22C55E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("${sensorState.batteryPercent}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = sensorState.batteryPercent.toFloat(),
                    onValueChange = { onBatteryChange(it.toInt()) },
                    valueRange = 5f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF22C55E),
                        activeTrackColor = Color(0xFF22C55E)
                    ),
                    modifier = Modifier.testTag("battery_slider")
                )

                // Weather preset chips
                Spacer(modifier = Modifier.height(6.dp))
                Text("☀️ Weather Presets", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Sunny (24°C)" to (24 to "Sunny"),
                        "Rain (16°C)" to (16 to "Rain"),
                        "Cloudy (20°C)" to (20 to "Cloudy")
                    ).forEach { (label, pair) ->
                        val isSelected = sensorState.weatherCondition == pair.second
                        FilterChip(
                            selected = isSelected,
                            onClick = { onWeatherChange(pair.second, pair.first) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hardware Specifications & Target Platform Verification Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Amazfit Bip Max Target Hardware Specs",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "VERIFIED",
                        color = Color(0xFF22C55E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                listOf(
                    "Display" to "2.07\" HD AMOLED Curved (3,000 Nits)",
                    "Resolution" to "432 x 514 pixels • 302 PPI",
                    "OS Support" to "Zepp OS 5.0 / 6.0 (Zeus CLI Target: bip_max)",
                    "Battery" to "550 mAh (Up to 18-day typical endurance)",
                    "Sensors" to "BioTracker 5.0 PPG, 3-Axis Accel, Geomagnetic, Barometer",
                    "Connectivity" to "Bluetooth 5.3 BLE Master/Slave + 4-Satellite GNSS",
                    "Water Resistance" to "5 ATM / 50 meters swimming proof"
                ).forEach { (specKey, specVal) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(specKey, color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(specVal, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun DigitalProWatchFaceView(
    time: String,
    sec: String,
    date: String,
    sensorState: SensorSimulationState,
    pulseScale: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Date
            Text(
                text = date,
                color = Color(0xFF00E5FF),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Center Digital Time
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = time,
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = sec,
                    color = Color(0xFF38BDF8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Dual Arcs (Steps & Heart Rate)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Steps gauge
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val stepFraction = (sensorState.steps.toFloat() / sensorState.stepGoal).coerceIn(0f, 1f)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color(0xFF1E293B),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color(0xFF10B981),
                            startAngle = 135f,
                            sweepAngle = 270f * stepFraction,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${sensorState.steps / 1000}.${(sensorState.steps % 1000) / 100}k",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "STEPS",
                            color = Color(0xFF94A3B8),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Heart rate gauge
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val hrFraction = (sensorState.heartRateBpm - 40f) / 140f
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color(0xFF1E293B),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color(0xFFEF4444),
                            startAngle = 135f,
                            sweepAngle = 270f * hrFraction.coerceIn(0f, 1f),
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${sensorState.heartRateBpm}",
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "♥ BPM",
                            color = Color(0xFFF87171),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer Complications
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "⚡ ${sensorState.batteryPercent}%",
                    color = Color(0xFF22C55E),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "🔥 ${sensorState.calorieKcal} KCAL",
                    color = Color(0xFFF97316),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FitnessTrackerView(
    sensorState: SensorSimulationState,
    isPaused: Boolean,
    onTogglePause: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⚡ HIIT INTERVAL",
            color = Color(0xFFF59E0B),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "18:45",
            color = Color(0xFF00E5FF),
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "♥ ${sensorState.heartRateBpm} BPM",
                    color = Color(0xFFEF4444),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (sensorState.heartRateBpm > 140) "ZONE 4: ANAEROBIC" else "ZONE 3: CARDIO",
                    color = Color(0xFFF87171),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("🔥 ${sensorState.calorieKcal} kcal", color = Color(0xFFF97316), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("👟 168 spm", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = onTogglePause,
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isPaused) Color(0xFF10B981) else Color(0xFFF59E0B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isPaused) "RESUME" else "PAUSE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("FINISH", fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WeatherWidgetView(sensorState: SensorSimulationState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("SAN FRANCISCO", color = Color(0xFF93C5FD), fontSize = 9.sp, fontWeight = FontWeight.Bold)

        Text(
            text = "${sensorState.temperatureCelsius}°C",
            color = Color.White,
            fontSize = 38.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            text = "☀️ ${sensorState.weatherCondition} • H: 26° L: 16°",
            color = Color(0xFF38BDF8),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("AQI: 24 (Good)", color = Color(0xFF34D399), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text("💧 Humidity: 58%", color = Color(0xFF60A5FA), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("UV Index: 5", color = Color(0xFFFBBF24), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text("🧭 1016 hPa", color = Color(0xFFA78BFA), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Tue 24° • Wed 25° • Thu 21°",
            color = Color(0xFF94A3B8),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun BleRemoteView(
    interactiveCounter: Int,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("● BLE LINKED: PHONE", color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(6.dp))

        Text("Midnight City - M83", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF1E293B), CircleShape)
                    .clickable { onButtonClick() },
                contentAlignment = Alignment.Center
            ) {
                Text("⏮", color = Color.White, fontSize = 14.sp)
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(Color(0xFF00E5FF), CircleShape)
                    .clickable { onButtonClick() },
                contentAlignment = Alignment.Center
            ) {
                Text("▶", color = Color.Black, fontSize = 18.sp)
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF1E293B), CircleShape)
                    .clickable { onButtonClick() },
                contentAlignment = Alignment.Center
            ) {
                Text("⏭", color = Color.White, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onButtonClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("📷 Trigger Camera Shutter", fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        if (interactiveCounter > 0) {
            Text(
                text = "Sent $interactiveCounter BLE packet(s)",
                color = Color(0xFF22C55E),
                fontSize = 8.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun MinimalAnalogView(date: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2 - 8.dp.toPx()

            // Outer luxury dial ring
            drawCircle(
                color = Color(0xFF334155),
                radius = radius,
                style = Stroke(width = 2.dp.toPx())
            )

            // Hour hand (~10:10)
            val hourAngle = Math.toRadians(300.0)
            val hourLength = radius * 0.5f
            drawLine(
                color = Color.White,
                start = center,
                end = Offset(
                    (center.x + hourLength * sin(hourAngle)).toFloat(),
                    (center.y - hourLength * cos(hourAngle)).toFloat()
                ),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Minute hand (~10:10)
            val minAngle = Math.toRadians(60.0)
            val minLength = radius * 0.75f
            drawLine(
                color = Color(0xFF00E5FF),
                start = center,
                end = Offset(
                    (center.x + minLength * sin(minAngle)).toFloat(),
                    (center.y - minLength * cos(minAngle)).toFloat()
                ),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Center Pin
            drawCircle(color = Color(0xFFEF4444), radius = 4.dp.toPx())
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("12", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(date, color = Color(0xFFF59E0B), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
            Text("6", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DynamicZeppWidgetCanvas(
    widgets: List<com.example.zeus.engine.parser.ZeppWidgetDef>,
    sensorState: SensorSimulationState,
    currentTimeString: String,
    modifier: Modifier = Modifier
) {
    val evaluated = remember(widgets, sensorState, currentTimeString) {
        ZeppRuntimeInterpreter.evaluateWidgets(widgets, sensorState)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
    ) {
        val scaleX = maxWidth.value / 432f
        val scaleY = maxHeight.value / 514f

        Canvas(modifier = Modifier.fillMaxSize()) {
            for (w in evaluated) {
                val left = w.x * scaleX
                val top = w.y * scaleY
                val width = w.w * scaleX
                val height = w.h * scaleY

                when (w.type) {
                    ZeppWidgetType.FILL_RECT -> {
                        drawRoundRect(
                            color = w.color,
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            cornerRadius = CornerRadius(w.radius * scaleX, w.radius * scaleY)
                        )
                    }
                    ZeppWidgetType.STROKE_RECT -> {
                        drawRoundRect(
                            color = w.color,
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            cornerRadius = CornerRadius(w.radius * scaleX, w.radius * scaleY),
                            style = Stroke(width = (w.lineWidth * scaleX).coerceAtLeast(1f))
                        )
                    }
                    ZeppWidgetType.ARC -> {
                        drawArc(
                            color = w.color,
                            startAngle = w.startAngle - 90f,
                            sweepAngle = (w.endAngle - w.startAngle),
                            useCenter = false,
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            style = Stroke(width = (w.lineWidth * scaleX).coerceAtLeast(2f), cap = StrokeCap.Round)
                        )
                    }
                    ZeppWidgetType.CIRCLE -> {
                        drawCircle(
                            color = w.color,
                            radius = width / 2f,
                            center = Offset(left + width / 2f, top + height / 2f)
                        )
                    }
                    else -> {}
                }
            }
        }

        // Overlay text and buttons
        for (w in evaluated) {
            val left = (w.x * scaleX).dp
            val top = (w.y * scaleY).dp
            val width = (w.w * scaleX).dp
            val height = (w.h * scaleY).dp

            if (w.type == ZeppWidgetType.TEXT && w.text.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .offset(x = left, y = top)
                        .size(width = width, height = height),
                    contentAlignment = when (w.align) {
                        "LEFT" -> Alignment.CenterStart
                        "RIGHT" -> Alignment.CenterEnd
                        else -> Alignment.Center
                    }
                ) {
                    Text(
                        text = w.text,
                        color = w.color,
                        fontSize = (w.textSize * scaleY).coerceAtLeast(8f).sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = when (w.align) {
                            "LEFT" -> TextAlign.Start
                            "RIGHT" -> TextAlign.End
                            else -> TextAlign.Center
                        }
                    )
                }
            } else if (w.type == ZeppWidgetType.BUTTON) {
                Box(
                    modifier = Modifier
                        .offset(x = left, y = top)
                        .size(width = width, height = height)
                        .background(w.color, RoundedCornerShape((w.radius * scaleX).dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = w.text.ifEmpty { "BUTTON" },
                        color = Color.White,
                        fontSize = (w.textSize * scaleY).coerceAtLeast(10f).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

