package com.example.zeus.data

import com.example.zeus.model.FileType
import com.example.zeus.model.ZeusFile
import com.example.zeus.model.ZeusProject
import com.example.zeus.model.ZeusTemplate

object ZeusTemplatesRepository {

    fun createProject(template: ZeusTemplate, projectName: String? = null): ZeusProject {
        val name = projectName?.trim()?.takeIf { it.isNotEmpty() } ?: when (template) {
            ZeusTemplate.BIP_MAX_DIGITAL_PRO -> "bip-max-digital-pro"
            ZeusTemplate.BIP_MAX_FITNESS_TRACKER -> "bip-max-hiit-tracker"
            ZeusTemplate.BIP_MAX_WEATHER_WIDGET -> "bip-max-weather-widget"
            ZeusTemplate.BIP_MAX_BLE_CONTROLLER -> "bip-max-ble-remote"
            ZeusTemplate.BIP_MAX_MINIMAL_ANALOG -> "bip-max-swiss-analog"
        }

        val (appType, files) = when (template) {
            ZeusTemplate.BIP_MAX_DIGITAL_PRO -> "watchface" to getDigitalProFiles(name)
            ZeusTemplate.BIP_MAX_FITNESS_TRACKER -> "app" to getFitnessTrackerFiles(name)
            ZeusTemplate.BIP_MAX_WEATHER_WIDGET -> "widget" to getWeatherWidgetFiles(name)
            ZeusTemplate.BIP_MAX_BLE_CONTROLLER -> "app" to getBleRemoteFiles(name)
            ZeusTemplate.BIP_MAX_MINIMAL_ANALOG -> "watchface" to getMinimalAnalogFiles(name)
        }

        return ZeusProject(
            name = name,
            appType = appType,
            template = template,
            targetDevice = "bip_max",
            targetResolution = "432x514",
            zeppOsVersion = "5.0",
            files = files,
            activeFileId = files.firstOrNull { it.name.endsWith(".js") }?.id ?: files.firstOrNull()?.id
        )
    }

    private fun getDigitalProFiles(projectName: String): List<ZeusFile> = listOf(
        ZeusFile(
            name = "app.json",
            path = "app.json",
            fileType = FileType.JSON,
            content = """{
  "configVersion": "v3",
  "app": {
    "appType": "watchface",
    "appName": "Bip Max Digital Pro",
    "appId": 1008601,
    "version": {
      "code": 1,
      "name": "1.0.0"
    },
    "icon": "assets/icon.png",
    "vender": "zeus-developer",
    "description": "Cyberpunk Neon Watch Face for Amazfit Bip Max (432x514 AMOLED, 3000 nits)"
  },
  "permissions": [
    "device:os.sensor",
    "device:os.bg_service"
  ],
  "targets": {
    "bip_max": {
      "module": {
        "watchface": {
          "path": "watchface/index"
        }
      },
      "designWidth": 432,
      "designHeight": 514
    }
  }
}"""
        ),
        ZeusFile(
            name = "index.js",
            path = "watchface/index.js",
            fileType = FileType.JS,
            content = """/**
 * Zeus CLI Watch Face for Amazfit Bip Max (432x514 AMOLED)
 * Zepp OS 5.0 / 6.0 API
 */
import { createWidget, widget, prop, align, text_style } from '@zos/ui'
import { Time, HeartRate, Step, Battery } from '@zos/sensor'
import { log } from '@zos/utils'

const logger = log.getLogger('bip-max-digital-pro')

WatchFace({
  initView() {
    logger.log('Initializing Bip Max Watch Face on 432x514 AMOLED...')

    // 1. Cyber Dark Background
    createWidget(widget.FILL_RECT, {
      x: 0,
      y: 0,
      w: 432,
      h: 514,
      color: 0x090D16
    })

    // 2. Neon Header Bar & Day Display
    const dateText = createWidget(widget.TEXT, {
      x: 20,
      y: 32,
      w: 392,
      h: 36,
      color: 0x00E5FF,
      text_size: 22,
      align_h: align.CENTER_H,
      text: 'MON • OCT 24'
    })

    // 3. Digital Clock (432x514 Center Display)
    const timeWidget = createWidget(widget.TIME_TEXT, {
      x: 16,
      y: 84,
      w: 400,
      h: 120,
      color: 0xFFFFFF,
      text_size: 96,
      align_h: align.CENTER_H,
      font: 'fonts/orbitron_bold.ttf'
    })

    // Seconds Pulse Accent
    const secText = createWidget(widget.TEXT, {
      x: 320,
      y: 155,
      w: 80,
      h: 40,
      color: 0x38BDF8,
      text_size: 26,
      text: ':45'
    })

    // 4. Step Progress Arc (Left Ring)
    const stepArc = createWidget(widget.ARC, {
      x: 32,
      y: 240,
      w: 160,
      h: 160,
      start_angle: 0,
      end_angle: 240,
      color: 0x10B981,
      line_width: 10
    })

    const stepLabel = createWidget(widget.TEXT, {
      x: 32,
      y: 290,
      w: 160,
      h: 36,
      color: 0x10B981,
      text_size: 28,
      align_h: align.CENTER_H,
      text: '6,420'
    })

    const stepSub = createWidget(widget.TEXT, {
      x: 32,
      y: 330,
      w: 160,
      h: 24,
      color: 0x94A3B8,
      text_size: 16,
      align_h: align.CENTER_H,
      text: 'STEPS'
    })

    // 5. Heart Rate Monitor Arc (Right Ring)
    const hrArc = createWidget(widget.ARC, {
      x: 240,
      y: 240,
      w: 160,
      h: 160,
      start_angle: 0,
      end_angle: 180,
      color: 0xEF4444,
      line_width: 10
    })

    const hrLabel = createWidget(widget.TEXT, {
      x: 240,
      y: 290,
      w: 160,
      h: 36,
      color: 0xEF4444,
      text_size: 28,
      align_h: align.CENTER_H,
      text: '78'
    })

    const hrSub = createWidget(widget.TEXT, {
      x: 240,
      y: 330,
      w: 160,
      h: 24,
      color: 0x94A3B8,
      text_size: 16,
      align_h: align.CENTER_H,
      text: 'BPM'
    })

    // 6. Battery & Bluetooth Status Footer
    const battText = createWidget(widget.TEXT, {
      x: 24,
      y: 440,
      w: 180,
      h: 36,
      color: 0x22C55E,
      text_size: 20,
      align_h: align.LEFT,
      text: '⚡ 84% BATT'
    })

    const calText = createWidget(widget.TEXT, {
      x: 228,
      y: 440,
      w: 180,
      h: 36,
      color: 0xF97316,
      text_size: 20,
      align_h: align.RIGHT,
      text: '345 KCAL 🔥'
    })
  },

  onInit() {
    this.initView()
  },

  onDestroy() {
    logger.log('Watchface destroyed')
  }
})"""
        ),
        ZeusFile(
            name = "index.style.js",
            path = "watchface/index.style.js",
            fileType = FileType.STYLE_JS,
            content = """export const SCREEN_WIDTH = 432
export const SCREEN_HEIGHT = 514

export const COLORS = {
  bg: 0x090D16,
  primary: 0x00E5FF,
  accent: 0x38BDF8,
  stepGreen: 0x10B981,
  heartRed: 0xEF4444,
  batteryGreen: 0x22C55E,
  calorieOrange: 0xF97316,
  textMuted: 0x94A3B8
}

export const STYLES = {
  clockFont: {
    size: 96,
    color: 0xFFFFFF
  },
  gaugeWidth: 10,
  gaugeRadius: 80
}"""
        ),
        ZeusFile(
            name = "README.md",
            path = "README.md",
            fileType = FileType.MARKDOWN,
            content = """# Bip Max Digital Pro Watch Face
Target: **Amazfit Bip Max (432x514 AMOLED, 3,000 Nits)**
Zepp OS SDK: **v5.0 / v6.0**

### Specs:
- 2.07\" HD AMOLED (432x514, 302 PPI)
- 550 mAh Battery (20-day longevity)
- Bluetooth 5.3 BLE Wireless Gateway

### CLI Commands:
- `zeus dev` - Start local live preview
- `zeus build --target bip_max` - Compile .zab release package
- `zeus bridge --install` - Flash directly over BLE to Bip Max watch"""
        )
    )

    private fun getFitnessTrackerFiles(projectName: String): List<ZeusFile> = listOf(
        ZeusFile(
            name = "app.json",
            path = "app.json",
            fileType = FileType.JSON,
            content = """{
  "configVersion": "v3",
  "app": {
    "appType": "app",
    "appName": "Bip Max HIIT Tracker",
    "appId": 1008602,
    "version": {
      "code": 1,
      "name": "1.0.0"
    },
    "icon": "assets/icon.png",
    "vender": "zeus-fitness",
    "description": "HIIT & Cardio Interval Workout Tracker for Bip Max (432x514 AMOLED)"
  },
  "permissions": [
    "device:os.sensor",
    "device:os.vibrate"
  ],
  "targets": {
    "bip_max": {
      "module": {
        "page": {
          "pages": [
            "page/home/index",
            "page/workout/index"
          ]
        }
      },
      "designWidth": 432,
      "designHeight": 514
    }
  }
}"""
        ),
        ZeusFile(
            name = "index.js",
            path = "page/home/index.js",
            fileType = FileType.JS,
            content = """import { createWidget, widget, prop, align, event } from '@zos/ui'
import { push } from '@zos/router'
import { Vibrate } from '@zos/sensor'

Page({
  build() {
    // Background
    createWidget(widget.FILL_RECT, {
      x: 0, y: 0, w: 432, h: 514, color: 0x0F172A
    })

    // Header Title
    createWidget(widget.TEXT, {
      x: 20, y: 24, w: 392, h: 44,
      color: 0xF59E0B, text_size: 30,
      align_h: align.CENTER_H,
      text: '⚡ HIIT WORKOUT'
    })

    // Workout Mode Cards
    createWidget(widget.BUTTON, {
      x: 24, y: 90, w: 384, h: 95,
      radius: 18, normal_color: 0x1E293B, press_color: 0x334155,
      text: '🏃 Sprint Intervals (30s/15s)',
      text_size: 20, color: 0xFFFFFF,
      click_func: () => {
        push({ url: 'page/workout/index', params: { mode: 'sprint' } })
      }
    })

    createWidget(widget.BUTTON, {
      x: 24, y: 205, w: 384, h: 95,
      radius: 18, normal_color: 0x1E293B, press_color: 0x334155,
      text: '🥊 Tabata Rounds (20s/10s)',
      text_size: 20, color: 0xFFFFFF,
      click_func: () => {
        push({ url: 'page/workout/index', params: { mode: 'tabata' } })
      }
    })

    // Quick Start Big Button
    createWidget(widget.BUTTON, {
      x: 24, y: 340, w: 384, h: 100,
      radius: 24, normal_color: 0x10B981, press_color: 0x059669,
      text: '▶ START SESSION',
      text_size: 24, color: 0xFFFFFF,
      click_func: () => {
        push({ url: 'page/workout/index', params: { mode: 'freestyle' } })
      }
    })
  }
})"""
        ),
        ZeusFile(
            name = "workout.js",
            path = "page/workout/index.js",
            fileType = FileType.JS,
            content = """import { createWidget, widget, prop, align } from '@zos/ui'
import { HeartRate, Calorie, Time } from '@zos/sensor'

Page({
  build() {
    createWidget(widget.FILL_RECT, {
      x: 0, y: 0, w: 432, h: 514, color: 0x0A0F1D
    })

    // Timer Display
    createWidget(widget.TEXT, {
      x: 24, y: 36, w: 384, h: 75,
      color: 0x00E5FF, text_size: 60,
      align_h: align.CENTER_H, text: '20:40'
    })

    // Heart Rate Zone
    createWidget(widget.TEXT, {
      x: 24, y: 130, w: 384, h: 50,
      color: 0xEF4444, text_size: 40,
      align_h: align.CENTER_H, text: '♥ 152 BPM'
    })
    createWidget(widget.TEXT, {
      x: 24, y: 185, w: 384, h: 30,
      color: 0xF87171, text_size: 18,
      align_h: align.CENTER_H, text: 'AEROBIC / ZONE 4'
    })

    // Calories & Cadence
    createWidget(widget.TEXT, {
      x: 24, y: 250, w: 180, h: 50,
      color: 0xF97316, text_size: 26,
      align_h: align.CENTER_H, text: '340 kcal'
    })
    createWidget(widget.TEXT, {
      x: 228, y: 250, w: 180, h: 50,
      color: 0x38BDF8, text_size: 26,
      align_h: align.CENTER_H, text: '168 SPM'
    })

    // Pause / Stop Controls
    createWidget(widget.BUTTON, {
      x: 24, y: 370, w: 180, h: 80,
      radius: 20, normal_color: 0xF59E0B, press_color: 0xD97706,
      text: '⏸ PAUSE', text_size: 20, color: 0x000000
    })
    createWidget(widget.BUTTON, {
      x: 228, y: 370, w: 180, h: 80,
      radius: 20, normal_color: 0xEF4444, press_color: 0xDC2626,
      text: '⏹ FINISH', text_size: 20, color: 0xFFFFFF
    })
  }
})"""
        )
    )

    private fun getWeatherWidgetFiles(projectName: String): List<ZeusFile> = listOf(
        ZeusFile(
            name = "app.json",
            path = "app.json",
            fileType = FileType.JSON,
            content = """{
  "configVersion": "v3",
  "app": {
    "appType": "widget",
    "appName": "Bip Max Weather Barometer",
    "appId": 1008603,
    "version": {
      "code": 1,
      "name": "1.0.0"
    },
    "vender": "zeus-weather",
    "description": "Weather & Atmospheric Barometer for Amazfit Bip Max (432x514 AMOLED)"
  },
  "targets": {
    "bip_max": {
      "module": {
        "widget": {
          "path": "widget/index"
        }
      },
      "designWidth": 432,
      "designHeight": 514
    }
  }
}"""
        ),
        ZeusFile(
            name = "index.js",
            path = "widget/index.js",
            fileType = FileType.JS,
            content = """import { createWidget, widget, prop, align } from '@zos/ui'
import { Weather } from '@zos/sensor'

Widget({
  build() {
    createWidget(widget.FILL_RECT, {
      x: 0, y: 0, w: 432, h: 514, color: 0x0C1E3A
    })

    // City & Conditions
    createWidget(widget.TEXT, {
      x: 24, y: 28, w: 384, h: 36,
      color: 0x93C5FD, text_size: 22,
      align_h: align.CENTER_H, text: 'SAN FRANCISCO'
    })

    // Main Temp
    createWidget(widget.TEXT, {
      x: 24, y: 76, w: 384, h: 90,
      color: 0xFFFFFF, text_size: 78,
      align_h: align.CENTER_H, text: '24°C'
    })

    createWidget(widget.TEXT, {
      x: 24, y: 170, w: 384, h: 32,
      color: 0x38BDF8, text_size: 20,
      align_h: align.CENTER_H, text: '☀️ Sunny • H: 26° L: 17°'
    })

    // Metrics Row 1: Air Quality & Humidity
    createWidget(widget.TEXT, {
      x: 32, y: 240, w: 170, h: 50,
      color: 0x34D399, text_size: 20,
      text: 'AQI: 28 (Good)'
    })
    createWidget(widget.TEXT, {
      x: 228, y: 240, w: 170, h: 50,
      color: 0x60A5FA, text_size: 20,
      text: '💧 Humidity: 54%'
    })

    // Metrics Row 2: UV & Barometer
    createWidget(widget.TEXT, {
      x: 32, y: 310, w: 170, h: 50,
      color: 0xFBBF24, text_size: 20,
      text: 'UV Index: 6 (Mod)'
    })
    createWidget(widget.TEXT, {
      x: 228, y: 310, w: 170, h: 50,
      color: 0xA78BFA, text_size: 20,
      text: '🧭 1014 hPa'
    })

    // 5-Day Forecast mini bar
    createWidget(widget.TEXT, {
      x: 24, y: 400, w: 384, h: 40,
      color: 0xE2E8F0, text_size: 18,
      align_h: align.CENTER_H,
      text: 'Tue 24° | Wed 25° | Thu 22° | Fri 20°'
    })
  }
})"""
        )
    )

    private fun getBleRemoteFiles(projectName: String): List<ZeusFile> = listOf(
        ZeusFile(
            name = "app.json",
            path = "app.json",
            fileType = FileType.JSON,
            content = """{
  "configVersion": "v3",
  "app": {
    "appType": "app",
    "appName": "Bip Max BLE Remote",
    "appId": 1008604,
    "version": {
      "code": 1,
      "name": "1.0.0"
    },
    "vender": "zeus-ble",
    "description": "Bluetooth 5.3 Media & Camera Controller for Amazfit Bip Max (432x514)"
  },
  "permissions": [
    "device:os.ble",
    "device:os.vibrate"
  ],
  "targets": {
    "bip_max": {
      "module": {
        "page": {
          "pages": [
            "page/remote/index"
          ]
        }
      },
      "designWidth": 432,
      "designHeight": 514
    }
  }
}"""
        ),
        ZeusFile(
            name = "index.js",
            path = "page/remote/index.js",
            fileType = FileType.JS,
            content = """import { createWidget, widget, prop, align } from '@zos/ui'
import { BleMaster } from '@zos/ble'
import { log } from '@zos/utils'

const logger = log.getLogger('bip-max-remote')

Page({
  build() {
    createWidget(widget.FILL_RECT, {
      x: 0, y: 0, w: 432, h: 514, color: 0x111827
    })

    // Status Header
    createWidget(widget.TEXT, {
      x: 24, y: 26, w: 384, h: 36,
      color: 0x38BDF8, text_size: 20,
      align_h: align.CENTER_H,
      text: '● BLE CONNECTED: PHONE (BT 5.3)'
    })

    // Music Player Section
    createWidget(widget.TEXT, {
      x: 24, y: 80, w: 384, h: 32,
      color: 0xFFFFFF, text_size: 22,
      align_h: align.CENTER_H,
      text: 'Midnight City - M83'
    })

    // Prev / Play-Pause / Next Controls
    createWidget(widget.BUTTON, {
      x: 32, y: 135, w: 100, h: 80,
      radius: 20, normal_color: 0x1F2937, press_color: 0x374151,
      text: '⏮', text_size: 28, color: 0xFFFFFF
    })
    createWidget(widget.BUTTON, {
      x: 156, y: 130, w: 120, h: 90,
      radius: 24, normal_color: 0x00E5FF, press_color: 0x0891B2,
      text: '▶', text_size: 34, color: 0x000000
    })
    createWidget(widget.BUTTON, {
      x: 300, y: 135, w: 100, h: 80,
      radius: 20, normal_color: 0x1F2937, press_color: 0x374151,
      text: '⏭', text_size: 28, color: 0xFFFFFF
    })

    // Camera Shutter Trigger
    createWidget(widget.BUTTON, {
      x: 32, y: 260, w: 368, h: 85,
      radius: 22, normal_color: 0x8B5CF6, press_color: 0x7C3AED,
      text: '📷 Remote Camera Shutter',
      text_size: 20, color: 0xFFFFFF
    })

    // Find Phone Ring
    createWidget(widget.BUTTON, {
      x: 32, y: 375, w: 368, h: 80,
      radius: 22, normal_color: 0xEF4444, press_color: 0xDC2626,
      text: '🔔 Ring Lost Phone',
      text_size: 20, color: 0xFFFFFF
    })
  }
})"""
        )
    )

    private fun getMinimalAnalogFiles(projectName: String): List<ZeusFile> = listOf(
        ZeusFile(
            name = "app.json",
            path = "app.json",
            fileType = FileType.JSON,
            content = """{
  "configVersion": "v3",
  "app": {
    "appType": "watchface",
    "appName": "Bip Max Swiss Analog",
    "appId": 1008605,
    "version": {
      "code": 1,
      "name": "1.0.0"
    },
    "vender": "zeus-analog",
    "description": "Minimalist Luxury Analog Watch Face for Amazfit Bip Max (432x514 AMOLED)"
  },
  "targets": {
    "bip_max": {
      "module": {
        "watchface": {
          "path": "watchface/index"
        }
      },
      "designWidth": 432,
      "designHeight": 514
    }
  }
}"""
        ),
        ZeusFile(
            name = "index.js",
            path = "watchface/index.js",
            fileType = FileType.JS,
            content = """import { createWidget, widget, prop, align } from '@zos/ui'
import { Time } from '@zos/sensor'

WatchFace({
  initView() {
    // Dark Dial Background (432x514 AMOLED)
    createWidget(widget.FILL_RECT, {
      x: 0, y: 0, w: 432, h: 514, color: 0x050811
    })

    // Luxury Dial Ring
    createWidget(widget.ARC, {
      x: 26, y: 67, w: 380, h: 380,
      start_angle: 0, end_angle: 360,
      color: 0x334155, line_width: 3
    })

    // Hour Markers (12, 3, 6, 9)
    createWidget(widget.TEXT, {
      x: 186, y: 75, w: 60, h: 40,
      color: 0xF8FAFC, text_size: 30,
      align_h: align.CENTER_H, text: '12'
    })
    createWidget(widget.TEXT, {
      x: 350, y: 237, w: 40, h: 40,
      color: 0xF8FAFC, text_size: 30,
      align_h: align.CENTER_H, text: '3'
    })
    createWidget(widget.TEXT, {
      x: 186, y: 395, w: 60, h: 40,
      color: 0xF8FAFC, text_size: 30,
      align_h: align.CENTER_H, text: '6'
    })
    createWidget(widget.TEXT, {
      x: 36, y: 237, w: 40, h: 40,
      color: 0xF8FAFC, text_size: 30,
      align_h: align.CENTER_H, text: '9'
    })

    // Brand Label
    createWidget(widget.TEXT, {
      x: 76, y: 155, w: 280, h: 32,
      color: 0x94A3B8, text_size: 17,
      align_h: align.CENTER_H, text: 'ZEPP OS • BIP MAX'
    })

    // Date Window at 4 o'clock
    createWidget(widget.TEXT, {
      x: 270, y: 295, w: 65, h: 32,
      color: 0xF59E0B, text_size: 20,
      align_h: align.CENTER_H, text: '24'
    })

    // Analog Hands Pointers (Center: 216, 257)
    createWidget(widget.TIME_POINTER, {
      hour_centerX: 216,
      hour_centerY: 257,
      hour_posX: 216,
      hour_posY: 140,
      minute_centerX: 216,
      minute_centerY: 257,
      minute_posX: 216,
      minute_posY: 90,
      second_centerX: 216,
      second_centerY: 257,
      second_posX: 216,
      second_posY: 75,
      second_color: 0xEF4444
    })
  }
})"""
        )
    )
}
