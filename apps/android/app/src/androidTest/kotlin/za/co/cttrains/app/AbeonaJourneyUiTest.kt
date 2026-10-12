package za.co.cttrains.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import za.co.cttrains.app.data.*
import za.co.cttrains.app.directions.*
import za.co.cttrains.core.*

@RunWith(AndroidJUnit4::class)
class AbeonaJourneyUiTest {
    @get:Rule val app=createAndroidComposeRule<MainActivity>()
    private lateinit var train: TrainViewModel
    private lateinit var features: FeatureViewModel
    @Before fun setup() {
        train=ViewModelProvider(app.activity)[TrainViewModel::class.java];features=ViewModelProvider(app.activity)[FeatureViewModel::class.java]
        app.waitUntil(15000) { features.state.value.ready && train.state.value.stations.isNotEmpty() }
        app.runOnUiThread { train.setMapMode("stations");train.setTheme("Dark");features.place(null,true);features.place(null,false);train.edit { it.copy(from="Unibell",to="Salt River",time="07:00") } }
    }
    @After fun cleanup() { DirectionsService.stop(app.activity);runBlocking { features.store.update { it.copy(spots=it.spots.filterNot { p->p.id.startsWith("design-") }) } } }
    @Test fun homeHasOneDestinationSearchAndNativeNavigation() {
        app.onNodeWithTag("home-destination").assertIsDisplayed()
        app.onNodeWithText("Where to?").assertIsDisplayed();app.onNodeWithTag("nav:plan").assertIsSelected()
        app.onNodeWithText("Find trains").assertDoesNotExist();UiScreenshots.save("abeona-home-dark")
        app.onNodeWithContentDescription("Settings").performClick();app.onNodeWithText("Your app, your data").assertIsDisplayed()
        app.runOnUiThread { app.activity.onBackPressedDispatcher.onBackPressed() }
        app.onNodeWithTag("home-destination").assertIsDisplayed()
        app.runOnUiThread { train.setTheme("Light") };app.waitForIdle();UiScreenshots.save("abeona-home-light")
    }
    @Test fun cachedRouteCanBeChosenAndGuidancePreviewOpensOffline() {
        val html=InstrumentationRegistry.getInstrumentation().context.assets.open("transfer.html").bufferedReader().use { it.readText() }
        val query=SearchQuery("Unibell","Salt River",train.state.value.date,"07:00")
        runBlocking { train.repository.store.cache(CachedSearch(query,CtTrainsParser.journeys(html,"07:00"),System.currentTimeMillis()-60000,sourceId="cttrains-web")) }
        app.onNodeWithTag("home-destination").performClick();app.onNodeWithText("Search places, stations or stops").performTextInput("Salt River")
        app.onNodeWithTag("place-choice:station:saltriver").performScrollTo().performClick()
        app.waitUntil(20000) { app.onAllNodesWithTag("start-selected-journey").fetchSemanticsNodes().isNotEmpty() }
        app.onNodeWithText("Your routes").assertIsDisplayed();app.onNodeWithTag("start-selected-journey").assertIsDisplayed();UiScreenshots.save("abeona-results-dark")
        app.onNodeWithTag("start-selected-journey").performClick();app.onNodeWithTag("directions-screen").assertIsDisplayed();app.onNodeWithTag("nav:plan").assertDoesNotExist();UiScreenshots.save("abeona-guidance-preview-dark")
    }
    @Test fun customPlaceConfirmationAndRestartPreserveIdentity() {
        app.onNodeWithTag("nav:saved").performClick();app.onNodeWithTag("add-custom-place").performScrollTo().performClick()
        app.onNodeWithText("Search places, stations or stops").performTextInput("Salt River");app.onNodeWithTag("place-choice:station:saltriver").performScrollTo().performClick()
        app.onNodeWithText("Place name").performTextInput("Evening class");app.onNodeWithText("Save address").performClick()
        app.waitUntil(15000) { features.state.value.extras.spots.any { it.name=="Evening class" } }
        val saved=features.state.value.extras.spots.first { it.name=="Evening class" }
        app.activityRule.scenario.recreate()
        runBlocking { val reopened=FeatureStore(app.activity).extras().spots.first { it.name=="Evening class" };assertEquals(saved.id,reopened.id);assertEquals(saved.point,reopened.point) }
        runBlocking { features.store.update { it.copy(spots=it.spots.filterNot { p->p.id==saved.id }) } }
    }
}
