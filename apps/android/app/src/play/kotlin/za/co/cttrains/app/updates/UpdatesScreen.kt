package za.co.cttrains.app.updates
import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.play.core.appupdate.*
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.*
object UpdateWork { fun reset(c: Context) { androidx.work.WorkManager.getInstance(c).apply { cancelUniqueWork("app-update-download");cancelUniqueWork("app-update-check") } } }
@Composable fun UpdatesScreen() {
    val c=LocalContext.current;val manager=remember { AppUpdateManagerFactory.create(c) }
    var info by remember { mutableStateOf<AppUpdateInfo?>(null) };var message by remember { mutableStateOf("Check Google Play for an available update.") };var checking by remember { mutableStateOf(false) };var downloaded by remember { mutableStateOf(false) }
    val launcher=rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { if(it.resultCode!=Activity.RESULT_OK) message="Update cancelled. Try again when ready." }
    fun check() { checking=true;manager.appUpdateInfo.addOnSuccessListener { next->checking=false;info=next;downloaded=next.installStatus()==InstallStatus.DOWNLOADED;message=if(next.updateAvailability()==UpdateAvailability.UPDATE_AVAILABLE) "An update is available through Google Play." else "Google Play reports no update for this installation." }.addOnFailureListener { checking=false;message="Google Play could not check this installation. Install Abeona from a Play testing or production track, then retry." } }
    DisposableEffect(manager) { val listener=InstallStateUpdatedListener { downloaded=it.installStatus()==InstallStatus.DOWNLOADED;if(it.installStatus()==InstallStatus.FAILED) message="The update failed. Check your connection and retry." };manager.registerListener(listener);onDispose { manager.unregisterListener(listener) } }
    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) { check();onPauseOrDispose { } }
    Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Text("App updates",style=MaterialTheme.typography.headlineLarge);Text(message);if(checking) LinearProgressIndicator(Modifier.fillMaxWidth())
        if(downloaded) Button(onClick={ manager.completeUpdate().addOnFailureListener { message="Could not apply the update. Try again." } }) { Text("Restart & update") }
        else if(info?.updateAvailability()==UpdateAvailability.UPDATE_AVAILABLE && info?.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)==true) Button(onClick={ info?.let { next->runCatching { manager.startUpdateFlowForResult(next,launcher,AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()) }.onFailure { message="Could not start the update. Check again." };info=null } }) { Text("Update with Google Play") }
        else OutlinedButton(onClick=::check,enabled=!checking) { Text("Check for updates") }
    }
}
