package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.theme.ZeusIdeTheme
import com.example.zeus.data.ZeusTemplatesRepository
import com.example.zeus.model.SensorSimulationState
import com.example.zeus.model.ZeusTemplate
import com.example.zeus.ui.screens.BipMaxWatchSimulator
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [35])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val project = ZeusTemplatesRepository.createProject(ZeusTemplate.BIP_MAX_DIGITAL_PRO)
    composeTestRule.setContent {
      ZeusIdeTheme {
        BipMaxWatchSimulator(
          project = project,
          sensorState = SensorSimulationState(),
          onHeartRateChange = {},
          onStepsChange = {},
          onBatteryChange = {},
          onWeatherChange = { _, _ -> },
          onDeployClick = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
