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
import com.google.accompanist.systemuicontroller.rememberSystemUiController

// Light Color Scheme (Updated)
private val LightColorScheme = lightColorScheme(
  primary = Color(0xFF4875E1),
  onPrimary = Color.White,
  secondary = Color.White,
  onSecondary = Color(0xFF4875E1),
  tertiary = Color.White,
  onTertiary = Color.Black,
  background = Color(0xFFF8F8F8),          // White clean UI background
  surface = Color(0xFFF8F8F8),       // Light gray for chat backgrounds
  onSurface = Color.Black,     // Dark gray for text
  error = Color(0xFFE57373),         // Soft Red (for errors)
  primaryContainer = Color(0xFF4875E1),
  onPrimaryContainer = Color.White,
  surfaceVariant = Color(0xFFF8F8F8),
  onSurfaceVariant = Color(0xFF4875E1),
  secondaryContainer = Color(0xFF4875E1).copy(alpha = 0.2f), // Container for secondary
  onSecondaryContainer = Color(0xFF4875E1),
)

// Dark Color Scheme (Updated)
private val DarkColorScheme = darkColorScheme(
  primary = Color(0xFF4875E1),
  onPrimary = Color.White,
  secondary = Color.White,
  onSecondary = Color.Black,
  tertiary = Color.Black,
  onTertiary = Color.White,
  background = Color(0xFF121212),
  surface = Color(0xFF242424),
  onSurface = Color.White,
  onBackground = Color(0xFFE0E0E0),
  error = Color(0xFFEF5350),
  primaryContainer = Color(0xFF4875E1),
  secondaryContainer = Color(0xFFBDBDBD)
)

// Extended Colors
object AppColors {
  val NotificationBadge = Color(0xFFFFD700) // Gold for notification bubbles
  val DividerColor = Color(0xFFE0E0E0) // Light gray for dividers
  val cardButtonColor1 = Color.White
  val cardButtonColor2 = Color.Black
  val whiteBlue = Color(0xFFF8F8F8)
}

// Typography Setup
private val AppTypography = Typography(
  displayLarge = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    letterSpacing = 0.5.sp,
    color = Color.Black
  ),
  titleLarge = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    color = Color.Black
  ),
  bodyLarge = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    color = Color.Black
  ),
  labelSmall = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    color = Color.Black
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
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {

  val colorScheme = when {
    /*
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
     */
    darkTheme -> LightColorScheme
    else -> LightColorScheme
  }


  // Remember System UI Controller
  val systemUiController = rememberSystemUiController()

  // Set the status bar color based on the theme
  SideEffect {
    systemUiController.setStatusBarColor(
      color = AppColors.whiteBlue,
      darkIcons = true
    )
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = AppTypography,
    shapes = AppShapes,
    content = content
  )
}