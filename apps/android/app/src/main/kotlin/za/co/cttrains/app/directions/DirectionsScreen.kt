@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package za.co.cttrains.app.directions

import android.Manifest
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.Alignment
import za.co.cttrains.app.*
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import za.co.cttrains.core.*

@Composable fun DirectionsButton(makePlan: ()->DirectionsPlan,onReady: ()->Unit) {
    val m: DirectionsModel=androidx.lifecycle.viewmodel.compose.viewModel()
    TextButton(onClick={ m.preview(makePlan,onReady) },modifier=Modifier.testTag("start-directions-preview")) { Text("Directions") }
}
@Composable fun DirectionsCompactBanner(onOpen: ()->Unit) {
    val s by DirectionsRuntime.state.collectAsStateWithLifecycle()
    if(s.session!=null && s.preview==null) Surface(onClick=onOpen,color=MaterialTheme.colorScheme.secondaryContainer,modifier=Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal=16.dp,vertical=6.dp),horizontalArrangement=Arrangement.SpaceBetween) {
            Text(s.guidance?.title ?: if(s.running) "Directions active" else "Resume your directions",style=MaterialTheme.typography.labelLarge,modifier=Modifier.weight(1f),maxLines=2,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text("Open",style=MaterialTheme.typography.labelLarge)
        }
    }
}
@Composable fun DirectionsScreen(train: TrainState,trainModel: TrainViewModel,features: FeatureState,onPlan: ()->Unit,onMap: ()->Unit,onSharing: ()->Unit,onStopSharing: ()->Unit={}) {
    val m: DirectionsModel=androidx.lifecycle.viewmodel.compose.viewModel()
    val state by m.state.collectAsStateWithLifecycle()
    val c=LocalContext.current
    var issue by remember { mutableStateOf<String?>(null) }
    var menu by remember { mutableStateOf(false) }
    var setup by remember { mutableStateOf(false) }
    var voice by remember { mutableStateOf(DirectionSpeech.enabled(c)) }
    var stopping by remember { mutableStateOf(false) }
    var steps by remember { mutableStateOf(false) }
    val speech=remember { DirectionSpeech(c) { issue=it } }
    DisposableEffect(Unit) { onDispose { speech.close() } }
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants->
        if(grants[Manifest.permission.ACCESS_FINE_LOCATION]==true || grants[Manifest.permission.ACCESS_COARSE_LOCATION]==true) { m.start();if(Build.VERSION.SDK_INT>=33 && grants[Manifest.permission.POST_NOTIFICATIONS]!=true) issue="Notifications are off. You can enable them in Settings." }
        else issue="Location was not allowed. You can read the itinerary or try again."
    }
    val plan=state.preview ?: state.session?.plan
    val session=state.session?.takeIf { state.preview==null }
    val current=session?.step ?: plan?.steps?.firstOrNull()
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).testTag("directions-screen")) {
        LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(horizontal=20.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            item { Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(if(session!=null) "Your active journey" else "Ready to travel",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(plan?.title ?: "Journey directions",style=MaterialTheme.typography.titleSmall,maxLines=2) }
                Box { IconButton(onClick={ menu=true }) { Icon(Icons.Outlined.MoreVert,"Journey options") };DropdownMenu(menu,{ menu=false }) {
                    DropdownMenuItem(text={ Text("Notification settings") },onClick={ setup=true;menu=false })
                    DropdownMenuItem(text={ Text("View full map") },onClick={ onMap();menu=false })
                    DropdownMenuItem(text={ Text("Read current instruction") },onClick={ if(state.running) m.command("speech") else speech.speak(current?.let { it.title+". "+it.detail }.orEmpty());menu=false })
                } }
            } }
            state.error?.let { item { Text(it,color=MaterialTheme.colorScheme.error) } };issue?.let { item { Text(it,color=MaterialTheme.colorScheme.error) } }
            if(plan==null) item { Text("Choose a route on Home to see directions here.");Button(onClick=onPlan) { Text("Plan a journey") } }
            if(plan!=null) {
                item { Column(verticalArrangement=Arrangement.spacedBy(6.dp)) { Text(state.guidance?.title?.takeIf { session!=null } ?: current?.title.orEmpty(),style=MaterialTheme.typography.headlineLarge);val meters=state.guidance?.remainingMeters ?: current?.distanceMeters?.toInt();val minutes=state.guidance?.minutes ?: current?.durationSeconds?.let { kotlin.math.ceil(it/60).toInt() };if(meters!=null) Text("$meters m${minutes?.let { " · about $it min" }.orEmpty()}",style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.primary);Text(state.guidance?.detail?.takeIf { session!=null } ?: current?.detail.orEmpty(),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) } }
                item { Card(Modifier.fillMaxWidth().height(220.dp)) { DirectionsMapScreen(train,trainModel,features,onMap,compact=true) } }
                item { LinearProgressIndicator(progress={ (state.guidance?.progress ?: 0)/100f },modifier=Modifier.fillMaxWidth());Text("Step ${(session?.index ?: 0)+1} of ${plan.steps.size} · phone guidance",style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(top=6.dp)) }
                val next=plan.steps.getOrNull((session?.index ?: 0)+1)
                if(next!=null) item { Surface(color=MaterialTheme.colorScheme.surfaceContainer,shape=MaterialTheme.shapes.medium) { Column(Modifier.fillMaxWidth().padding(14.dp)) { Text("Up next",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary);Text(next.title,style=MaterialTheme.typography.titleMedium);Text(next.detail,style=MaterialTheme.typography.bodySmall) } } }
                item { plan.steps.lastOrNull { it.scheduledAt!=null }?.scheduledAt?.let { Text("Scheduled transport arrival · ${java.time.Instant.ofEpochMilli(it).atZone(CapeTownZone).toLocalTime().toString().take(5)}",style=MaterialTheme.typography.titleSmall) };Text(if(plan.sourceCheckedAt>0) "Timetable checked ${java.time.Instant.ofEpochMilli(plan.sourceCheckedAt).atZone(CapeTownZone).format(java.time.format.DateTimeFormatter.ofPattern("d MMM, HH:mm"))}" else "Source date unavailable · check before travelling",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);if(plan.notice.isNotBlank()) Text(plan.notice,style=MaterialTheme.typography.bodySmall) }
                item { Row(verticalAlignment=Alignment.CenterVertically) { Icon(Icons.Outlined.VolumeUp,null);Spacer(Modifier.width(12.dp));Text("Spoken guidance",Modifier.weight(1f));Switch(voice,{ voice=it;DirectionSpeech.enabled(c,it);if(!it) speech.stop();if(state.running) m.command("voice") }) } }
                item { Row(verticalAlignment=Alignment.CenterVertically) { Icon(Icons.Outlined.Share,null);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)) { Text("Location sharing");Text(if(features.live?.sharing==true) "Active · open to stop" else "Off · private by default",style=MaterialTheme.typography.bodySmall) };TextButton(onClick={ if(features.live?.sharing==true) onStopSharing() else onSharing() }) { Text(if(features.live?.sharing==true) "Stop sharing" else "Set up") } } }
                if(session==null || !state.running) item { Text("Active directions use location on this phone for up to four hours, with a visible notification. Sharing requires a separate, time-limited session.",style=MaterialTheme.typography.bodySmall) }
                item { TextButton(onClick={ steps=!steps }) { Text(if(steps) "Hide itinerary" else "Full itinerary") } }
                if(steps) itemsIndexed(plan.steps,key={ _,s->s.id }) { i,s->Column { Text("${i+1}. ${s.title}",style=MaterialTheme.typography.titleSmall);Text(s.detail,style=MaterialTheme.typography.bodySmall) } }
                if(state.preview!=null) item { TextButton(onClick={ m.discardPreview();onPlan() }) { Text("Choose another route") } }
            }
        }
        if(plan!=null) Column(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=12.dp)) {
            if(session==null || !state.running) Button(onClick={ permission.launch((listOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)+if(Build.VERSION.SDK_INT>=33) listOf(Manifest.permission.POST_NOTIFICATIONS) else emptyList()).toTypedArray()) },modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("begin-directions")) { Text(if(session==null) "Start directions" else "Resume directions") }
            else { Button(onClick={ m.command("next") },modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("directions-next")) { Text(when(current?.kind) { DirectionKind.BOARD->"I’ve boarded";DirectionKind.RIDE->"I’ve arrived";else->"I’m at the stop" }) };Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { TextButton(onClick={ m.command("pause") }) { Text(if(session.paused) "Resume GPS" else "Pause") };TextButton(onClick={ stopping=true },modifier=Modifier.testTag("directions-stop")) { Text("Stop journey",color=MaterialTheme.colorScheme.error) } } }
        }
    }
    if(setup) AlertDialog(onDismissRequest={ setup=false },title={ Text("Journey notifications") },text={ Column(Modifier.heightIn(max=400.dp).verticalScroll(rememberScrollState())) { LiveDirectionsSetup() } },confirmButton={ TextButton(onClick={ setup=false }) { Text("Done") } })
    if(stopping) AlertDialog(onDismissRequest={ stopping=false },title={ Text("Stop this journey?") },text={ Text("Directions and GPS sampling will end. Stop any separate sharing session in sharing settings.") },confirmButton={ TextButton(onClick={ m.stop();stopping=false;onPlan() },modifier=Modifier.testTag("confirm-stop-journey")) { Text("Stop journey") } },dismissButton={ TextButton(onClick={ stopping=false }) { Text("Continue") } })
}
@Composable fun LiveDirectionsSetup() {
    val c=LocalContext.current
    var promoted by remember { mutableStateOf(DirectionsNotifications.promotionAllowed(c)) }
    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) { promoted=DirectionsNotifications.promotionAllowed(c);onPauseOrDispose { } }
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("Live notifications",style=MaterialTheme.typography.titleMedium)
        Text(if(Build.VERSION.SDK_INT<36) "This Android version uses an ongoing directions notification. Android 16 adds Live Updates." else when(promoted) { true->"Android allows promoted notifications.";false->"Promoted notifications are disabled in device settings.";null->"This firmware decides which notifications can appear as Live Updates." })
        Text("On One UI 8, check Settings → Lock screen and AOD → Now Bar, and Abeona notification settings. Allow lock-screen content if you want the instruction there. Samsung controls eligibility; the app cannot force Now Bar placement.",style=MaterialTheme.typography.bodySmall)
        FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            TextButton(onClick={ runCatching { c.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,c.packageName)) } }) { Text("Notification settings") }
            if(Build.VERSION.SDK_INT>=36) TextButton(onClick={
                val live=Intent("android.settings.MANAGE_APP_PROMOTED_NOTIFICATIONS").setData(android.net.Uri.parse("package:${c.packageName}"))
                if(live.resolveActivity(c.packageManager)!=null) c.startActivity(live)
                else c.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,c.packageName))
            }) { Text("Live Update settings") }
        }
        Text("Pause stops GPS sampling. Stop ends the session. Boarding and getting off require your confirmation; passing a station or a scheduled time will not advance those steps.",style=MaterialTheme.typography.bodySmall)
    } }
}
