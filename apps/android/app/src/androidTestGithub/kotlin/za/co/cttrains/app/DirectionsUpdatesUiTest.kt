package za.co.cttrains.app

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.apksig.ApkVerifier
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import za.co.cttrains.app.directions.*
import za.co.cttrains.app.updates.*
import za.co.cttrains.core.*
import java.io.File

@RunWith(AndroidJUnit4::class)
class DirectionsUpdatesUiTest {
    @get:Rule val app=createAndroidComposeRule<MainActivity>()
    private val point=GeoPoint(-33.949,18.5)
    private fun plan(): DirectionsPlan {
        val now=System.currentTimeMillis()
        return DirectionsPlan("device-directions","Work → Home",listOf(
            DirectionStep("board",DirectionKind.BOARD,"Board train 3500","Salt River · Platform not published",point,scheduledAt=now-60_000),
            DirectionStep("ride",DirectionKind.RIDE,"Get off at Unibell","Train 3500",point,scheduledAt=now+20*60_000),
            DirectionStep("walk",DirectionKind.WALK,"Walk to Home","Unibell → Home",point,listOf(point,GeoPoint(-33.948,18.5)),distanceMeters=120.0,durationSeconds=120.0)),now,now,"Scheduled train times; confirm boarding.")
    }
    @Before fun setup() {
        DirectionsService.stop(app.activity)
        val shell=InstrumentationRegistry.getInstrumentation().uiAutomation
        shell.grantRuntimePermission(app.activity.packageName,Manifest.permission.ACCESS_COARSE_LOCATION)
        shell.grantRuntimePermission(app.activity.packageName,Manifest.permission.ACCESS_FINE_LOCATION)
        if(Build.VERSION.SDK_INT>=33) shell.grantRuntimePermission(app.activity.packageName,Manifest.permission.POST_NOTIFICATIONS)
    }
    @After fun cleanup() { DirectionsService.stop(app.activity);UpdateWork.reset(app.activity) }
    @Test fun updatesAndNowBarGuidesAreAccessibleWithoutAnyAccountOrDownload() {
        app.runOnUiThread { ViewModelProvider(app.activity)[TrainViewModel::class.java].openAppUpdates() }
        app.onNodeWithTag("app-updates-screen").assertIsDisplayed()
        app.onNodeWithText("Current · ${BuildConfig.VERSION_NAME}").assertIsDisplayed()
        app.onNodeWithTag("app-updates-screen").performScrollToNode(hasText("How do updates work?"))
        app.onNodeWithText("How do updates work?").performClick()
        app.onNodeWithTag("app-updates-screen").performScrollToNode(hasText("This is app-level staging",substring=true))
        app.onNodeWithText("This is app-level staging",substring=true).assertIsDisplayed()
        UiScreenshots.save("github-update-guide")
        app.runOnUiThread { DirectionsRuntime.state.value=DirectionsState(preview=plan());ViewModelProvider(app.activity)[TrainViewModel::class.java].openDirections() }
        app.onNodeWithContentDescription("Journey options").performClick()
        app.onNodeWithText("Notification settings").performClick()
        app.onNodeWithText("Notification settings").assertIsDisplayed()
        UiScreenshots.save("live-update-now-bar-setup")
    }
    @Test fun apkVerifierRejectsCorruptedPayloadAndUpdateCredentialsAreEncrypted() {
        val c=app.activity
        val installed=File(c.applicationInfo.sourceDir)
        fun verified(file: File)=runCatching { ApkVerifier.Builder(file).setMinCheckedPlatformVersion(Build.VERSION.SDK_INT).setMaxCheckedPlatformVersion(Build.VERSION.SDK_INT).build().verify().isVerified }.getOrDefault(false)
        assertTrue(verified(installed))
        val corrupt=File(c.cacheDir,"tampered-test.apk")
        try {
            installed.copyTo(corrupt,true)
            java.io.RandomAccessFile(corrupt,"rw").use { f->f.seek(128);val value=f.readByte();f.seek(128);f.writeByte(value.toInt() xor 1) }
            assertFalse(verified(corrupt))
        } finally { corrupt.delete() }
        val fixture="test_read_only_token_not_a_real_credential"
        val store=UpdateStore(c);store.save(UpdateSettings(token=fixture))
        assertEquals(fixture,store.settings().token)
        assertFalse(c.getSharedPreferences("app-updates-private",0).getString("settings","")!!.contains(fixture))
        assertEquals(64,UpdateRepository(c).certificate().length)
    }
}
