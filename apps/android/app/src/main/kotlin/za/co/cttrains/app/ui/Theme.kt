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
    primary=Color(0xFF006BD6),onPrimary=Color.White,primaryContainer=Color(0xFFE6F0FF),onPrimaryContainer=Color(0xFF006BD6),
    secondary=Color(0xFF63636C),onSecondary=Color.White,secondaryContainer=Color(0xFFE6F0FF),onSecondaryContainer=Color(0xFF006BD6),
    tertiary=Color(0xFF126F60),tertiaryContainer=Color(0xFFE3F3EB),onTertiaryContainer=Color(0xFF126F60),
    background=Color(0xFFF3F3F6),onBackground=Color(0xFF151518),surface=Color.White,onSurface=Color(0xFF151518),
    surfaceContainer=Color.White,surfaceContainerLow=Color.White,surfaceContainerHigh=Color(0xFFEAEAF0),surfaceContainerHighest=Color.White,
    surfaceVariant=Color(0xFFEAEAF0),onSurfaceVariant=Color(0xFF63636C),outline=Color(0xFF858590),outlineVariant=Color(0xFFDDDDE4),
    error=Color(0xFFB53036),errorContainer=Color(0xFFFCEBED),onErrorContainer=Color(0xFF7D1A23),
    inverseSurface=Color(0xFF1C1C1F),inverseOnSurface=Color(0xFFF6F6F8),inversePrimary=Color(0xFF91BFFF),
)
private val Dark=darkColorScheme(
    primary=Color(0xFF91BFFF),onPrimary=Color(0xFF10233D),primaryContainer=Color(0xFF162B47),onPrimaryContainer=Color(0xFF91BFFF),
    secondary=Color(0xFFB0B0BA),secondaryContainer=Color(0xFF162B47),onSecondaryContainer=Color(0xFF91BFFF),
    tertiary=Color(0xFF87D8BD),tertiaryContainer=Color(0xFF16382E),onTertiaryContainer=Color(0xFF87D8BD),
    background=Color(0xFF080809),onBackground=Color(0xFFF6F6F8),surface=Color(0xFF1C1C1F),onSurface=Color(0xFFF6F6F8),
    surfaceContainer=Color(0xFF1C1C1F),surfaceContainerLow=Color(0xFF1C1C1F),surfaceContainerHigh=Color(0xFF29292F),surfaceContainerHighest=Color(0xFF1C1C1F),
    surfaceVariant=Color(0xFF29292F),onSurfaceVariant=Color(0xFFB0B0BA),outline=Color(0xFF858590),outlineVariant=Color(0xFF393940),
    error=Color(0xFFFFADB0),errorContainer=Color(0xFF432126),onErrorContainer=Color(0xFFFFDADC),
    inverseSurface=Color(0xFFF6F6F8),inverseOnSurface=Color(0xFF151518),inversePrimary=Color(0xFF006BD6),
)
private fun type(size: Int,line: Int,weight: FontWeight=FontWeight.Normal)=TextStyle(fontFamily=FontFamily.SansSerif,fontSize=size.sp,lineHeight=line.sp,fontWeight=weight,letterSpacing=0.sp)
private val AbeonaType=Typography(
    displaySmall=type(47,54,FontWeight.Bold),headlineLarge=type(38,46,FontWeight.Bold),headlineMedium=type(30,38,FontWeight.Bold),headlineSmall=type(28,34,FontWeight.SemiBold),
    titleLarge=type(22,28,FontWeight.SemiBold),titleMedium=type(18,24,FontWeight.Medium),titleSmall=type(16,24,FontWeight.SemiBold),
    bodyLarge=type(16,24),bodyMedium=type(14,20),bodySmall=type(13,18),labelLarge=type(16,24,FontWeight.SemiBold),labelMedium=type(14,20,FontWeight.SemiBold),labelSmall=type(12,16,FontWeight.Medium),
)
private val AbeonaShapes=Shapes(extraSmall=RoundedCornerShape(12.dp),small=RoundedCornerShape(18.dp),medium=RoundedCornerShape(26.dp),large=RoundedCornerShape(28.dp),extraLarge=RoundedCornerShape(32.dp))

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
