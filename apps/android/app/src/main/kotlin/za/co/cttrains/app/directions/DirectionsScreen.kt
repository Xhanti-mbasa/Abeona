@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package za.co.cttrains.app.directions

import android.Manifest
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.Alignment
import za.co.cttrains.app.*
import za.co.cttrains.app.R
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import za.co.cttrains.app.ui.AbeonaHeading
import za.co.cttrains.app.ui.AbeonaSpace
import za.co.cttrains.app.ui.DesignIcon
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
        LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(horizontal=20.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
            item { Column { Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Text(plan?.title ?: "Journey directions",style=MaterialTheme.typography.titleSmall,modifier=Modifier.weight(1f))
                Box { IconButton(onClick={ menu=true }) { DesignIcon(R.drawable.abeona_ellipsis,"Journey options") };DropdownMenu(menu,{ menu=false }) {
                    DropdownMenuItem(text={ Text("Notification settings") },onClick={ setup=true;menu=false })
                    DropdownMenuItem(text={ Text("View full map") },onClick={ onMap();menu=false })
                    DropdownMenuItem(text={ Text("Read current instruction") },onClick={ if(state.running) m.command("speech") else speech.speak(current?.let { it.title+". "+it.detail }.orEmpty());menu=false })
                } }
            }
            AbeonaHeading("Your journey",plan?.steps?.lastOrNull { it.scheduledAt!=null }?.scheduledAt?.let { "Scheduled arrival "+java.time.Instant.ofEpochMilli(it).atZone(CapeTownZone).toLocalTime().toString().take(5) } ?: "Your route, one step at a time.") } }
            state.error?.let { item { Text(it,color=MaterialTheme.colorScheme.error) } };issue?.let { item { Text(it,color=MaterialTheme.colorScheme.error) } }
            if(plan==null) item { Text("Choose a route in Plan to see directions here.");Button(onClick=onPlan) { Text("Plan a journey") } }
            if(plan!=null) {
                item { Card(Modifier.fillMaxWidth(),shape=AbeonaSpace.panel,colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().heightIn(min=200.dp).padding(22.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
                        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                            Surface(color=MaterialTheme.colorScheme.primaryContainer,shape=RoundedCornerShape(16.dp)) { Text(if(session?.paused==true) "PAUSED" else if(session==null) "JOURNEY PREVIEW" else "CURRENT STEP",Modifier.padding(horizontal=16.dp,vertical=8.dp),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onPrimaryContainer) }
                            Spacer(Modifier.weight(1f));DesignIcon(if(current?.kind==DirectionKind.WALK) R.drawable.abeona_navigation2 else R.drawable.abeona_train_front,modifier=Modifier.size(28.dp),tint=MaterialTheme.colorScheme.primary)
                        }
                        Text(state.guidance?.title?.takeIf { session!=null } ?: current?.title.orEmpty(),style=MaterialTheme.typography.headlineSmall.copy(fontWeight=FontWeight.Normal))
                        Text(state.guidance?.detail?.takeIf { session!=null } ?: current?.detail.orEmpty(),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
                        val meters=state.guidance?.remainingMeters ?: current?.distanceMeters?.toInt()
                        val minutes=state.guidance?.minutes ?: current?.durationSeconds?.let { kotlin.math.ceil(it/60).toInt() }
                        if(meters!=null) Text("$meters m${minutes?.let { " · about $it min" }.orEmpty()}",style=MaterialTheme.typography.titleSmall,color=MaterialTheme.colorScheme.primary)
                        current?.scheduledAt?.let { Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) { DesignIcon(R.drawable.abeona_clock3,tint=MaterialTheme.colorScheme.primary);Text("Scheduled · "+java.time.Instant.ofEpochMilli(it).atZone(CapeTownZone).toLocalTime().toString().take(5),style=MaterialTheme.typography.bodyMedium) } }
                    }
                } }
                item {
                    val index=session?.index ?: 0
                    val labels=remember(plan) {
                        (listOf(plan.title.substringBefore("→").trim())+plan.steps.filter { it.kind==DirectionKind.RIDE }.map { it.title.removePrefix("Get off at ") }+plan.title.substringAfter("→").trim()).filter { it.isNotBlank() }.distinct().let { if(it.size>3) listOf(it.first(),it[it.size/2],it.last()) else it }
                    }
                    val progress=(index+(state.guidance?.progress ?: 0)/100f)/plan.steps.size.coerceAtLeast(1)
                    Box(Modifier.fillMaxWidth().height(20.dp),contentAlignment=Alignment.Center) {
                        LinearProgressIndicator(progress={ progress },modifier=Modifier.fillMaxWidth().height(4.dp),trackColor=MaterialTheme.colorScheme.outlineVariant)
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { labels.forEachIndexed { i,_->Surface(Modifier.size(14.dp),shape=CircleShape,color=if(i==0 || progress>=i.toFloat()/(labels.size-1).coerceAtLeast(1)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,border=BorderStroke(2.dp,MaterialTheme.colorScheme.primary)) {} } }
                    }
                    Row(Modifier.fillMaxWidth()) { labels.forEachIndexed { i,label->Text(label,modifier=Modifier.weight(1f),style=MaterialTheme.typography.bodySmall,maxLines=2,overflow=TextOverflow.Ellipsis,textAlign=when(i) { 0->TextAlign.Start;labels.lastIndex->TextAlign.End;else->TextAlign.Center }) } }
                    Text("Step ${index+1} of ${plan.steps.size} · phone guidance",style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(top=12.dp))
                }
                val next=plan.steps.getOrNull((session?.index ?: 0)+1)
                if(next!=null) item { Surface(color=MaterialTheme.colorScheme.surface,shape=AbeonaSpace.panel) { Row(Modifier.fillMaxWidth().padding(20.dp),horizontalArrangement=Arrangement.spacedBy(16.dp),verticalAlignment=Alignment.CenterVertically) { DesignIcon(R.drawable.abeona_navigation2,tint=MaterialTheme.colorScheme.primary);Column(Modifier.weight(1f)) { Text("Next: ${next.title}",style=MaterialTheme.typography.titleSmall);Text(next.detail,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) } } } }
                item { Row(verticalAlignment=Alignment.CenterVertically) { DesignIcon(if(voice) R.drawable.abeona_volume2 else R.drawable.abeona_volume_x);Spacer(Modifier.width(12.dp));Text("Spoken guidance",Modifier.weight(1f));Switch(voice,{ voice=it;DirectionSpeech.enabled(c,it);if(!it) speech.stop();if(state.running) m.command("voice") }) } }
                item { OutlinedButton(onClick=onMap,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)) { DesignIcon(R.drawable.abeona_map);Spacer(Modifier.width(12.dp));Text("View route map") } }
                item { Row(verticalAlignment=Alignment.CenterVertically) { DesignIcon(R.drawable.abeona_share2);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)) { Text("Location sharing");Text(if(features.live?.sharing==true) "Active · open to stop" else "Off · private by default",style=MaterialTheme.typography.bodySmall) };TextButton(onClick={ if(features.live?.sharing==true) onStopSharing() else onSharing() }) { Text(if(features.live?.sharing==true) "Stop sharing" else "Set up") } } }
                item { Text(if(plan.sourceCheckedAt>0) "Timetable checked ${java.time.Instant.ofEpochMilli(plan.sourceCheckedAt).atZone(CapeTownZone).format(java.time.format.DateTimeFormatter.ofPattern("d MMM, HH:mm"))}" else "Source date unavailable · check before travelling",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);if(plan.notice.isNotBlank()) Text(plan.notice,style=MaterialTheme.typography.bodySmall) }
                if(session==null || !state.running) item { Text("Active directions use location on this phone for up to four hours, with a visible notification. Sharing requires a separate, time-limited session.",style=MaterialTheme.typography.bodySmall) }
                item { TextButton(onClick={ steps=!steps }) { Text(if(steps) "Hide itinerary" else "Full itinerary") } }
                if(steps) itemsIndexed(plan.steps,key={ _,step->step.id }) { i,step->Column { Text("${i+1}. ${step.title}",style=MaterialTheme.typography.titleSmall);Text(step.detail,style=MaterialTheme.typography.bodySmall) } }
                if(state.preview!=null) item { TextButton(onClick={ m.discardPreview();onPlan() }) { Text("Choose another route") } }
            }
        }
        if(plan!=null) Column(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            if(session==null || !state.running) Button(onClick={ permission.launch((listOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)+if(Build.VERSION.SDK_INT>=33) listOf(Manifest.permission.POST_NOTIFICATIONS) else emptyList()).toTypedArray()) },modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("begin-directions")) { Text(if(session==null) "Start directions" else "Resume directions") }
            else {
                Button(onClick={ m.command("next") },modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("directions-next")) { Text(when(current?.kind) { DirectionKind.BOARD->"I’ve boarded";DirectionKind.RIDE->"I’ve arrived";else->"I’m at the stop" }) }
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick={ m.command("pause") },modifier=Modifier.weight(1f).heightIn(min=48.dp)) { DesignIcon(R.drawable.abeona_pause);Spacer(Modifier.width(8.dp));Text(if(session.paused) "Resume GPS" else "Pause") }
                    OutlinedButton(onClick={ stopping=true },modifier=Modifier.weight(1f).heightIn(min=48.dp).testTag("directions-stop"),colors=ButtonDefaults.outlinedButtonColors(contentColor=MaterialTheme.colorScheme.error)) { DesignIcon(R.drawable.abeona_square);Spacer(Modifier.width(8.dp));Text("Stop journey") }
                }
            }
            Text("Phone guidance · no live train tracking",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.align(Alignment.CenterHorizontally))
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
