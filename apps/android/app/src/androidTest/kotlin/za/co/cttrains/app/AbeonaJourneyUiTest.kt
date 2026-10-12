package za.co.cttrains.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
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
        app.waitUntil(15000) { train.state.value.preferences.theme=="Dark" }
    }
    @After fun cleanup() { DirectionsService.stop(app.activity);runBlocking { features.store.update { it.copy(spots=it.spots.filterNot { p->p.id.startsWith("design-") }) } } }
    @Test fun homeHasOneDestinationSearchAndNativeNavigation() {
        app.onNodeWithTag("home-destination").assertIsDisplayed()
        app.onNodeWithText("Where to?").assertIsDisplayed();app.onNodeWithTag("nav:plan").assertIsSelected()
        app.onNodeWithText("Find trains").assertDoesNotExist();UiScreenshots.save("abeona-home-dark")
        app.onNodeWithContentDescription("Settings").performClick();app.onNodeWithText("Your app, your data").assertIsDisplayed()
        app.runOnUiThread { app.activity.onBackPressedDispatcher.onBackPressed() }
        app.onNodeWithTag("home-destination").assertIsDisplayed()
        app.runOnUiThread { train.setTheme("Light") };app.waitUntil(15000) { train.state.value.preferences.theme=="Light" };app.waitForIdle();UiScreenshots.save("abeona-home-light")
    }
    @Test fun freshLiveTrainSearchCanStartGuidanceWithoutAnyBackendOrSeededCache() {
        assertTrue("This acceptance test must exercise the shipped unconfigured build",BuildConfig.TRANSIT_BASE_URL.isBlank())
        val catalog=runBlocking { train.repository.refreshCatalog("cttrains-web") }
        var day=java.time.LocalDate.now(CapeTownZone).plusDays(1)
        while(day.dayOfWeek.value>5 || day.toString() in catalog.holidays) day=day.plusDays(1)
        val query=SearchQuery("Unibell","Salt River",day.toString(),"07:00")
        runBlocking { train.repository.clearCache();train.repository.updatePreferences { it.copy(sourceId="cttrains-web") } }
        assertNull(runBlocking { train.repository.store.cached(query,"cttrains-web") })
        app.runOnUiThread { features.place(null,true);features.place(null,false);train.edit { it.copy(from="",to="",date=query.date,time=query.time,catalog=catalog) } }
        app.onNodeWithTag("home-destination").performClick();app.onNodeWithText("Search places, stations or stops").performTextInput("Salt River")
        app.onNodeWithTag("place-choice:station:saltriver").performScrollTo().performClick()
        app.onNodeWithText("Search places, stations or stops").performTextInput("Unibell")
        app.onNodeWithTag("place-choice:station:unibell").performScrollTo().performClick()
        app.waitUntil(120000) { app.onAllNodesWithTag("start-selected-journey").fetchSemanticsNodes().isNotEmpty() }
        val fresh=runBlocking { train.repository.store.cached(query,"cttrains-web") }
        assertNotNull("The UI must have fetched a new timetable",fresh)
        assertTrue(fresh!!.journeys.isNotEmpty());assertTrue(fresh.provenance!!.sourceAvailable);assertFalse(fresh.provenance!!.cached)
        assertEquals("scheduled",fresh.provenance!!.kind)
        android.util.Log.i("AbeonaLiveAcceptance","fresh-source=${fresh.sourceId}; query=$query; options=${fresh.journeys.size}; fetchedAt=${fresh.fetchedAt}; backend=absent; seededCache=false")
        app.onNodeWithText("Your routes").assertIsDisplayed();app.onNodeWithTag("start-selected-journey").assertIsDisplayed();UiScreenshots.save("abeona-live-results-dark")
        app.onNodeWithTag("start-selected-journey").performClick()
        app.waitUntil(15000) { app.onAllNodesWithTag("directions-screen").fetchSemanticsNodes().isNotEmpty() }
        app.onNodeWithTag("directions-screen").assertIsDisplayed();app.onNodeWithTag("nav:plan").assertDoesNotExist()
        app.mainClock.advanceTimeBy(300);app.waitForIdle();UiScreenshots.save("abeona-guidance-preview-dark")
        val shell=InstrumentationRegistry.getInstrumentation().uiAutomation
        shell.grantRuntimePermission(app.activity.packageName,android.Manifest.permission.ACCESS_COARSE_LOCATION)
        shell.grantRuntimePermission(app.activity.packageName,android.Manifest.permission.ACCESS_FINE_LOCATION)
        if(android.os.Build.VERSION.SDK_INT>=33) shell.grantRuntimePermission(app.activity.packageName,android.Manifest.permission.POST_NOTIFICATIONS)
        app.onNodeWithTag("begin-directions").performClick()
        app.waitUntil(15000) { DirectionsRuntime.state.value.running }
        app.onNodeWithTag("directions-next").assertIsDisplayed()
        app.mainClock.advanceTimeBy(300);app.waitForIdle();UiScreenshots.save("abeona-active-dark")
    }
    @Test fun customPlaceConfirmationAndRestartPreserveIdentity() {
        app.onNodeWithTag("nav:saved").performClick()
        app.waitUntil(15000) { app.onAllNodesWithTag("saved-addresses").fetchSemanticsNodes().isNotEmpty() }
        app.onNodeWithTag("saved-addresses").performScrollToNode(hasTestTag("add-custom-place"))
        app.onNodeWithTag("add-custom-place").performClick()
        app.onNodeWithText("Search places, stations or stops").performTextInput("Salt River");app.onNodeWithTag("place-choice:station:saltriver").performScrollTo().performClick()
        app.onNodeWithText("Place name").performTextInput("Home")
        app.onNodeWithText("Save address").assertIsNotEnabled()
        app.onNodeWithText("Place name").performTextReplacement("Evening class");app.onNodeWithText("Save address").performClick()
        app.waitUntil(15000) { features.state.value.extras.spots.any { it.name=="Evening class" } }
        val saved=features.state.value.extras.spots.first { it.name=="Evening class" }
        app.activityRule.scenario.recreate()
        runBlocking { val reopened=FeatureStore(app.activity).extras().spots.first { it.name=="Evening class" };assertEquals(saved.id,reopened.id);assertEquals(saved.point,reopened.point) }
        runBlocking { features.store.update { it.copy(spots=it.spots.filterNot { p->p.id==saved.id }) } }
    }
    @Test fun chosenTrainRoutePreservesQueryAndRestoresStationsAfterRestart() {
        val html=InstrumentationRegistry.getInstrumentation().context.assets.open("transfer.html").bufferedReader().use { it.readText() }
        val query=SearchQuery("Unibell","Salt River",java.time.LocalDate.now(CapeTownZone).plusDays(1).toString(),"07:00")
        val journey=CtTrainsParser.journeys(html,"07:00").first()
        app.runOnUiThread { train.presentPlannedJourney(query,journey,System.currentTimeMillis(),true,null,"cttrains-web",false) }
        app.waitUntil(15000) { train.state.value.preferences.lastQuery==query }
        assertEquals(query,train.state.value.query)
        app.activityRule.scenario.recreate()
        val reopened=ViewModelProvider(app.activity)[TrainViewModel::class.java]
        app.waitUntil(15000) { reopened.state.value.from==query.from && reopened.state.value.to==query.to && reopened.state.value.time==query.time }
        assertEquals(java.time.LocalDate.now(CapeTownZone).toString(),reopened.state.value.date)
        assertEquals(query,runBlocking { TrainRepository(app.activity).store.preferences.first().lastQuery })
    }
    @Test fun brokenBuildSourcePreferenceRecoversWithoutLosingStationsOrTheme() {
        runBlocking { train.repository.updatePreferences { it.copy(sourceId="cttrains-api",theme="Dark",favoriteStations=listOf("Unibell")) } }
        app.waitUntil(15000) { train.state.value.preferences.run { sourceId=="cttrains-web" && theme=="Dark" && favoriteStations==listOf("Unibell") } }
        app.runOnUiThread { train.setTheme("Light") }
        app.waitUntil(15000) { train.state.value.preferences.theme=="Light" }
        app.activityRule.scenario.recreate()
        runBlocking { val reopened=TrainRepository(app.activity).store.preferences.first();assertEquals("cttrains-web",reopened.sourceId);assertEquals("Light",reopened.theme);assertEquals(listOf("Unibell"),reopened.favoriteStations) }
    }
}
