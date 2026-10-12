package za.co.cttrains.app

import android.content.pm.PackageManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayDistributionUiTest {
    @get:Rule val app=createAndroidComposeRule<MainActivity>()

    @Test fun playAppCannotInstallExternalUpdatesOrRequestBackgroundLocation() {
        assertFalse(BuildConfig.GITHUB_DISTRIBUTION)
        val permissions=app.activity.packageManager.getPackageInfo(app.activity.packageName,PackageManager.GET_PERMISSIONS).requestedPermissions.orEmpty().toSet()
        assertFalse("android.permission.REQUEST_INSTALL_PACKAGES" in permissions)
        assertFalse("android.permission.ACCESS_BACKGROUND_LOCATION" in permissions)
        for(name in listOf("za.co.cttrains.app.updates.UpdateRepository","za.co.cttrains.app.updates.UpdateDownloadWorker")) {
            assertTrue("External installer class must be absent",runCatching { Class.forName(name) }.isFailure)
        }
    }

    @Test fun playUpdatesExplainUnavailableSideloadedInstallation() {
        app.runOnUiThread { ViewModelProvider(app.activity)[TrainViewModel::class.java].openAppUpdates() }
        app.waitUntil(15000) { app.onAllNodesWithTag("play-updates-screen").fetchSemanticsNodes().isNotEmpty() }
        app.onNodeWithText("App updates").assertIsDisplayed()
        app.waitUntil(30000) { app.onAllNodesWithText("Check for updates").fetchSemanticsNodes().any { it.config.contains(androidx.compose.ui.semantics.SemanticsProperties.Disabled).not() } }
        app.onNodeWithText("Check for updates").assertIsEnabled()
        app.onNodeWithText("GitHub repository").assertDoesNotExist()
        UiScreenshots.save("play-update-fallback")
    }
}
