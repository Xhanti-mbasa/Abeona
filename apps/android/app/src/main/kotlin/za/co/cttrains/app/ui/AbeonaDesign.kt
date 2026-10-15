package za.co.cttrains.app.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/** Shared native dimensions from the accepted One UI proposal. All sizes reflow in dp/sp. */
internal object AbeonaSpace {
    val page = 20.dp
    val inset = 24.dp
    val gap = 12.dp
    val touch = 48.dp
    val action = 56.dp
    val panel = RoundedCornerShape(26.dp)
    val sheet = RoundedCornerShape(32.dp)
}

@Composable internal fun DesignIcon(@DrawableRes asset: Int, description: String? = null,
    modifier: Modifier = Modifier.size(22.dp), tint: Color = LocalContentColor.current) {
    Icon(painterResource(asset), description, modifier, tint)
}

@Composable internal fun AbeonaHeading(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable internal fun AbeonaAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(onClick, modifier.fillMaxWidth().heightIn(min = AbeonaSpace.action), enabled = enabled,
        shape = RoundedCornerShape(28.dp), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)) { Text(label) }
}

@Composable internal fun SettingsTravelRow(label: String, @DrawableRes asset: Int, onClick: () -> Unit) {
    Surface(onClick=onClick,color=MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().heightIn(min=56.dp).padding(horizontal=20.dp,vertical=14.dp),horizontalArrangement=Arrangement.spacedBy(16.dp),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
            DesignIcon(asset,tint=MaterialTheme.colorScheme.onSurfaceVariant)
            Text(label,Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium)
            DesignIcon(za.co.cttrains.app.R.drawable.abeona_chevron_right,tint=MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
