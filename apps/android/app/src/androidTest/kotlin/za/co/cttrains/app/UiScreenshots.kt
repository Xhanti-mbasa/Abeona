package za.co.cttrains.app

import android.os.ParcelFileDescriptor
import androidx.test.platform.app.InstrumentationRegistry

/** UTP may uninstall the app after tests. Shell-owned screenshots survive that cleanup. */
internal object UiScreenshots {
    fun save(name: String) {
        require(Regex("[a-z0-9-]+",RegexOption.IGNORE_CASE).matches(name))
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        val automation=instrumentation.uiAutomation
        runCatching { automation.waitForIdle(200,3000) }
        android.os.SystemClock.sleep(200)
        fun command(text: String) { ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(text)).use { it.readBytes() } }
        command("mkdir -p /data/local/tmp/cttrains-ui-screenshots")
        command("screencap -p /data/local/tmp/cttrains-ui-screenshots/$name.png")
    }
}
