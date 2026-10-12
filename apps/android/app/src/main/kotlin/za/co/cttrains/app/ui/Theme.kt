package za.co.cttrains.app.ui

import android.os.Build
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import za.co.cttrains.app.data.Preferences

private val Light=lightColorScheme(
    primary=Color(0xFF425B19),onPrimary=Color.White,primaryContainer=Color(0xFFD8ED9A),onPrimaryContainer=Color(0xFF1B290B),
    secondary=Color(0xFF52605A),secondaryContainer=Color(0xFFE4EBCF),tertiary=Color(0xFF526879),tertiaryContainer=Color(0xFFD9E7F1),
    background=Color(0xFFF4F6F2),surface=Color(0xFFF4F6F2),surfaceContainer=Color(0xFFE9EEE6),surfaceContainerLow=Color(0xFFEEF2EA),
    surfaceContainerHigh=Color(0xFFE2E8DE),surfaceContainerHighest=Color(0xFFDCE3D7),onSurface=Color(0xFF171C20),onSurfaceVariant=Color(0xFF515C55),outline=Color(0xFF727E72),outlineVariant=Color(0xFFCDD5C9),
)
private val Dark=darkColorScheme(
    primary=Color(0xFFD8ED9A),onPrimary=Color(0xFF1B290B),primaryContainer=Color(0xFF344326),onPrimaryContainer=Color(0xFFE1F1B4),
    secondary=Color(0xFFB8CAB1),secondaryContainer=Color(0xFF323D32),tertiary=Color(0xFFB3CDDD),tertiaryContainer=Color(0xFF354A58),
    background=Color(0xFF171C20),surface=Color(0xFF171C20),surfaceContainer=Color(0xFF232B30),surfaceContainerLow=Color(0xFF1D2429),
    surfaceContainerHigh=Color(0xFF2B3438),surfaceContainerHighest=Color(0xFF333E41),onSurface=Color(0xFFF4F6F2),onSurfaceVariant=Color(0xFFAFBBB5),outline=Color(0xFF84938B),outlineVariant=Color(0xFF3D484A),
)
private fun type(size: Int,line: Int,weight: FontWeight=FontWeight.Normal)=TextStyle(fontFamily=FontFamily.SansSerif,fontSize=size.sp,lineHeight=line.sp,fontWeight=weight,letterSpacing=0.sp)
private val AbeonaType=Typography(
    headlineLarge=type(32,38,FontWeight.SemiBold),headlineMedium=type(28,34,FontWeight.SemiBold),headlineSmall=type(24,30,FontWeight.SemiBold),
    titleLarge=type(22,28,FontWeight.Medium),titleMedium=type(18,24,FontWeight.Medium),titleSmall=type(16,22,FontWeight.Medium),
    bodyLarge=type(16,24),bodyMedium=type(16,23),bodySmall=type(14,20),labelLarge=type(14,20,FontWeight.Medium),labelMedium=type(14,20,FontWeight.Medium),labelSmall=type(13,18),
)
private val AbeonaShapes=Shapes(extraSmall=RoundedCornerShape(8.dp),small=RoundedCornerShape(12.dp),medium=RoundedCornerShape(16.dp),large=RoundedCornerShape(20.dp),extraLarge=RoundedCornerShape(24.dp))

@Composable
fun TrainTheme(preferences: Preferences, content: @Composable () -> Unit) {
    val dark = when (preferences.theme) { "Light" -> false; "Dark" -> true; else -> isSystemInDarkTheme() }
    val context = LocalContext.current
    val view = LocalView.current
    SideEffect {
        (context as? Activity)?.let { activity ->
            WindowCompat.getInsetsController(activity.window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    val colors = if (preferences.dynamicColor && Build.VERSION.SDK_INT >= 31) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (dark) Dark else Light
    MaterialTheme(colorScheme=colors,typography=AbeonaType,shapes=AbeonaShapes,content=content)
}
