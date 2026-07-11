package com.exa.android.reflekt.loopit.presentation.main.placement.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Shared Liquid UI Tokens
object LiquidColors {
    val isDark: Boolean
        @Composable get() = androidx.compose.foundation.isSystemInDarkTheme()

    val BgBase: Color
        @Composable get() = if (isDark) Color(0xFF101D22) else Color(0xFFF1F5F9)
        
    val MeshCyan: Color 
        @Composable get() = if (isDark) Color(0xFF13B6EC).copy(alpha = 0.25f) else Color(0xFF13B6EC).copy(alpha = 0.35f)
        
    val MeshPurple: Color 
        @Composable get() = if (isDark) Color(0xFF8B5CF6).copy(alpha = 0.20f) else Color(0xFF8B5CF6).copy(alpha = 0.30f)
        
    val MeshBlue: Color 
        @Composable get() = if (isDark) Color(0xFF3B82F6).copy(alpha = 0.25f) else Color(0xFF3B82F6).copy(alpha = 0.35f)
    
    val GlassSurface: Color 
        @Composable get() = if (isDark) Color(0xFFFFFFFF).copy(alpha = 0.03f) else Color(0xFFFFFFFF).copy(alpha = 0.6f)
        
    val GlassBorder: Color 
        @Composable get() = if (isDark) Color(0xFFFFFFFF).copy(alpha = 0.1f) else Color(0xFFFFFFFF).copy(alpha = 0.6f)
        
    val GlassHighlight: Color 
        @Composable get() = if (isDark) Color(0xFFFFFFFF).copy(alpha = 0.2f) else Color(0xFFFFFFFF).copy(alpha = 0.8f)
    
    val Primary: Color 
        @Composable get() = Color(0xFF13B6EC)
        
    val Amber: Color 
        @Composable get() = if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706)
        
    val Red: Color 
        @Composable get() = if (isDark) Color(0xFFEF4444) else Color(0xFFDC2626)
        
    val TextPrimary: Color 
        @Composable get() = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
        
    val TextSecondary: Color
        @Composable get() = if (isDark) Color.White else Color.Black
        
    val TextMuted: Color
        @Composable get() = if (isDark) Color(0xFF6B7280) else Color(0xFF94A3B8)
        
    val Green: Color 
        @Composable get() = if (isDark) Color(0xFF10B981) else Color(0xFF059669)

    // Additional adaptive tokens from EventDetail
    val Surface: Color
        @Composable get() = if (isDark) Color(0xFF16262C) else Color(0xFFFFFFFF)

    val Border: Color
        @Composable get() = if (isDark) Color(0xFF233840) else Color(0xFFE2E8F0)

    val UtilityLabel: Color
        @Composable get() = if (isDark) Color(0xFFD1D5DB) else Color(0xFF475569)

    val MapPlaceholder: Color
        @Composable get() = if (isDark) Color(0xFF1F3540) else Color(0xFFDCEFF9)

    val ProgressTrack: Color
        @Composable get() = if (isDark) Color(0xFF374151) else Color(0xFFE2E8F0)

    val AvatarExtra: Color
        @Composable get() = if (isDark) Color(0xFF374151) else Color(0xFFCBD5E1)
}

@Composable
fun LiquidMeshBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val xOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse),
        label = "xOffset"
    )
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Reverse),
        label = "yOffset"
    )

    val meshCyan = LiquidColors.MeshCyan
    val meshPurple = LiquidColors.MeshPurple
    val meshBlue = LiquidColors.MeshBlue

    Canvas(modifier = Modifier.fillMaxSize().blur(100.dp)) {
        val width = size.width
        val height = size.height

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(meshCyan, Color.Transparent),
                center = Offset(width * (0.2f + 0.6f * xOffset), height * (0.1f + 0.3f * yOffset)),
                radius = width * 0.8f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(meshPurple, Color.Transparent),
                center = Offset(width * (0.8f - 0.4f * yOffset), height * (0.4f + 0.4f * xOffset)),
                radius = width * 0.7f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(meshBlue, Color.Transparent),
                center = Offset(width * (0.5f + 0.3f * yOffset), height * (0.8f - 0.2f * xOffset)),
                radius = width * 0.9f
            )
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(32.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(LiquidColors.GlassSurface)
            .border(1.dp, LiquidColors.GlassBorder, shape)
    ) {
        content()
    }
}

// ─── Reusable glass top app bar ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun liquidTopBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor        = LiquidColors.GlassSurface,
    scrolledContainerColor = LiquidColors.GlassSurface,
    titleContentColor     = LiquidColors.TextPrimary,
    navigationIconContentColor = LiquidColors.TextPrimary,
    actionIconContentColor = LiquidColors.TextPrimary,
)

/** Drop-in replacement for TopAppBar with the Liquid glass style */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidTopBar(
    title: String,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(LiquidColors.GlassSurface)
            .border(
                width = 0.5.dp,
                color = LiquidColors.GlassBorder,
                shape = RoundedCornerShape(0.dp)
            )
    ) {
        TopAppBar(
            title = {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = LiquidColors.TextPrimary
                )
            },
            navigationIcon = navigationIcon,
            actions = actions,
            colors = liquidTopBarColors(),
            scrollBehavior = scrollBehavior
        )
    }
}
