@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package za.co.cttrains.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import za.co.cttrains.app.*
import za.co.cttrains.app.R
import za.co.cttrains.core.*

@Composable internal fun AbeonaHome(train: TrainState,model: TrainViewModel,s: FeatureState,m: FeatureViewModel,
    from: WalkEndpoint?,to: WalkEndpoint?,board: String?,arrive: String?,date: String,time: String,mode: String,
    onMode: (String)->Unit,onCompare: ()->Unit,onDestination: ()->Unit,onOrigin: ()->Unit,onSwap: ()->Unit,
    onDate: ()->Unit,onTime: ()->Unit,onStation: (Boolean)->Unit,onPreferences: ()->Unit,onSearch: ()->Unit) {
    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.weight(1f).testTag("journey-planner"),contentPadding=PaddingValues(horizontal=AbeonaSpace.page,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            item { AbeonaHeading("Where to?","A calmer way to get there.") }
            item { Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { ModeOfTransport(mode,compact=true,onMode=onMode) }
                TextButton(onClick=onCompare,modifier=Modifier.heightIn(min=48.dp)) { Text("Compare",style=MaterialTheme.typography.labelMedium) }
            } }
            item { Surface(color=MaterialTheme.colorScheme.surface,shape=AbeonaSpace.panel) {
                Box(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        EndpointRow("FROM",from?.label ?: "Choose your start",if(s.access!=null) board?.let { "Via $it · your choice" } ?: "Choose a preferred station" else "Choose any station or saved place",R.drawable.abeona_map_pin,"home-origin",onOrigin)
                        HorizontalDivider(Modifier.padding(start=58.dp,end=24.dp),color=MaterialTheme.colorScheme.outlineVariant)
                        EndpointRow("TO",to?.label ?: "Choose destination",if(s.destination!=null) arrive?.let { "Via $it · your choice" } ?: "Choose a preferred station" else null,R.drawable.abeona_search,"home-destination",onDestination)
                    }
                    IconButton(onClick=onSwap,enabled=from!=null&&to!=null,modifier=Modifier.align(Alignment.CenterEnd).padding(end=8.dp).size(48.dp)) { DesignIcon(R.drawable.abeona_arrow_down_up,"Swap origin and destination") }
                }
            } }
            if(s.access!=null || s.destination!=null) item { FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                if(s.access!=null) TextButton(onClick={ onStation(true) },modifier=Modifier.testTag("home-boarding-station")) { Text(board?.let { "Board at $it" } ?: "Choose boarding station") }
                if(s.destination!=null) TextButton(onClick={ onStation(false) },modifier=Modifier.testTag("home-arrival-station")) { Text(arrive?.let { "Get off at $it" } ?: "Choose arrival station") }
            } }
            item { Surface(shape=RoundedCornerShape(22.dp),color=MaterialTheme.colorScheme.surface) {
                Row(Modifier.fillMaxWidth().heightIn(min=64.dp).padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    DesignIcon(R.drawable.abeona_calendar_days,tint=MaterialTheme.colorScheme.onSurfaceVariant)
                    Column(Modifier.weight(1f).heightIn(min=48.dp).clickable(role=Role.Button,onClick=onDate).padding(vertical=8.dp)) {
                        Text(runCatching { java.time.LocalDate.parse(date).format(java.time.format.DateTimeFormatter.ofPattern("EEE, d MMM")) }.getOrDefault(date),style=MaterialTheme.typography.bodyMedium)
                        Text("After $time",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick=onTime) { DesignIcon(R.drawable.abeona_chevron_right,"Choose departure time") }
                }
            } }
            item { TextButton(onClick=onPreferences,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("route-preferences"),contentPadding=PaddingValues(horizontal=12.dp)) {
                DesignIcon(R.drawable.abeona_sliders_horizontal,tint=MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Walking & route preferences",Modifier.weight(1f).padding(horizontal=12.dp),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurface)
                DesignIcon(R.drawable.abeona_chevron_right,tint=MaterialTheme.colorScheme.onSurfaceVariant)
            } }
            item { Text("QUICK PLACES",Modifier.padding(start=4.dp,top=4.dp),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant) }
            item { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                listOf("Home","Work","School").forEach { label->
                    val spot=s.extras.spots.firstOrNull { SavedAddresses.role(it)==label }
                    Surface(onClick={ if(spot==null) model.edit { it.copy(requestedScreen="saved") } else { m.place(WalkEndpoint("spot:${spot.id}",spot.name,spot.point),false,spot.preferredStation);if(from==null) onOrigin() else onSearch() } },color=MaterialTheme.colorScheme.surface,shape=RoundedCornerShape(22.dp),modifier=Modifier.weight(1f).heightIn(min=78.dp).testTag("quick-place:$label")) {
                        Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                            DesignIcon(when(label) { "Home"->R.drawable.abeona_house;"Work"->R.drawable.abeona_briefcase_business;else->R.drawable.abeona_graduation_cap },tint=MaterialTheme.colorScheme.primary)
                            Text(label,style=MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            } }

        }
        Column(Modifier.fillMaxWidth().padding(start=20.dp,end=20.dp,bottom=12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically) {
                DesignIcon(R.drawable.abeona_clock3,modifier=Modifier.size(16.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Scheduled information. Check before travelling.",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AbeonaAction(if(to==null) "Choose a destination" else if(from==null) "Choose your start" else if(mode=="compare") "Compare routes" else "Find routes",onSearch,Modifier.testTag("home-find-routes"),enabled=from!=null&&to!=null&&from!=to)
        }
    }
}

@Composable private fun EndpointRow(label: String,value: String,detail: String?,asset: Int,tag: String,onClick: ()->Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min=86.dp).testTag(tag).clickable(role=Role.Button,onClick=onClick).padding(start=20.dp,end=64.dp,top=14.dp,bottom=14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
        DesignIcon(asset,tint=MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)) {
            Text(label,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value,style=MaterialTheme.typography.titleMedium)
            if(detail!=null) Text(detail,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
