@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class,androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package za.co.cttrains.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import za.co.cttrains.app.*
import za.co.cttrains.core.*

@Composable fun SavedAddressesScreen(train: TrainState,model: TrainViewModel,s: FeatureState,m: FeatureViewModel,onPlan: ()->Unit,onMap: ()->Unit={}) {
    var editing by remember { mutableStateOf<String?>(null) }
    var chosenLabel by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<String?>(null) }
    var target by remember { mutableStateOf<WalkEndpoint?>(null) }
    var stationChoice by remember { mutableStateOf<MarkedSpot?>(null) }
    var removing by remember { mutableStateOf<MarkedSpot?>(null) }
    var customName by remember { mutableStateOf("") }
    fun edit(label: String,id: String?) { editing=label;chosenLabel=label;editingId=id }
    fun use(spot: MarkedSpot,start: Boolean) {
        m.place(WalkEndpoint("spot:${spot.id}",spot.name,spot.point),start,spot.preferredStation)
        spot.preferredStation?.let { station->model.edit { if(start) it.copy(from=station) else it.copy(to=station) } }
        onPlan()
    }
    LazyColumn(Modifier.fillMaxSize().testTag("saved-addresses"),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item { AbeonaHeading("Saved places","Your everyday places, ready to go.") }
        item { FeatureStatus(s) }
        items(SavedAddresses.labels) { label->
            val spot=s.extras.spots.firstOrNull { SavedAddresses.role(it)==label }
            AddressCard(label,spot,{ edit(label,spot?.id) },{ spot?.let { use(it,true) } },{ spot?.let { use(it,false) } },{ stationChoice=spot },{ removing=spot })
        }
        items(s.extras.spots.filter { SavedAddresses.role(it)==null },key={ it.id }) { spot->AddressCard(spot.name,spot,{ edit(spot.name,spot.id) },{ use(spot,true) },{ use(spot,false) },{ stationChoice=spot },{ removing=spot }) }
        item { OutlinedButton(onClick={ customName="";edit("Custom",null) },modifier=Modifier.fillMaxWidth().heightIn(min=52.dp).testTag("add-custom-place")) { Text("Add a place") } }
        item { HorizontalDivider();Text("Favourite stations",style=MaterialTheme.typography.titleMedium) }
        items(train.preferences.favoriteStations,key={ "station:$it" }) { name->ListItem(headlineContent={ Text(name) },trailingContent={ Row { TextButton(onClick={ m.place(null,true);model.edit { it.copy(from=name) };onPlan() }) { Text("From here") };TextButton(onClick={ model.toggleStation(name) }) { Text("Remove") } } }) }
        item { Text("Saved routes",style=MaterialTheme.typography.titleMedium) }
        items(train.preferences.routes,key={ "route:$it" }) { route->ListItem(headlineContent={ Text("${route.from} → ${route.to}") },trailingContent={ TextButton(onClick={ m.place(null,true);m.place(null,false);model.useRoute(route);onPlan() }) { Text("Plan") } }) }
        item { Text("Addresses stay on this phone. Preferred stations can be changed for any journey.",style=MaterialTheme.typography.bodySmall) }
        item { TextButton(onClick=onMap) { Text("Explore map") } }
    }
    editing?.let { label->JourneyPlacePicker(train,model,s,m,true,{ editing=null }) { place->target=place;editing=null } }
    target?.let { place->
        val label=chosenLabel
        val reserved=label=="Custom" && SavedAddresses.labels.any { it.equals(customName.trim(),ignoreCase=true) }
        AlertDialog(onDismissRequest={ target=null },title={ Text("Confirm $label address") },text={ Column(Modifier.heightIn(max=360.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) { Text(place.label);Text("${place.point.latitude}, ${place.point.longitude}",style=MaterialTheme.typography.bodySmall);if(label=="Custom") OutlinedTextField(customName,{ customName=it.take(100) },label={ Text("Place name") },singleLine=true,isError=reserved,supportingText=if(reserved) ({ Text("Edit Home, Work or School from its saved-place card. Choose another name here.") }) else null);Text("Choose a station after saving.",style=MaterialTheme.typography.bodySmall) } },confirmButton={ TextButton(onClick={ m.saveAddress(if(label=="Custom") customName.trim() else label,place,editingId);target=null },enabled=label!="Custom" || customName.isNotBlank() && !reserved) { Text("Save address") } },dismissButton={ TextButton(onClick={ target=null }) { Text("Cancel") } })
    }
    stationChoice?.let { spot->JourneyStationPicker(WalkEndpoint("spot:${spot.id}",spot.name,spot.point),train,s,true,spot.preferredStation,{ stationChoice=null }) { station,_->
        m.saveSpotStation(spot.id,station);stationChoice=null
    } }
    removing?.let { spot->AlertDialog(onDismissRequest={ removing=null },title={ Text("Remove ${spot.name}?") },text={ Text("You can set it again later.") },confirmButton={ TextButton(onClick={ m.removeSpot(spot.id);removing=null }) { Text("Remove") } },dismissButton={ TextButton(onClick={ removing=null }) { Text("Keep") } }) }
}
@Composable private fun AddressCard(label: String,spot: MarkedSpot?,onEdit: ()->Unit,onFrom: ()->Unit,onTo: ()->Unit,onStation: ()->Unit,onRemove: ()->Unit) {
    Card(Modifier.fillMaxWidth(),shape=AbeonaSpace.panel,colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) { Icon(when(label) { "Home"->Icons.Outlined.Home;"Work"->Icons.Outlined.WorkOutline;"School"->Icons.Outlined.School;else->Icons.Outlined.Place },null,tint=MaterialTheme.colorScheme.primary);Text(label,style=MaterialTheme.typography.titleLarge) }
        Text(spot?.note?.ifBlank { "Saved map location" } ?: "Address not set",style=MaterialTheme.typography.bodyMedium)
        spot?.preferredStation?.let { Text("Preferred station · $it",style=MaterialTheme.typography.labelLarge) }
        FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            TextButton(onClick=onEdit,modifier=Modifier.testTag("edit-address:$label")) { Text(if(spot==null) "Set address" else "Edit address") }
            if(spot!=null) {
                TextButton(onClick=onFrom) { Text("From here") };TextButton(onClick=onTo) { Text("To here") }
                TextButton(onClick=onStation) { Text("Choose station") };TextButton(onClick=onRemove) { Text("Remove") }
            }
        }
    } }
}
