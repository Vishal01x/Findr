package io.getstream.meeting.room.compose.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//// Light Color Scheme (Updated)
//private val LightColorScheme = lightColorScheme(
//  primary = Color(0xFF4875E1),
//  onPrimary = Color.White,
//  secondary = Color.White,
//  onSecondary = Color(0xFF4875E1),
//  tertiary = Color.White,
//  onTertiary = Color.Black,
//  background = Color(0xFFF8F8F8),          // White clean UI background
//  surface = Color(0xFFF8F8F8),       // Light gray for chat backgrounds
//  //onSurface = Color(0xFF212121),     // Dark gray for text
//  onSurface = Color.Black,
//  error = Color(0xFFE57373),         // Soft Red (for errors)
//  primaryContainer = Color(0xFF4875E1),
//  onPrimaryContainer = Color.White,
//  surfaceVariant = Color(0xFFF8F8F8),
//  onSurfaceVariant = Color(0xFF4875E1),
//  secondaryContainer = Color(0xFF4875E1).copy(alpha = 0.2f), // Container for secondary
//  onSecondaryContainer = Color(0xFF4875E1),
//)
//
//// Dark Color Scheme (Updated)
//private val DarkColorScheme = darkColorScheme(
//  primary = Color(0xFF4875E1),
//  onPrimary = Color.White,
//  secondary = Color.White,
//  onSecondary = Color.Black,
//  tertiary = Color.Black,
//  onTertiary = Color.White,
//  background = Color(0xFF121212),
//  surface = Color(0xFF242424),
//  onSurface = Color.White,
//  onBackground = Color(0xFFE0E0E0),
//  error = Color(0xFFEF5350),
//  primaryContainer = Color(0xFF242424),
//  onPrimaryContainer = Color.White,
//  secondaryContainer = Color(0xFF4875E1).copy(alpha = 0.2f), // Container for secondary
//  onSecondaryContainer = Color(0xFF4875E1),
//)
//
//// Extended Colors
//object AppColors {
//  val NotificationBadge = Color(0xFFFFD700) // Gold for notification bubbles
//  val DividerColor = Color(0xFFE0E0E0) // Light gray for dividers
//  val cardButtonColor1 = Color.White
//  val cardButtonColor2 = Color.Black
//  val whiteBlue = Color(0xFFF8F8F8)
//}

private val LightColorScheme = lightColorScheme(

    primary = Color(0xFF1FB6FF),
    onPrimary = Color.White,

    background = Color(0xFFF3F6F8),
    onBackground = Color(0xFF0F172A),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),

    surfaceVariant = Color(0xFFE9EEF2),
    onSurfaceVariant = Color(0xFF64748B),

    secondary = Color(0xFFE9EEF2),
    onSecondary = Color(0xFF0F172A),

    primaryContainer = Color(0xFF0EA5E9),
    onPrimaryContainer = Color.White,

    secondaryContainer = Color(0xFF1FB6FF).copy(alpha = 0.1f),
    onSecondaryContainer = Color(0xFF1FB6FF),

    error = Color(0xFFDC2626),
    onError = Color.White,
)


private val DarkColorScheme = darkColorScheme(

    primary = Color(0xFF1FB6FF),
    onPrimary = Color.White,

    background = Color(0xFF0B1E24),
    onBackground = Color(0xFFE6F1F5),

    surface = Color(0xFF102A33),
    onSurface = Color(0xFFE6F1F5),

    surfaceVariant = Color(0xFF143642),
    onSurfaceVariant = Color(0xFF8FA3AD),

    secondary = Color(0xFF143642),
    onSecondary = Color.White,

    primaryContainer = Color(0xFF0EA5E9),
    onPrimaryContainer = Color.White,

    secondaryContainer = Color(0xFF1E4A57),
    onSecondaryContainer = Color.White,

    error = Color(0xFFEF4444),
    onError = Color.White,
)

object AppColors {

    // Accent Variants
    val PrimaryDark = Color(0xFF0EA5E9)
    val PrimaryLight = Color(0xFF38BDF8)

    // Semantic
    val Success = Color(0xFF22C55E)
    val Warning = Color(0xFFFACC15)
    val ErrorRed = Color(0xFFEF4444)
    val EventPurple = Color(0xFFA855F7)
    val BugRed = Color(0xFFDC2626)

    // Notification
    val NotificationBadge = Color(0xFFFACC15)

    // Dividers
    val DividerLight = Color(0xFFD8E1E8)
    val DividerDark = Color(0xFF1E4A57)

    // Dark Cards
    val DarkCard = Color(0xFF102A33)
    val DarkElevatedCard = Color(0xFF143642)

    // Light Containers
    val LightSoftContainer = Color(0xFFE9EEF2)
}


// Typography Setup
private val AppTypography = Typography(
  displayLarge = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    letterSpacing = 0.5.sp
  ),
  titleLarge = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    color = Color(0xFF333333)
  ),
  bodyLarge = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp
  ),
  labelSmall = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    color = Color(0xFF616161)
  )
)

// Shapes for Components
private val AppShapes = Shapes(
  small = RoundedCornerShape(4.dp),
  medium = RoundedCornerShape(8.dp),
  large = RoundedCornerShape(16.dp)
)

@Composable
fun AppTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit
) {
  val colorScheme = when {
    /*
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }

     */
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }


//  val useDarkIcons = true // dark icons = black icons
//  val statusBarColor = AppColors.whiteBlue

//  SideEffect {
//    systemUiController.setStatusBarColor(
//      color = statusBarColor,
//      darkIcons = useDarkIcons
//    )
//  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = AppTypography,
    shapes = AppShapes,
    content = content
  )
}