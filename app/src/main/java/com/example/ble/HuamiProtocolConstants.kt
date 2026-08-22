package com.example.ble

import java.util.UUID

object HuamiProtocolConstants {
    // Standard Huami / Amazfit GATT Services
    val UUID_SERVICE_HUAMI_MAIN: UUID = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
    val UUID_SERVICE_HUAMI_AUTH: UUID = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb")
    val UUID_SERVICE_HEART_RATE: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    val UUID_SERVICE_BATTERY: UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
    val UUID_SERVICE_IMMEDIATE_ALERT: UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb")

    // Characteristics
    val UUID_CHAR_AUTH: UUID = UUID.fromString("00000009-0000-3512-2118-0009af100700")
    val UUID_CHAR_REALTIME_STEPS: UUID = UUID.fromString("00000007-0000-3512-2118-0009af100700")
    val UUID_CHAR_ACTIVITY_DATA: UUID = UUID.fromString("00000005-0000-3512-2118-0009af100700")
    val UUID_CHAR_BATTERY_LEVEL: UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
    val UUID_CHAR_HEART_RATE_MEASURE: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
    val UUID_CHAR_HEART_RATE_CONTROL: UUID = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb")
    val UUID_CHAR_ALERT_LEVEL: UUID = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb")
    val UUID_CHAR_CHUNK_TRANSFER: UUID = UUID.fromString("00000020-0000-3512-2118-0009af100700")
    val UUID_CHAR_TIME_SYNC: UUID = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")

    // Protocol Commands
    val CMD_ALERT_HIGH_VIBRATION = byteArrayOf(0x02)
    val CMD_ALERT_MILD_VIBRATION = byteArrayOf(0x01)
    val CMD_ALERT_STOP = byteArrayOf(0x00)

    val CMD_START_HEART_RATE_CONTINUOUS = byteArrayOf(0x15, 0x01, 0x01)
    val CMD_STOP_HEART_RATE_CONTINUOUS = byteArrayOf(0x15, 0x01, 0x00)
    val CMD_FETCH_ACTIVITY_DATA = byteArrayOf(0x01, 0x01)
    val CMD_REBOOT_DEVICE = byteArrayOf(0x07)

    // Supported Amazfit Watch Identifiers
    val SUPPORTED_WATCH_PREFIXES = listOf(
        "Amazfit Bip Max",
        "Bip Max",
        "Amazfit Bip",
        "Amazfit Bip U",
        "Amazfit Bip S",
        "Amazfit Bip 3",
        "Amazfit Bip 5",
        "Huami",
        "Zepp"
    )
}
