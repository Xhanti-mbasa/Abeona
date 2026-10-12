@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package za.co.cttrains.app.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import za.co.cttrains.app.*
import za.co.cttrains.app.maps.*
import za.co.cttrains.core.*

@Composable internal fun PlaceMapPicker(train: TrainState,onDismiss: ()->Unit,onSelect: (WalkEndpoint)->Unit) {
    var point by remember { mutableStateOf<GeoPoint?>(null) };var address by remember { mutableStateOf("") };var failed by remember { mutableStateOf(false) };var retry by remember { mutableIntStateOf(0) }
    val viewport=remember { MapViewport() };val layers=remember(point) { FeatureLayers(pins=point?.let { listOf(MapPin("selected-place","Selected place",it,null,"Confirm the address before saving")) }.orEmpty()) }
    ModalBottomSheet(onDismissRequest=onDismiss,sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true)) {
        Column(Modifier.fillMaxWidth().imePadding().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("Choose on the map",style=MaterialTheme.typography.headlineSmall);Text("Touch and hold the location, then confirm its address. Online maps receive the viewed area and IP.",style=MaterialTheme.typography.bodySmall)
            if(train.mapsReady && !failed) key(retry) { StreetMap(train.copy(selectedJourney=null,routeTrace=null,from="",to=""),null,viewport,emptyList(),{}, {},{ failed=true },Modifier.fillMaxWidth().height(300.dp),features=layers,onDrop={ point=it }) }
            else { Text("Street map unavailable. Saved places and stations remain searchable.");TextButton(onClick={ failed=false;retry++ }) { Text("Retry map") } }
            point?.let { Text("Pin · %.5f, %.5f".format(java.util.Locale.ENGLISH,it.latitude,it.longitude),style=MaterialTheme.typography.bodySmall) }
            OutlinedTextField(address,{ address=it.take(100) },label={ Text("Confirm address or place description") },modifier=Modifier.fillMaxWidth(),maxLines=2)
            Button(onClick={ point?.let { onSelect(WalkEndpoint("pin:${java.util.UUID.randomUUID()}",address.trim(),it)) } },enabled=point!=null && address.isNotBlank(),modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)) { Text("Use this address") }
        }
    }
}
