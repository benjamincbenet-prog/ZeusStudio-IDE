package com.example.zeus.engine.parser

import androidx.compose.ui.graphics.Color
import com.example.zeus.model.SensorSimulationState
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Zepp OS Widget Types supported by @zos/ui on Amazfit Bip Max (432x514)
 */
enum class ZeppWidgetType {
    FILL_RECT,
    STROKE_RECT,
    TEXT,
    ARC,
    CIRCLE,
    BUTTON,
    IMG,
    ANALOG_CLOCK,
    UNKNOWN
}

/**
 * Parsed Widget Definition from Zepp OS JavaScript AST
 */
data class ZeppWidgetDef(
    val id: String,
    val type: ZeppWidgetType,
    val x: Float = 0f,
    val y: Float = 0f,
    val w: Float = 100f,
    val h: Float = 100f,
    val color: Long = 0xFFFFFFFF,
    val text: String = "",
    val textSize: Float = 20f,
    val startAngle: Float = 0f,
    val endAngle: Float = 360f,
    val lineWidth: Float = 6f,
    val radius: Float = 0f,
    val align: String = "CENTER",
    val sensorBinding: String? = null,
    val rawProperties: Map<String, String> = emptyMap()
)

/**
 * Resolved Renderable Widget with computed real-time sensor/clock values
 */
data class RenderableZeppWidget(
    val id: String,
    val type: ZeppWidgetType,
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val color: Color,
    val text: String,
    val textSize: Float,
    val startAngle: Float,
    val endAngle: Float,
    val lineWidth: Float,
    val radius: Float,
    val align: String
)

/**
 * Diagnostic Syntax Issue
 */
data class ZeppSyntaxIssue(
    val line: Int,
    val column: Int,
    val message: String,
    val isError: Boolean,
    val fileName: String
)

/**
 * Complete AST analysis of a Zepp OS JavaScript program
 */
data class ZeppParsedProgram(
    val fileName: String,
    val imports: List<String> = emptyList(),
    val widgets: List<ZeppWidgetDef> = emptyList(),
    val usesSensors: List<String> = emptyList(),
    val appType: String = "watchface",
    val errors: List<ZeppSyntaxIssue> = emptyList(),
    val warnings: List<ZeppSyntaxIssue> = emptyList(),
    val linesOfCode: Int = 0,
    val tokenCount: Int = 0
)

/**
 * Real JavaScript Lexer and Parser for Zepp OS (Zeus Toolchain)
 */
object ZeppJsParser {

    private val ZEUS_MODULES = setOf(
        "@zos/ui",
        "@zos/sensor",
        "@zos/router",
        "@zos/storage",
        "@zos/ble",
        "@zos/device",
        "@zos/app",
        "@zos/display",
        "@zos/utils",
        "@zos/notification",
        "@zos/media"
    )

    private val ILLEGAL_BROWSER_APIS = listOf(
        "window." to "Browser global 'window' is unavailable in Zepp OS QuickJS runtime.",
        "document." to "Browser DOM 'document' is unavailable in Zepp OS. Use createWidget() from @zos/ui.",
        "localStorage." to "Browser 'localStorage' is unavailable. Use @zos/storage instead.",
        "XMLHttpRequest" to "Browser XMLHttpRequest is unsupported. Use @zos/ble or @zos/app companion bridge.",
        "alert(" to "Browser alert() is unavailable. Use createWidget(widget.TEXT) or system dialogs."
    )

    /**
     * Parse and statically analyze a Zepp OS JavaScript file
     */
    fun parse(fileName: String, content: String): ZeppParsedProgram {
        val lines = content.lines()
        val errors = mutableListOf<ZeppSyntaxIssue>()
        val warnings = mutableListOf<ZeppSyntaxIssue>()
        val imports = mutableListOf<String>()
        val usesSensors = mutableListOf<String>()
        val widgets = mutableListOf<ZeppWidgetDef>()

        var openBraces = 0
        var openParens = 0
        var openBrackets = 0
        var tokenCount = 0

        // 1. Line-by-line lexical & syntax pass
        lines.forEachIndexed { index, lineText ->
            val lineNum = index + 1
            val trimmed = lineText.trim()

            // Count tokens
            val tokens = lineText.split(Regex("[\\s,;(){}\\[\\]]+")).filter { it.isNotEmpty() }
            tokenCount += tokens.size

            // Count braces and structure balance (ignoring string contents approximately)
            val nonStringText = lineText.replace(Regex("\"[^\"]*\"|'[^']*'|`[^`]*`"), "")
            openBraces += nonStringText.count { it == '{' } - nonStringText.count { it == '}' }
            openParens += nonStringText.count { it == '(' } - nonStringText.count { it == ')' }
            openBrackets += nonStringText.count { it == '[' } - nonStringText.count { it == ']' }

            // Check illegal browser APIs
            for ((apiPattern, errorMsg) in ILLEGAL_BROWSER_APIS) {
                if (lineText.contains(apiPattern)) {
                    val col = lineText.indexOf(apiPattern) + 1
                    errors.add(ZeppSyntaxIssue(lineNum, col, errorMsg, isError = true, fileName = fileName))
                }
            }

            // Check Imports
            if (trimmed.startsWith("import ") && trimmed.contains("from")) {
                val match = Regex("from\\s+['\"]([^'\"]+)['\"]").find(trimmed)
                if (match != null) {
                    val moduleName = match.groupValues[1]
                    imports.add(moduleName)
                    if (!ZEUS_MODULES.contains(moduleName) && !moduleName.startsWith("./") && !moduleName.startsWith("../")) {
                        warnings.add(
                            ZeppSyntaxIssue(
                                lineNum, 1,
                                "Unknown module '$moduleName'. Official Zepp OS modules start with '@zos/'.",
                                isError = false,
                                fileName = fileName
                            )
                        )
                    }
                }
            }

            // Sensor usage detection
            listOf("Time", "HeartRate", "Step", "Battery", "Calorie", "Distance", "Weather", "Barometer", "Compass").forEach { sensor ->
                if (lineText.contains(sensor)) {
                    if (!usesSensors.contains(sensor)) usesSensors.add(sensor)
                }
            }
        }

        // Structural balance validation
        if (openBraces != 0) {
            errors.add(
                ZeppSyntaxIssue(
                    lines.size, 1,
                    "Syntax Error: Unbalanced curly braces { } (Delta: $openBraces).",
                    isError = true,
                    fileName = fileName
                )
            )
        }
        if (openParens != 0) {
            errors.add(
                ZeppSyntaxIssue(
                    lines.size, 1,
                    "Syntax Error: Unbalanced parentheses ( ) (Delta: $openParens).",
                    isError = true,
                    fileName = fileName
                )
            )
        }
        if (openBrackets != 0) {
            errors.add(
                ZeppSyntaxIssue(
                    lines.size, 1,
                    "Syntax Error: Unbalanced square brackets [ ] (Delta: $openBrackets).",
                    isError = true,
                    fileName = fileName
                )
            )
        }

        // 2. Extract createWidget() calls and build UI AST
        val widgetCalls = extractCreateWidgetCalls(content)
        widgets.addAll(widgetCalls)

        val appType = when {
            content.contains("WatchFace(") -> "watchface"
            content.contains("App(") -> "app"
            content.contains("Page(") -> "page"
            else -> "module"
        }

        return ZeppParsedProgram(
            fileName = fileName,
            imports = imports,
            widgets = widgets,
            usesSensors = usesSensors,
            appType = appType,
            errors = errors,
            warnings = warnings,
            linesOfCode = lines.size,
            tokenCount = tokenCount
        )
    }

    /**
     * Regex and object literal parser for createWidget(widget.TYPE, { ... })
     */
    private fun extractCreateWidgetCalls(content: String): List<ZeppWidgetDef> {
        val widgets = mutableListOf<ZeppWidgetDef>()
        val regex = Regex("createWidget\\s*\\(\\s*widget\\.([A-Z_]+)\\s*,\\s*\\{([\\s\\S]*?)\\}\\s*\\)", RegexOption.MULTILINE)

        var widgetIndex = 0
        regex.findAll(content).forEach { match ->
            widgetIndex++
            val typeStr = match.groupValues[1]
            val propsBody = match.groupValues[2]

            val type = when (typeStr) {
                "FILL_RECT" -> ZeppWidgetType.FILL_RECT
                "STROKE_RECT" -> ZeppWidgetType.STROKE_RECT
                "TEXT" -> ZeppWidgetType.TEXT
                "ARC" -> ZeppWidgetType.ARC
                "CIRCLE" -> ZeppWidgetType.CIRCLE
                "BUTTON" -> ZeppWidgetType.BUTTON
                "IMG" -> ZeppWidgetType.IMG
                "TIME_POINTER", "ANALOG_CLOCK" -> ZeppWidgetType.ANALOG_CLOCK
                else -> ZeppWidgetType.UNKNOWN
            }

            val props = parseObjectProps(propsBody)

            val x = props["x"]?.toFloatOrNull() ?: 0f
            val y = props["y"]?.toFloatOrNull() ?: 0f
            val w = props["w"]?.toFloatOrNull() ?: 100f
            val h = props["h"]?.toFloatOrNull() ?: 100f

            val colorLong = parseColorHex(props["color"] ?: props["normal_color"] ?: "0xFFFFFF")
            val text = props["text"]?.trim('\'', '"', '`') ?: ""
            val textSize = props["text_size"]?.toFloatOrNull() ?: props["size"]?.toFloatOrNull() ?: 22f
            val startAngle = props["start_angle"]?.toFloatOrNull() ?: 0f
            val endAngle = props["end_angle"]?.toFloatOrNull() ?: 360f
            val lineWidth = props["line_width"]?.toFloatOrNull() ?: 6f
            val radius = props["radius"]?.toFloatOrNull() ?: 0f
            val align = props["align_h"] ?: "CENTER"

            widgets.add(
                ZeppWidgetDef(
                    id = "widget_$widgetIndex",
                    type = type,
                    x = x,
                    y = y,
                    w = w,
                    h = h,
                    color = colorLong,
                    text = text,
                    textSize = textSize,
                    startAngle = startAngle,
                    endAngle = endAngle,
                    lineWidth = lineWidth,
                    radius = radius,
                    align = align,
                    rawProperties = props
                )
            )
        }

        return widgets
    }

    private fun parseObjectProps(propsString: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val lines = propsString.split(Regex("[,\\n]"))
        for (l in lines) {
            val colonIdx = l.indexOf(':')
            if (colonIdx != -1) {
                val key = l.substring(0, colonIdx).trim()
                val value = l.substring(colonIdx + 1).trim().trim('\'', '"', '`')
                if (key.isNotEmpty()) {
                    map[key] = value
                }
            }
        }
        return map
    }

    private fun parseColorHex(raw: String): Long {
        val clean = raw.trim()
        return try {
            when {
                clean.startsWith("0x") || clean.startsWith("0X") -> {
                    val hex = clean.substring(2)
                    val value = hex.toLong(16)
                    if (value <= 0xFFFFFF) {
                        0xFF000000 or value
                    } else {
                        value
                    }
                }
                clean.startsWith("#") -> {
                    val hex = clean.substring(1)
                    val value = hex.toLong(16)
                    if (value <= 0xFFFFFF) {
                        0xFF000000 or value
                    } else {
                        value
                    }
                }
                else -> 0xFFFFFFFF
            }
        } catch (_: Exception) {
            0xFFFFFFFF
        }
    }

    /**
     * Validate app.json manifest file
     */
    fun validateManifest(jsonContent: String): Pair<Boolean, List<String>> {
        val errors = mutableListOf<String>()
        try {
            val json = JSONObject(jsonContent)
            if (!json.has("app")) {
                errors.add("Missing required 'app' object in app.json")
            } else {
                val appObj = json.getJSONObject("app")
                if (!appObj.has("appId")) errors.add("Missing 'app.appId' in app.json")
                if (!appObj.has("appName")) errors.add("Missing 'app.appName' in app.json")
                if (!appObj.has("appType")) errors.add("Missing 'app.appType' in app.json")
            }

            if (!json.has("targets")) {
                errors.add("Missing 'targets' configuration in app.json")
            } else {
                val targets = json.getJSONObject("targets")
                if (!targets.has("bip_max") && !targets.has("target")) {
                    errors.add("Warning: No target explicitly defined for 'bip_max' (432x514)")
                }
            }
        } catch (e: Exception) {
            errors.add("Invalid JSON format in app.json: ${e.localizedMessage}")
        }
        return Pair(errors.isEmpty(), errors)
    }

    /**
     * Real JavaScript minifier
     */
    fun minifyJs(source: String): String {
        // Strip block comments /* ... */
        var min = source.replace(Regex("/\\*[\\s\\S]*?\\*/"), "")
        // Strip line comments // ...
        min = min.lines()
            .map { line ->
                val commentIdx = line.indexOf("//")
                if (commentIdx != -1) line.substring(0, commentIdx) else line
            }
            .filter { it.trim().isNotEmpty() }
            .joinToString("\n") { it.trim() }

        return min
    }
}
