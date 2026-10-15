package za.co.cttrains.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import za.co.cttrains.app.data.Preferences
import za.co.cttrains.app.ui.*

/** Narrow, large-text acceptance: primary actions and every tab stay reachable. */
class OneUiLayoutTest {
    @get:Rule val app=createComposeRule()
    @Test fun narrowLargeTextKeepsPrimaryActionAboveEveryNavigationTab() {
        var taps=0
        app.setContent {
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,2f)) {
                TrainTheme(Preferences(theme="Dark")) {
                    var route by remember { mutableStateOf("plan") }
                    Column(Modifier.size(320.dp,640.dp)) {
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) { repeat(12) { Text("Scheduled route information that can reflow at larger text sizes.") } }
                        AbeonaAction("Find routes",{ taps++ },Modifier.padding(20.dp).testTag("large-text-action"))
                        AbeonaDock(route) { route=it }
                    }
                }
            }
        }
        val action=app.onNodeWithTag("large-text-action").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        for(route in listOf("map","plan","saved","upcoming","settings")) {
            val tab=app.onNodeWithTag("nav:$route").assertIsDisplayed()
            assertTrue("Primary action must not overlap $route",action.bottom<=tab.fetchSemanticsNode().boundsInRoot.top)
            tab.performClick().assertIsSelected()
        }
        app.onNodeWithTag("large-text-action").performClick()
        app.runOnIdle { assertEquals(1,taps) }
        UiScreenshots.save("one-ui-320dp-200percent-text")
    }
}
