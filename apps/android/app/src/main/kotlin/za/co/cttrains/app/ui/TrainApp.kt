@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class,androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package za.co.cttrains.app.ui

import android.content.Intent
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import za.co.cttrains.app.TrainState
import za.co.cttrains.app.BuildConfig
import za.co.cttrains.app.R
import za.co.cttrains.app.TrainViewModel
import za.co.cttrains.core.*
import androidx.navigation.compose.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import za.co.cttrains.app.scheduling.openStreetMapStationUrl
import za.co.cttrains.app.directions.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged

private val FullDate = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH)
private val FetchTime = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.ENGLISH).withZone(CapeTownZone)

@Composable
fun TrainApp(state: TrainState, model: TrainViewModel) {
    val features: za.co.cttrains.app.FeatureViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val feature by features.state.collectAsStateWithLifecycle()
    val liveFix by remember { DirectionsRuntime.state.map { if(it.running && it.session?.paused!=true) it.fix else null }.distinctUntilChanged() }.collectAsStateWithLifecycle(initialValue=null)
    var directionPositionShown by remember { mutableStateOf(false) }
    LaunchedEffect(liveFix) {
        if(liveFix!=null) {
            liveFix?.let { model.directionsPosition(za.co.cttrains.app.location.CurrentPosition(it.point.latitude,it.point.longitude,it.accuracy.toFloat(),it.at)) }
            directionPositionShown=true
        } else if(directionPositionShown) {
            model.directionsPosition(null)
            directionPositionShown=false
        }
    }
    val navigation = rememberNavController()
    var toolsOpen by remember { mutableStateOf(false) }
    val backStack by navigation.currentBackStackEntryAsState()
    val screen = backStack?.destination?.route ?: "plan"
    val fullMap = screen == "map/fullscreen" || screen=="directions/map" || screen=="directions"
    val snackbar = remember { SnackbarHostState() }
    fun go(route: String) { navigation.navigate(route) { popUpTo(navigation.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } }
    LaunchedEffect(state.message) { state.message?.let { snackbar.showSnackbar(it); model.dismissMessage() } }
    LaunchedEffect(feature.message) { feature.message?.let { snackbar.showSnackbar(it);features.dismissMessage() } }
    LaunchedEffect(feature.error) { feature.error?.let { snackbar.showSnackbar(it) } }
    LaunchedEffect(state.requestedScreen) { state.requestedScreen?.let { go(it); model.navigationHandled() } }
    Scaffold(
        contentWindowInsets=if(fullMap) WindowInsets(0,0,0,0) else WindowInsets.safeDrawing,
        topBar = { if(!fullMap) Column { TopAppBar(title={ Text(stringResource(R.string.app_name),style=MaterialTheme.typography.titleLarge) },actions={ IconButton(onClick={ go("settings") },modifier=Modifier.testTag("open-settings")) { Icon(Icons.Outlined.Settings,"Settings") } },colors=TopAppBarDefaults.topAppBarColors(containerColor=MaterialTheme.colorScheme.background));DirectionsCompactBanner { go("directions") } } },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { if(!fullMap) NavigationBar(containerColor=MaterialTheme.colorScheme.background,tonalElevation=0.dp) {
            listOf(Triple("plan","Home",Icons.Outlined.Home),Triple("upcoming","Journeys",Icons.AutoMirrored.Outlined.EventNote),Triple("saved","Saved",Icons.Outlined.BookmarkBorder),Triple("alerts","Updates",Icons.Outlined.NotificationsNone)).forEach { (route,title,icon)->NavigationBarItem(selected=screen==route || route=="saved" && screen=="places",onClick={ go(route) },icon={ Icon(icon,null) },label={ Text(title) },modifier=Modifier.testTag("nav:$route")) }
        } },
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
            val wide = maxWidth >= 840.dp
            NavHost(navigation, startDestination = "plan", modifier = Modifier.fillMaxSize(), enterTransition={ EnterTransition.None },exitTransition={ ExitTransition.None },popEnterTransition={ EnterTransition.None },popExitTransition={ ExitTransition.None }) {
                composable("map") { TransitMapScreen(state, model, onExpand = { navigation.navigate("map/fullscreen") { launchSingleTop = true } }, onPlan = { go("plan") },feature=feature,features=features,onBuses={ go("buses") }) }
                composable("map/fullscreen") { TransitMapScreen(state, model, fullScreen = true, onClose = { navigation.popBackStack() }, onPlan = { go("plan") },feature=feature,features=features,onBuses={ go("buses") }) }
                composable("plan") { PlanScreen(state, model, wide,feature,features) { journey -> model.selectJourney(journey);features.showTrainMap(); go("map") } }
                composable("saved") { SavedAddressesScreen(state,model,feature,features,{ go("plan") },{ go("map") }) }
                composable("places") { SavedAddressesScreen(state,model,feature,features,{ go("plan") },{ go("map") }) }
                composable("upcoming") { UpcomingScreen(state, model) }
                composable("settings") { AboutScreen(state, model,feature,features,{ go("buses") },{ go("trips") },{ go("community") },{ go("whatsapp") },{ go("directions") },{ go("app-updates") }) }
                composable("budget") { BudgetScreen(state, model) { go("plan") } }
                composable("alerts") { AlertsScreen(state, model) }
                composable("buses") { BusScreen(feature,features,{ go("trips") }) { service,route -> model.selectJourney(null);features.showBusMap(service,route);go("map") } }
                composable("trips") { SafetyTripsScreen(state,feature,features) }
                composable("whatsapp") { za.co.cttrains.app.whatsapp.WhatsAppScreen({ go("community") },{ go("trips") }) }
                composable("community") { CommunityScreen(state,feature,features,model) { go("settings") } }
                composable("directions") { DirectionsScreen(state,model,feature,{ go("plan") },{ navigation.navigate("directions/map") { launchSingleTop=true } },{ go("trips") }) }
                composable("directions/map") { DirectionsMapScreen(state,model,feature,{ navigation.popBackStack() }) }
                composable("app-updates") { za.co.cttrains.app.updates.UpdatesScreen() }
            }
        }
    }
    if (toolsOpen) TransitToolsSheet({ toolsOpen = false }, { toolsOpen = false; go("alerts") }, { toolsOpen = false; go("budget") },{ toolsOpen=false;go("buses") },{ toolsOpen=false;go("trips") },{ toolsOpen=false;go("community") },{ toolsOpen=false;go("whatsapp") },{ toolsOpen=false;go("directions") },{ toolsOpen=false;go("app-updates") })
    ImportedFiles(feature,features)
}

@Composable
private fun PlanScreen(state: TrainState, model: TrainViewModel, wide: Boolean, feature: za.co.cttrains.app.FeatureState, features: za.co.cttrains.app.FeatureViewModel, onMap: (Journey) -> Unit) {
    JourneyPlanner(state,model,feature,features,{ model.edit { it.copy(requestedScreen="settings") } })
}

@Composable
private fun StationSheet(label: String, stations: List<String>, other: String, onDismiss: () -> Unit, onSelected: (String) -> Unit) {
    var search by rememberSaveable { mutableStateOf("") }
    val filtered = remember(search, stations) { stations.filter { it.contains(search.trim(), ignoreCase = true) } }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).imePadding()) {
            Text("${if (label == "From") "Departure" else "Arrival"} station", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Search stations") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, singleLine = true, shape = RoundedCornerShape(16.dp))
            Spacer(Modifier.height(12.dp))
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                if (filtered.isEmpty()) item { Text("No matching stations.", Modifier.padding(16.dp)) }
                items(filtered, key = { it }) { station ->
                    ListItem(headlineContent = { Text(station) }, leadingContent = { Icon(Icons.Outlined.Train, null) }, supportingContent = if (station == other) ({ Text("Already selected for the other station") }) else null, modifier = Modifier.clickable(enabled = station != other, role = Role.Button) { onSelected(station) })
                }
            }
        }
    }
}

@Composable
internal fun TravelDatePicker(date: String, holidays: List<String>, onDismiss: () -> Unit, trainOnly: Boolean=true, onSelected: (String) -> Unit) {
    val today = LocalDate.now(CapeTownZone)
    val picker = rememberDatePickerState(initialSelectedDateMillis = LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(), yearRange = today.year..TravelRules.lastAllowedDate(today).year, selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            val selected = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
            return selected >= today && selected <= TravelRules.lastAllowedDate(today) && (!trainOnly || (selected.dayOfWeek.value != 7 && selected.toString() !in holidays))
        }
    })
    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onClick = {
        picker.selectedDateMillis?.let { onSelected(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString()) }
    }, enabled = picker.selectedDateMillis?.let { picker.selectableDates.isSelectableDate(it) }==true) { Text("Choose date") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }) { DatePicker(state = picker,showModeToggle=false) }
}

@Composable
internal fun DepartureTimePicker(time: String, onDismiss: () -> Unit, trainOnly: Boolean=true, onSelected: (String) -> Unit) {
    var hour by rememberSaveable { mutableIntStateOf(time.substringBefore(":").toInt().coerceIn(if(trainOnly) 3 else 0,23)) }
    var minute by rememberSaveable { mutableIntStateOf(time.substringAfter(":").toInt()/15*15) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Depart after") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("24-hour time · 15-minute intervals", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimeDropdown("Hour", (if(trainOnly) 3..23 else 0..23).toList(), hour, Modifier.weight(1f)) { hour = it }
                TimeDropdown("Minute", listOf(0, 15, 30, 45), minute, Modifier.weight(1f)) { minute = it }
            }
        }
    }, confirmButton = { TextButton(onClick = { onSelected(String.format(Locale.ROOT, "%02d:%02d", hour, minute)) }) { Text("Choose time") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
private fun TimeDropdown(label: String, values: List<Int>, selected: Int, modifier: Modifier, onSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = !expanded }, modifier) {
        OutlinedTextField(value = selected.toString().padStart(2, '0'), onValueChange = {}, readOnly = true, label = { Text(label) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth())
        ExposedDropdownMenu(expanded, { expanded = false }) {
            values.forEach { value -> DropdownMenuItem(text = { Text(value.toString().padStart(2, '0')) }, onClick = { onSelected(value); expanded = false }) }
        }
    }
}

@Composable
private fun AboutScreen(state: TrainState, model: TrainViewModel,feature: za.co.cttrains.app.FeatureState,features: za.co.cttrains.app.FeatureViewModel,onBuses: ()->Unit,onTrips: ()->Unit,onCommunity: ()->Unit,onWhatsApp: ()->Unit,onDirections: ()->Unit,onUpdates: ()->Unit) {
    val uri = LocalUriHandler.current
    var confirmClear by remember { mutableStateOf(false) }
    var confirmProfile by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp).widthIn(max = 700.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Your app, your data", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Notice("An independent, community app for Cape Town rail commuters. Timetables come from CT Trains. This app is not affiliated with CT Trains, PRASA or Metrorail.", Icons.Outlined.Train)
        FeatureStatus(feature)
        FilledTonalButton(onClick=onDirections,modifier=Modifier.fillMaxWidth()) { Text("Journey directions & Live Updates") }
        OutlinedButton(onClick=onUpdates,modifier=Modifier.fillMaxWidth()) { Text("App updates") }
        OutlinedButton(onClick=model::openMap,modifier=Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Map,null);Spacer(Modifier.width(8.dp));Text("Explore transport map") }
        BetaSettings(feature,features,onBuses,onTrips,onCommunity,onWhatsApp)
        ProfileSettings(feature,features)
        HostedSettings(feature,features)
        Card(shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Appearance", style = MaterialTheme.typography.titleLarge)
                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("System", "Light", "Dark").forEach { theme -> FilterChip(selected = state.preferences.theme == theme, onClick = { model.setTheme(theme) }, label = { Text(theme) }) }
                }
                if (Build.VERSION.SDK_INT >= 31) Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("Use device colours"); Text("Material You wallpaper palette", style = MaterialTheme.typography.bodySmall) }
                    Switch(checked = state.preferences.dynamicColor, onCheckedChange = model::setDynamicColor)
                }
            }
        }
        Card(shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Timetables & privacy", style = MaterialTheme.typography.titleLarge)
                Text("Your last 12 searches are stored on this device for up to 14 days. If a refresh fails, the same route, date and departure time can use its saved result. Saved routes stay until you remove them.")
                Text("An anonymous UUID identifies this installation locally. Personal records and optional account tokens are encrypted with Android Keystore in Room. Public station positions and scheduling timestamps remain readable inside the private database. No analytics or device identifiers. Google profile and hosted Matrix accounts are optional and independent.", style = MaterialTheme.typography.bodyMedium)
                Text("Searches send the route, date, time and IP to their timetable source. Online maps reveal the viewed area and IP to OpenFreeMap or City of Cape Town imagery hosting. Explicit place searches go to OpenStreetMap Nominatim or Photon. Offline map mode disables map tile requests; timetable and hosted communication checks have their own controls.", style = MaterialTheme.typography.bodyMedium)
                Text("Locate me samples Android location while the app is open. Active directions run locally with a visible notification for up to four hours; no GPS history is saved or uploaded. Separate, optional live sharing runs for at most two hours and sends encrypted updates to your private Matrix room. Public reports go to the selected community room. Friend positions disappear from the map after five minutes without a fresh update.", style = MaterialTheme.typography.bodyMedium)
                Text("Calendar handoff opens your calendar app for review. The app cannot read your calendar. Optional Health Connect step totals are read only after separate permission; no health data is uploaded. Trip files reveal the itinerary you review before sharing. Encrypted profile backups are stored only at the file location you choose.", style = MaterialTheme.typography.bodyMedium)
                Text("Downloaded maps and map-area bounds are public cache data and are not encrypted. Manage and delete them from the Map download button.", style = MaterialTheme.typography.bodyMedium)
                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("online" to "Online", "offline" to "Offline", "stations" to "Stations").forEach { (mode, title) -> FilterChip(state.preferences.mapMode == mode, { model.setMapMode(mode) }, label = { Text(title) }) } }
                OutlinedButton(onClick = model::forgetLocation) { Text("Forget sampled location") }
                OutlinedButton(onClick = { confirmProfile = true }) { Icon(Icons.Outlined.DeleteForever, null); Spacer(Modifier.width(8.dp)); Text("Delete all local personal data") }
                OutlinedButton(onClick = { confirmClear = true }) { Icon(Icons.Outlined.DeleteOutline, null); Spacer(Modifier.width(8.dp)); Text("Clear cached timetables") }
            }
        }
        SourceSelector(state, model)
        Text("Check before you travel", style = MaterialTheme.typography.titleLarge)
        Text("Timetables are not live tracking. Open Updates & delays for published service and platform notices; reports may be incomplete or late. The finder supports today through the end of next month and does not list Sunday or public-holiday services.")
        OutlinedButton(onClick = model::openAlerts) { Text("Updates & delays") }
        OutlinedButton(onClick = { uri.openUri("https://cttrains.co.za/train-form.php") }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.AutoMirrored.Outlined.OpenInNew, null); Spacer(Modifier.width(8.dp)); Text("Visit CT Trains") }
        TextButton(onClick = { uri.openUri("https://github.com/Xhanti-mbasa") }) { Text("Built by Xhanti-mbasa · GitHub",style=MaterialTheme.typography.labelMedium) }
        Text("${stringResource(R.string.app_name)} for Android · ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (confirmClear) AlertDialog(onDismissRequest = { confirmClear = false }, title = { Text("Clear saved timetables?") }, text = { Text("You’ll need an internet connection for your next search. Your favourite routes will stay saved.") }, confirmButton = { TextButton(onClick = { model.clearCache(); confirmClear = false }) { Text("Clear") } }, dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } })
    if (confirmProfile) AlertDialog(onDismissRequest = { confirmProfile = false }, title = { Text("Delete your local profile?") }, text = { Text("Favourites, corrections, journeys, reminders, personal spots/photos, trip files, account sessions and saved timetables will be removed. Public offline maps are managed separately. External backup files and calendar events remain at their chosen locations.") }, confirmButton = { TextButton(onClick = { features.reset();model.clearPersonalData(); confirmProfile = false }) { Text("Delete local data") } }, dismissButton = { TextButton(onClick = { confirmProfile = false }) { Text("Cancel") } })
}

@Composable
private fun Notice(text: String, icon: ImageVector, error: Boolean = false) {
    Surface(color = if (error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, Modifier.size(22.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EmptyState(icon: ImageVector, title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 28.dp, horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(body, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun SourceSelector(state: TrainState, model: TrainViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Timetable source", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            model.repository.sources.forEach { source -> FilterChip(selected = state.preferences.sourceId == source.id, onClick = { model.setSource(source.id) }, label = { Text(source.label) }) }
        }
        Text(if (model.repository.sources.size > 1) "The API and direct source both use CT Trains. Results stay separate; switching clears the current result." else "Community timetable from CT Trains. Check source and fetch date before travelling.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
