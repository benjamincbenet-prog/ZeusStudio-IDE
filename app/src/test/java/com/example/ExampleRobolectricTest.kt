package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.zeus.data.ZeusTemplatesRepository
import com.example.zeus.engine.CliCommandResult
import com.example.zeus.engine.ZeusCliRunner
import com.example.zeus.engine.ZeusCompiler
import com.example.zeus.model.ZeusTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun readStringFromContext() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Zeus IDE", appName)
  }

  @Test
  fun testZeusCompilerForBipMax() {
    val project = ZeusTemplatesRepository.createProject(ZeusTemplate.BIP_MAX_DIGITAL_PRO)
    val result = ZeusCompiler.compile(project)

    assertTrue("Compilation should succeed for Bip Max template", result.isSuccess)
    assertNotNull(result.zabPackage)
    assertEquals("432x514", result.zabPackage?.resolution)
  }

  @Test
  fun testZeusCliRunnerCommands() {
    val project = ZeusTemplatesRepository.createProject(ZeusTemplate.BIP_MAX_DIGITAL_PRO)
    val helpResult = ZeusCliRunner.execute("zeus help", project, isDevRunning = false, isBleConnected = false)

    assertTrue(helpResult is CliCommandResult.Output)
    val logs = (helpResult as CliCommandResult.Output).logs
    assertTrue(logs.any { it.message.contains("Zeus CLI") })

    val signResult = ZeusCliRunner.execute("zeus sign", project, isDevRunning = false, isBleConnected = false)
    assertTrue(signResult is CliCommandResult.Output)
    val signLogs = (signResult as CliCommandResult.Output).logs
    assertTrue(signLogs.any { it.message.contains("signed successfully") })
  }
}
