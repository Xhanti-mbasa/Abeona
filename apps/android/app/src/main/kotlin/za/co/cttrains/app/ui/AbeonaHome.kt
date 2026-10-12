package za.co.cttrains.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import za.co.cttrains.app.*
import za.co.cttrains.core.*

@Composable internal fun AbeonaHome(train: TrainState,model: TrainViewModel,s: FeatureState,m: FeatureViewModel,from: WalkEndpoint?,board: String?,date: String,time: String,onDestination: ()->Unit,onOrigin: ()->Unit,onDate: ()->Unit,onTime: ()->Unit,onStation: ()->Unit,onPreferences: ()->Unit,onSearch: ()->Unit) {
    LazyColumn(Modifier.fillMaxSize().testTag("journey-planner"),contentPadding=PaddingValues(horizontal=20.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
        item { Column(verticalArrangement=Arrangement.spacedBy(6.dp)) { Text("Where to?",style=MaterialTheme.typography.headlineLarge);Text("Let’s plan your next journey.",color=MaterialTheme.colorScheme.onSurfaceVariant) } }
        item { Surface(onClick=onDestination,shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surfaceContainer,modifier=Modifier.fillMaxWidth().heightIn(min=60.dp).testTag("home-destination")) { Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) { Icon(Icons.Outlined.Search,null,tint=MaterialTheme.colorScheme.primary);Text("Search places or stations",Modifier.weight(1f));Icon(Icons.Outlined.ChevronRight,null) } } }
        item { Column {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) { Icon(Icons.Outlined.TripOrigin,null,Modifier.size(20.dp));Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)) { Text(from?.let { "From ${it.label}" } ?: "Choose your start",style=MaterialTheme.typography.titleSmall);if(s.access!=null) Text(board?.let { "Via $it · your choice" } ?: "Station suggested when planning",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) };TextButton(onClick=onOrigin) { Text("Change") } }
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) { Icon(Icons.Outlined.Schedule,null,Modifier.size(20.dp));Spacer(Modifier.width(10.dp));Text("Leave after $time",Modifier.weight(1f));TextButton(onClick=onTime) { Text("Change time") } }
            TextButton(onClick=onDate) { Text(runCatching { java.time.LocalDate.parse(date).format(java.time.format.DateTimeFormatter.ofPattern("EEE, d MMM")) }.getOrDefault(date)) }
        } }
        item { Text("Quick destinations",style=MaterialTheme.typography.titleMedium) }
        item { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf("Home","Work","School").forEach { label->val spot=s.extras.spots.firstOrNull { SavedAddresses.role(it)==label };Surface(onClick={ if(spot==null) model.edit { it.copy(requestedScreen="saved") } else { m.place(WalkEndpoint("spot:${spot.id}",spot.name,spot.point),false,spot.preferredStation);if(from==null) onOrigin() else onSearch() } },color=MaterialTheme.colorScheme.surfaceContainerLow,shape=MaterialTheme.shapes.medium,modifier=Modifier.weight(1f).heightIn(min=100.dp).testTag("quick-place:$label")) { Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) { Icon(when(label) { "Home"->Icons.Outlined.Home;"Work"->Icons.Outlined.WorkOutline;else->Icons.Outlined.School },null,tint=MaterialTheme.colorScheme.primary);Text(label,style=MaterialTheme.typography.titleSmall);Text(spot?.preferredStation ?: if(spot!=null) "Saved place" else "Set address",style=MaterialTheme.typography.bodySmall,maxLines=2) } } } } }
        item { HorizontalDivider();Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) { Text("Nearby stations",style=MaterialTheme.typography.titleMedium,modifier=Modifier.weight(1f));TextButton(onClick={ if(from==null) onOrigin() else onStation() }) { Text("Choose station") } } }
        val nearby=from?.let { StationChoices.ranked(it.point,train.stations,train.catalog.stations).take(2) }.orEmpty()
        items(nearby,key={ it.name }) { station->Row(Modifier.fillMaxWidth().heightIn(min=56.dp).clickable { m.chooseStation(station.name,true);if(s.access==null) model.edit { it.copy(from=station.name) } },verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) { Icon(Icons.Outlined.Train,null,tint=MaterialTheme.colorScheme.onSurfaceVariant);Column(Modifier.weight(1f)) { Text(station.name,style=MaterialTheme.typography.titleSmall);Text(stationDistance(station,from!!.point),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) };if(stationKey(station.name)==board?.let(::stationKey)) Icon(Icons.Outlined.Check,null,tint=MaterialTheme.colorScheme.primary) } }
        item { Text("Suggestions only. Choose the station that works for you.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Surface(color=MaterialTheme.colorScheme.surfaceContainerLow,shape=MaterialTheme.shapes.medium) { Row(Modifier.padding(14.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)) { Icon(Icons.Outlined.Info,null,Modifier.size(20.dp));Text("Scheduled timetables · Abeona backend\nLive vehicle tracking is unavailable.",style=MaterialTheme.typography.bodySmall) } } }
        item { TextButton(onClick=onPreferences) { Text("Route preferences & providers") } }
    }
}
