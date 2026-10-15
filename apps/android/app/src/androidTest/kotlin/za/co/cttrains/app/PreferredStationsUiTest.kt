package za.co.cttrains.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import za.co.cttrains.core.*

@RunWith(AndroidJUnit4::class)
class PreferredStationsUiTest {
    @get:Rule val app=createAndroidComposeRule<MainActivity>()
    private val work=MarkedSpot("preferred-test-work","Work",null,GeoPoint(-33.927,18.451),"Work","33 Albert Road",createdAt=1)
    private lateinit var home: MarkedSpot
    private lateinit var train: TrainViewModel
    private lateinit var features: FeatureViewModel
    @Before fun setup() {
        train=ViewModelProvider(app.activity)[TrainViewModel::class.java]
        features=ViewModelProvider(app.activity)[FeatureViewModel::class.java]
        app.waitUntil(15000) { features.state.value.ready && train.state.value.stations.any { it.name=="Unibell" && it.mapped } }
        val station=train.state.value.stations.first { it.name=="Unibell" }
        home=MarkedSpot("preferred-test-home","Home",null,GeoPoint(station.latitude!!+.001,station.longitude!!),"Home",createdAt=1,preferredStation="Unibell")
        runBlocking { features.store.update { it.copy(spots=listOf(work,home)) } }
        app.waitUntil(15000) { features.state.value.extras.spots.size==2 }
        app.runOnUiThread { train.setMapMode("stations");train.edit { it.copy(from="",to="Unibell") };features.place(null,true);features.place(null,false) }
        app.onNodeWithTag("nav:plan").performClick()
    }
    @After fun cleanup() {
        app.runOnUiThread { features.place(null,true);features.place(null,false) }
        runBlocking { features.store.update { it.copy(spots=it.spots.filterNot { p -> p.id.startsWith("preferred-test-") }) } }
    }
    @Test fun aPreferredStationIsOptionalAndSurvivesSwap() {
        app.runOnUiThread { features.place(WalkEndpoint("spot:${work.id}",work.name,work.point),true) }
        app.onNodeWithTag("journey-planner").performScrollToNode(hasTestTag("home-boarding-station"));app.onNodeWithTag("home-boarding-station").performClick()
        val ranked=StationChoices.ranked(work.point,train.state.value.stations,train.state.value.catalog.stations)
        app.onNodeWithTag("station-choice:${stationKey(ranked.first().name)}").assertIsDisplayed();UiScreenshots.save("preferred-station-suggestions")
        app.onNodeWithTag("journey-station-picker").performScrollToNode(hasTestTag("station-choice:${stationKey("Salt River")}"));app.onNodeWithTag("station-choice:${stationKey("Salt River")}").performClick()
        app.waitUntil(15000) { features.state.value.extras.spots.first { it.id==work.id }.preferredStation=="Salt River" }
        app.runOnUiThread { features.place(WalkEndpoint("spot:${home.id}",home.name,home.point),false,home.preferredStation) }
        app.onNodeWithTag("journey-planner").performScrollToNode(hasText("Walking & route preferences"));app.onNodeWithText("Walking & route preferences").performClick()
        app.onNodeWithText("Swap From and To").performScrollTo().performClick()
        app.runOnIdle { assertEquals("Unibell",features.state.value.boardingStation);assertEquals("Salt River",features.state.value.arrivalStation);assertEquals("Home",features.state.value.access?.label);assertEquals("Work",features.state.value.destination?.label) };UiScreenshots.save("work-salt-river-unibell-home")
    }
    @Test fun preferredStationSurvivesReselectionAndBackupRestore() {
        app.runOnUiThread { features.place(WalkEndpoint("spot:${work.id}",work.name,work.point),true);features.chooseStation("Salt River",true,true) }
        app.waitUntil(15000) { features.state.value.extras.spots.first { it.id==work.id }.preferredStation=="Salt River" }
        val password="station preference test backup"
        runBlocking { val raw=features.files.export(password);val backup=features.files.inspect(raw,password);features.files.restore(backup) }
        app.waitUntil(15000) { features.state.value.extras.spots.firstOrNull { it.id==work.id }?.preferredStation=="Salt River" }
        app.runOnUiThread { features.place(null,true);features.place(WalkEndpoint("spot:${work.id}",work.name,work.point),true) }
        app.runOnIdle { assertEquals("Salt River",features.state.value.boardingStation) }
        app.onNodeWithTag("journey-planner").performScrollToNode(hasTestTag("home-boarding-station"))
        app.onNodeWithTag("home-boarding-station").performClick()
        app.onNodeWithText("Auto · find nearby usable stations").performClick()
        app.waitUntil(15000) { features.state.value.extras.spots.first { it.id==work.id }.preferredStation==null }
        app.runOnIdle { assertNull(features.state.value.boardingStation) }
    }
}
