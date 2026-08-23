package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bip_devices")
data class BipDevice(
    @PrimaryKey
    val macAddress: String,
    val name: String = "Amazfit Bip Max",
    val model: String = "Bip Max (A2170)",
    val firmwareVersion: String = "v1.4.2.18",
    val hardwareRevision: String = "V2.1 - Nordic nRF52840",
    val batteryPercent: Int = 84,
    val isCharging: Boolean = false,
    val rssi: Int = -62,
    val isBonded: Boolean = true,
    val isConnected: Boolean = true,
    val authKeyHex: String = "30743632303437346162386438343831",
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val isSimulated: Boolean = false
)

enum class ConnectionState {
    DISCONNECTED,
    BLUETOOTH_DISABLED,
    BLUETOOTH_UNAVAILABLE,
    SCANNING,
    CONNECTING,
    AUTHENTICATING,
    CONNECTED,
    SYNCING
}
