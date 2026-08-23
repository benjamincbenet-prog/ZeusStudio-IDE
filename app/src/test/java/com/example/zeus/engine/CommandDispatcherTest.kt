package com.example.zeus.engine

import com.example.zeus.data.ZeusTemplatesRepository
import com.example.zeus.model.ZabPackage
import com.example.zeus.model.ZeusTemplate
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Priority 4: terminal command dispatcher, routing, parsing, and failure truthfulness.
 */
class CommandDispatcherTest {

    private val baseProject = ZeusTemplatesRepository.createProject(ZeusTemplate.BIP_MAX_DIGITAL_PRO, "test-app")

    private val noBluetoothContext = DispatcherContext(
        project = baseProject,
        isBleConnected = false,
        connectedDeviceMac = null,
        connectedDeviceName = null,
        isBluetoothEnabled = false,
        isBluetoothAvailable = false,
        hasBlePermissions = false,
        lastBuildArtifact = null
    )

    private val bleReadyContext = DispatcherContext(
        project = baseProject,
        isBleConnected = true,
        connectedDeviceMac = "D4:22:CD:88:F1:04",
        connectedDeviceName = "Amazfit Bip Max",
        isBluetoothEnabled = true,
        isBluetoothAvailable = true,
        hasBlePermissions = true,
        lastBuildArtifact = ZabPackage(
            packageName = "test-app-debug.zab",
            version = "1.0.0",
            fileSizeKb = 42.0,
            checksumCrc32 = "AABBCCDD",
            fileCount = 5,
            appType = "watchface"
        )
    )

    // ── Command parsing ────────────────────────────────────────────────────────

    @Test
    fun `parseCommandLine splits simple tokens`() {
        val tokens = parseCommandLine("zeus build --release")
        assertEquals(listOf("zeus", "build", "--release"), tokens)
    }

    @Test
    fun `parseCommandLine handles quoted strings`() {
        val tokens = parseCommandLine("zeus create \"my cool app\"")
        assertEquals(listOf("zeus", "create", "my cool app"), tokens)
    }

    @Test
    fun `parseCommandLine handles single-quote strings`() {
        val tokens = parseCommandLine("zeus create 'my-app'")
        assertEquals(listOf("zeus", "create", "my-app"), tokens)
    }

    @Test
    fun `parseCommandLine returns empty for blank input`() {
        assertTrue(parseCommandLine("   ").isEmpty())
    }

    @Test
    fun `parseCommandLine handles cmd without zeus prefix`() {
        val tokens = parseCommandLine("build --release")
        assertEquals(listOf("build", "--release"), tokens)
    }

    // ── Dispatcher routing ────────────────────────────────────────────────────

    @Test
    fun `dispatch routes build to BuildCommandHandler`() {
        val result = CommandDispatcher.dispatch("zeus build", noBluetoothContext)
        // Build must fail (no toolchain), but events must be present
        assertFalse("build with no toolchain must not succeed", result.exitCode == 0)
        assertTrue(result.events.any { it is CommandEvent.Started })
        assertTrue(result.events.any { it is CommandEvent.Completed })
        assertEquals(ErrorCategory.BACKEND_NOT_CONFIGURED, result.errorCategory)
    }

    @Test
    fun `dispatch routes bridge --scan to BridgeCommandHandler`() {
        val result = CommandDispatcher.dispatch("zeus bridge --scan", noBluetoothContext)
        val started = result.events.filterIsInstance<CommandEvent.Started>()
        assertEquals(1, started.size)
        assertEquals("bridge --scan", started.first().command)
    }

    @Test
    fun `dispatch routes bridge --install to BridgeCommandHandler`() {
        val result = CommandDispatcher.dispatch("zeus bridge --install", noBluetoothContext)
        val started = result.events.filterIsInstance<CommandEvent.Started>()
        assertEquals(1, started.size)
    }

    @Test
    fun `dispatch routes doctor to DoctorCommandHandler`() {
        val result = CommandDispatcher.dispatch("zeus doctor", noBluetoothContext)
        assertTrue(result.events.any { it is CommandEvent.Started })
        assertTrue(result.events.any { it is CommandEvent.Completed })
    }

    @Test
    fun `dispatch returns non-zero exit for unknown command`() {
        val result = CommandDispatcher.dispatch("zeus frobnicate", noBluetoothContext)
        assertEquals(1, result.exitCode)
        assertEquals(ErrorCategory.INVALID_ARGS, result.errorCategory)
    }

    // ── Event sequencing ──────────────────────────────────────────────────────

    @Test
    fun `event sequence is started then stream then completed`() {
        val result = CommandDispatcher.dispatch("zeus doctor", noBluetoothContext)
        val events = result.events
        assertTrue(events.isNotEmpty())
        assertTrue(events.first() is CommandEvent.Started)
        assertTrue(events.last() is CommandEvent.Completed)
        // All events between first and last must be Stream
        events.drop(1).dropLast(1).forEach { event ->
            assertTrue("Expected Stream event but got $event", event is CommandEvent.Stream)
        }
    }

    // ── Failure truthfulness ──────────────────────────────────────────────────

    @Test
    fun `build without toolchain must fail`() {
        val result = CommandDispatcher.dispatch("zeus build", noBluetoothContext)
        assertNotEquals("build must fail when toolchain not configured", 0, result.exitCode)
        assertEquals(ErrorCategory.BACKEND_NOT_CONFIGURED, result.errorCategory)
    }

    @Test
    fun `bridge --install without artifact must fail`() {
        val result = CommandDispatcher.dispatch("zeus bridge --install", noBluetoothContext)
        assertNotEquals("install must fail when no artifact", 0, result.exitCode)
        assertEquals(ErrorCategory.BACKEND_NOT_CONFIGURED, result.errorCategory)
    }

    @Test
    fun `bridge --install without connected device must fail`() {
        // Has an artifact but no connected device
        val ctx = noBluetoothContext.copy(
            lastBuildArtifact = bleReadyContext.lastBuildArtifact
        )
        val result = CommandDispatcher.dispatch("zeus bridge --install", ctx)
        assertNotEquals("install must fail when no device connected", 0, result.exitCode)
        assertEquals(ErrorCategory.DEVICE_NOT_FOUND, result.errorCategory)
    }

    @Test
    fun `bridge --install with artifact and device still fails with BACKEND_NOT_CONFIGURED`() {
        // Even with artifact + device connected, OTA transport is not implemented
        val result = CommandDispatcher.dispatch("zeus bridge --install", bleReadyContext)
        assertNotEquals("OTA not implemented — must fail", 0, result.exitCode)
        assertEquals(ErrorCategory.BACKEND_NOT_CONFIGURED, result.errorCategory)
    }

    @Test
    fun `bridge --scan without Bluetooth availability fails`() {
        val result = CommandDispatcher.dispatch("zeus bridge --scan", noBluetoothContext)
        assertNotEquals(0, result.exitCode)
        assertEquals(ErrorCategory.DEPENDENCY_MISSING, result.errorCategory)
    }

    @Test
    fun `bridge --scan without permissions fails`() {
        val ctx = noBluetoothContext.copy(isBluetoothAvailable = true, isBluetoothEnabled = true, hasBlePermissions = false)
        val result = CommandDispatcher.dispatch("zeus bridge --scan", ctx)
        assertNotEquals(0, result.exitCode)
        assertEquals(ErrorCategory.PERMISSION_DENIED, result.errorCategory)
    }

    @Test
    fun `bridge --scan with permissions and connected device reports found`() {
        val result = CommandDispatcher.dispatch("zeus bridge --scan", bleReadyContext)
        assertEquals(0, result.exitCode)
        assertNull(result.errorCategory)
        assertTrue(result.summary.contains("1 device"))
    }

    @Test
    fun `doctor reports failures when Bluetooth not available`() {
        val result = CommandDispatcher.dispatch("zeus doctor", noBluetoothContext)
        // Doctor exits 1 because build toolchain + BT are both failing
        assertNotEquals(0, result.exitCode)
        assertEquals(ErrorCategory.DEPENDENCY_MISSING, result.errorCategory)
    }

    @Test
    fun `ZeusCliRunner legacy overload routes build through dispatcher`() {
        val result = ZeusCliRunner.execute(
            commandLine = "zeus build",
            currentProject = baseProject,
            isDevRunning = false,
            isBleConnected = false
        )
        assertTrue(result is CliCommandResult.Output)
        val output = result as CliCommandResult.Output
        // Must contain a failure message (toolchain not configured)
        assertTrue(output.logs.any { it.level == com.example.zeus.model.ZeusLogEntry.LogLevel.ERROR })
    }
}
