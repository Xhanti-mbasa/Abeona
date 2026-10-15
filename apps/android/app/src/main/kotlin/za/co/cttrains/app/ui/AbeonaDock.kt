package za.co.cttrains.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import za.co.cttrains.app.R

@Composable internal fun AbeonaDock(screen: String, go: (String) -> Unit) {
    Surface(color=MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().heightIn(min=72.dp),verticalAlignment=Alignment.CenterVertically) {
            listOf(Triple("map","Map",R.drawable.abeona_map),Triple("plan","Plan",R.drawable.abeona_route),Triple("saved","Saved",R.drawable.abeona_bookmark),Triple("upcoming","Journeys",R.drawable.abeona_calendar_days),Triple("settings","Settings",R.drawable.abeona_settings)).forEach { (route,label,asset)->
                val selected=screen==route || route=="saved"&&screen=="places"
                val colour=if(selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                Column(Modifier.weight(1f).testTag("nav:$route").selectable(selected=selected,role=Role.Tab,onClick={ go(route) }).heightIn(min=72.dp).padding(vertical=6.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.width(28.dp).height(3.dp).then(if(selected) Modifier.background(colour,RoundedCornerShape(2.dp)) else Modifier))
                    DesignIcon(asset,modifier=Modifier.size(24.dp),tint=colour)
                    Text(label,style=MaterialTheme.typography.labelSmall,color=colour)
                }
            }
        }
    }
}
