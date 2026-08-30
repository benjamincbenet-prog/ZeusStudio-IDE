package com.example.zeus.engine.runtime

import androidx.compose.ui.graphics.Color
import com.example.zeus.engine.parser.RenderableZeppWidget
import com.example.zeus.engine.parser.ZeppWidgetDef
import com.example.zeus.engine.parser.ZeppWidgetType
import com.example.zeus.model.SensorSimulationState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Real-time Zepp OS Runtime & Widget Tree Evaluator
 * Evaluates parsed JavaScript UI widget AST against live watch sensors and clock
 */
object ZeppRuntimeInterpreter {

    fun evaluateWidgets(
        widgetDefs: List<ZeppWidgetDef>,
        sensorState: SensorSimulationState,
        currentTime: Date = Date()
    ): List<RenderableZeppWidget> {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val secFormat = SimpleDateFormat(":ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE • MMM dd", Locale.getDefault())

        val timeStr = timeFormat.format(currentTime)
        val secStr = secFormat.format(currentTime)
        val dateStr = dateFormat.format(currentTime).uppercase()

        return widgetDefs.map { def ->
            val resolvedText = resolveDynamicText(def.text, def.rawProperties, sensorState, timeStr, secStr, dateStr)
            val resolvedColor = Color(def.color)

            // Dynamic Arc progress evaluation if binding is step goal / battery / heart rate
            var startAngle = def.startAngle
            var endAngle = def.endAngle

            if (def.type == ZeppWidgetType.ARC) {
                val propStr = def.rawProperties.values.joinToString(" ")
                if (propStr.contains("step") || def.text.contains("step") || def.rawProperties["data_type"] == "STEP") {
                    val progress = (sensorState.steps.toFloat() / sensorState.stepGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
                    endAngle = startAngle + (360f * progress)
                } else if (propStr.contains("battery") || def.text.contains("battery") || def.rawProperties["data_type"] == "BATTERY") {
                    val progress = (sensorState.batteryPercent / 100f).coerceIn(0f, 1f)
                    endAngle = startAngle + (360f * progress)
                }
            }

            RenderableZeppWidget(
                id = def.id,
                type = def.type,
                x = def.x,
                y = def.y,
                w = def.w,
                h = def.h,
                color = resolvedColor,
                text = resolvedText,
                textSize = def.textSize,
                startAngle = startAngle,
                endAngle = endAngle,
                lineWidth = def.lineWidth,
                radius = def.radius,
                align = def.align
            )
        }
    }

    private fun resolveDynamicText(
        rawText: String,
        props: Map<String, String>,
        sensor: SensorSimulationState,
        timeStr: String,
        secStr: String,
        dateStr: String
    ): String {
        var text = rawText

        // If text contains JS template placeholders like ${time} or sensor strings
        if (text.isEmpty() || text.contains("time") || text.contains("10:09") || props.containsKey("time")) {
            if (text.isEmpty() && props["data_type"] == "TIME") return timeStr
        }

        // Replacements for dynamic sensor interpolation
        text = text.replace("{time}", timeStr)
            .replace("{sec}", secStr)
            .replace("{date}", dateStr)
            .replace("{bpm}", "${sensor.heartRateBpm}")
            .replace("{heartRate}", "${sensor.heartRateBpm}")
            .replace("{steps}", "${sensor.steps}")
            .replace("{battery}", "${sensor.batteryPercent}%")
            .replace("{temp}", "${sensor.temperatureCelsius}°C")
            .replace("{weather}", sensor.weatherCondition)
            .replace("{calorie}", "${sensor.calorieKcal} kcal")
            .replace("{distance}", String.format(Locale.US, "%.1f km", sensor.distanceKm))
            .replace("{spO2}", "${sensor.spO2Percent}%")

        // Handle raw JS property tokens
        if (text.isEmpty()) {
            if (props.containsKey("text")) {
                val propText = props["text"] ?: ""
                return when {
                    propText.contains("time") || propText.contains("Time") -> timeStr
                    propText.contains("heart") || propText.contains("Heart") || propText.contains("bpm") -> "${sensor.heartRateBpm}"
                    propText.contains("step") || propText.contains("Step") -> "${sensor.steps}"
                    propText.contains("battery") || propText.contains("Battery") -> "${sensor.batteryPercent}%"
                    propText.contains("date") || propText.contains("Date") -> dateStr
                    else -> propText.trim('\'', '"', '`')
                }
            }
        }

        return text
    }
}
