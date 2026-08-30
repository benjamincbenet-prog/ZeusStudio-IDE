package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.zeus.data.ZeusTemplatesRepository
import com.example.zeus.engine.CliCommandResult
import com.example.zeus.engine.ZeusCliRunner
import com.example.zeus.engine.ZeusCompiler
import com.example.zeus.engine.bundler.ZeusPackageBuilder
import com.example.zeus.engine.parser.ZeppJsParser
import com.example.zeus.engine.parser.ZeppWidgetType
import com.example.zeus.engine.runtime.ZeppRuntimeInterpreter
import com.example.zeus.model.SensorSimulationState
import com.example.zeus.model.ZeusTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.util.Date
import java.util.zip.ZipInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

  @Test
  fun readStringFromContext() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Zeus IDE", appName)
  }

  @Test
  fun testZeusCompilerForAllTemplates() {
    // Verify every single standard template compiles with genuine binary packages and valid manifest
    val templates = listOf(
      ZeusTemplate.BIP_MAX_DIGITAL_PRO,
      ZeusTemplate.BIP_MAX_FITNESS_TRACKER,
      ZeusTemplate.BIP_MAX_WEATHER_WIDGET,
      ZeusTemplate.BIP_MAX_BLE_CONTROLLER,
      ZeusTemplate.BIP_MAX_MINIMAL_ANALOG
    )

    for (tpl in templates) {
      val project = ZeusTemplatesRepository.createProject(tpl)
      val result = ZeusCompiler.compile(project)

      assertTrue("Compilation should succeed for ${tpl.title}", result.isSuccess)
      assertNotNull("Generated package should not be null for ${tpl.title}", result.zabPackage)
      assertEquals("432x514", result.zabPackage?.resolution)
      assertNotNull("Package binary bytes must exist for ${tpl.title}", result.zabPackage?.packageBytes)
      assertTrue("Package byte size must be > 0", (result.zabPackage?.packageBytes?.size ?: 0) > 0)
      assertTrue("SHA-256 digest must be populated", result.zabPackage?.sha256Digest?.isNotEmpty() == true)
      assertTrue("CRC32 must be generated", result.zabPackage?.checksumCrc32?.startsWith("0x") == true)
    }
  }

  @Test
  fun testZabZipArchiveStructureAndIntegrity() {
    val project = ZeusTemplatesRepository.createProject(ZeusTemplate.BIP_MAX_DIGITAL_PRO)
    val zab = ZeusPackageBuilder.buildZabPackage(project, isRelease = true)

    assertNotNull(zab.packageBytes)
    val zipStream = ZipInputStream(ByteArrayInputStream(zab.packageBytes!!))
    val entries = mutableListOf<String>()

    var entry = zipStream.nextEntry
    while (entry != null) {
      entries.add(entry.name)
      zipStream.closeEntry()
      entry = zipStream.nextEntry
    }

    // Verify required Zepp OS package files
    assertTrue("Archive must contain app.json", entries.contains("app.json"))
    assertTrue("Archive must contain manifest.json", entries.contains("manifest.json"))
    assertTrue("Archive must contain device/bip_max.json", entries.contains("device/bip_max.json"))
    assertTrue("Archive must contain keys/signature.bin", entries.contains("keys/signature.bin"))
    assertTrue("Archive must contain assets/icon.png", entries.contains("assets/icon.png"))
    assertTrue("Archive must contain index.js", entries.any { it.endsWith("index.js") })
  }

  @Test
  fun testZeppJsParserAstExtraction() {
    val jsCode = """
      import { createWidget, widget, align } from '@zos/ui'
      import { HeartRate, Step } from '@zos/sensor'

      WatchFace({
        initView() {
          createWidget(widget.FILL_RECT, {
            x: 0,
            y: 0,
            w: 432,
            h: 514,
            color: 0x050811
          })

          createWidget(widget.TEXT, {
            x: 24,
            y: 90,
            w: 384,
            h: 96,
            color: 0x38BDF8,
            text_size: 76,
            align_h: align.CENTER,
            text: '{time}'
          })

          createWidget(widget.ARC, {
            x: 44,
            y: 280,
            w: 120,
            h: 120,
            color: 0x22C55E,
            start_angle: 0,
            end_angle: 270,
            line_width: 8,
            data_type: 'STEP'
          })
        }
      })
    """.trimIndent()

    val parsed = ZeppJsParser.parse("test_index.js", jsCode)
    assertEquals(3, parsed.widgets.size)
    assertEquals("watchface", parsed.appType)
    assertEquals(0, parsed.errors.size)
    assertTrue("Detected @zos/ui import", parsed.imports.contains("@zos/ui"))
    assertTrue("Detected @zos/sensor import", parsed.imports.contains("@zos/sensor"))
    assertTrue("Detected HeartRate sensor", parsed.usesSensors.contains("HeartRate"))
    assertTrue("Detected Step sensor", parsed.usesSensors.contains("Step"))

    val fillRect = parsed.widgets[0]
    assertEquals(ZeppWidgetType.FILL_RECT, fillRect.type)
    assertEquals(432f, fillRect.w)
    assertEquals(514f, fillRect.h)

    val timeText = parsed.widgets[1]
    assertEquals(ZeppWidgetType.TEXT, timeText.type)
    assertEquals("{time}", timeText.text)
    assertEquals(76f, timeText.textSize)

    val stepArc = parsed.widgets[2]
    assertEquals(ZeppWidgetType.ARC, stepArc.type)
    assertEquals(8f, stepArc.lineWidth)
  }

  @Test
  fun testRuntimeDynamicSensorEvaluation() {
    val jsCode = """
      createWidget(widget.TEXT, {
        x: 10, y: 10, w: 100, h: 40,
        text: '{bpm} BPM'
      })
      createWidget(widget.TEXT, {
        x: 10, y: 50, w: 100, h: 40,
        text: '{steps} STEPS'
      })
      createWidget(widget.ARC, {
        x: 20, y: 20, w: 100, h: 100,
        start_angle: 0,
        end_angle: 360,
        data_type: 'STEP'
      })
    """.trimIndent()

    val parsed = ZeppJsParser.parse("sensors.js", jsCode)
    val sensorState = SensorSimulationState(
      heartRateBpm = 142,
      steps = 6000,
      stepGoal = 10000,
      batteryPercent = 85
    )

    val evaluated = ZeppRuntimeInterpreter.evaluateWidgets(
      parsed.widgets,
      sensorState,
      Date()
    )

    assertEquals(3, evaluated.size)
    assertEquals("142 BPM", evaluated[0].text)
    assertEquals("6000 STEPS", evaluated[1].text)
    // Progress should be 6000 / 10000 = 60% of 360 = 216
    assertEquals(216f, evaluated[2].endAngle, 1.0f)
  }

  @Test
  fun testManifestValidation() {
    val validJson = """
      {
        "configVersion": "v3",
        "app": {
          "appType": "watchface",
          "appName": "BipMaxWatch",
          "appId": 1008601
        },
        "targets": {
          "bip_max": {
            "designWidth": 432,
            "designHeight": 514
          }
        }
      }
    """.trimIndent()

    val (valid, issues) = ZeppJsParser.validateManifest(validJson)
    assertTrue("Valid manifest should pass validation", valid)
    assertTrue(issues.isEmpty())

    val brokenJson = """
      {
        "configVersion": "v3"
      }
    """.trimIndent()

    val (invalid, errorIssues) = ZeppJsParser.validateManifest(brokenJson)
    assertFalse("Invalid manifest should fail", invalid)
    assertTrue("Should report missing app block", errorIssues.any { it.contains("Missing required 'app'") })
  }

  @Test
  fun testJsMinification() {
    val rawJs = """
      // This is a header comment
      import { createWidget } from '@zos/ui'; /* inline comment */

      /* Multiline
         block comment */
      export function init() {
        // Log step
        console.log("Hello Bip Max");
      }
    """.trimIndent()

    val minified = ZeppJsParser.minifyJs(rawJs)
    assertFalse("Should strip comments", minified.contains("This is a header comment"))
    assertFalse("Should strip block comments", minified.contains("Multiline"))
    assertTrue("Should preserve imports", minified.contains("import { createWidget } from '@zos/ui'"))
    assertTrue("Should preserve functions", minified.contains("console.log(\"Hello Bip Max\")"))
  }

  @Test
  fun testZeusCompilerCatchesSyntaxErrors() {
    val project = ZeusTemplatesRepository.createProject(ZeusTemplate.BIP_MAX_DIGITAL_PRO)
    val brokenFiles = project.files.map {
      if (it.name == "index.js") {
        it.copy(content = "WatchFace({ initView() { window.alert('error') ") // Missing closing braces & illegal window API
      } else it
    }
    val brokenProject = project.copy(files = brokenFiles)
    val result = ZeusCompiler.compile(brokenProject)

    assertTrue("Compilation should fail for broken syntax", !result.isSuccess)
    assertTrue("Errors should be detected", result.errors.isNotEmpty())
  }

  @Test
  fun testZeusCliRunnerCommands() {
    val project = ZeusTemplatesRepository.createProject(ZeusTemplate.BIP_MAX_DIGITAL_PRO)
    val helpResult = ZeusCliRunner.execute("zeus help", project, isDevRunning = false, isBleConnected = false)

    assertTrue(helpResult is CliCommandResult.Output)
    val logs = (helpResult as CliCommandResult.Output).logs
    assertTrue(logs.any { it.message.contains("Zeus CLI") })

    val lintResult = ZeusCliRunner.execute("zeus lint", project, isDevRunning = false, isBleConnected = false)
    assertTrue(lintResult is CliCommandResult.Output)
    val lintLogs = (lintResult as CliCommandResult.Output).logs
    assertTrue(lintLogs.any { it.message.contains("analysis") || it.message.contains("Clean") })

    val signResult = ZeusCliRunner.execute("zeus sign", project, isDevRunning = false, isBleConnected = false)
    assertTrue(signResult is CliCommandResult.Output)
    val signLogs = (signResult as CliCommandResult.Output).logs
    assertTrue(signLogs.any { it.message.contains("signed successfully") })
  }
}
