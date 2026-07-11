package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors

/**
 * Premium Liquid UI Bottom Navigation Bar
 * Features: custom drawn icons, glow on selection, animated FAB, glassmorphic pill
 */
@Composable
fun BottomNavigationBar(
    selectedItem: Int = 0,
    onItemSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val primary = LiquidColors.Primary
    val glassSurface = LiquidColors.GlassSurface
    val glassBorder = LiquidColors.GlassBorder

    // FAB pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val fabPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_scale"
    )
    val fabGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_glow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Glass Pill (4 tabs) ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .shadow(
                        elevation = 20.dp,
                        shape = CircleShape,
                        ambientColor = primary.copy(alpha = 0.15f),
                        spotColor = primary.copy(alpha = 0.15f)
                    )
                    .clip(CircleShape)
                    .background(glassSurface)
            ) {
                // Subtle border using Canvas
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawRoundRect(
                        color = glassBorder,
                        cornerRadius = CornerRadius(size.height / 2f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavItem(
                        index = 0, selectedItem = selectedItem,
                        label = "Home", primary = primary,
                        iconPainter = { color, selected -> DrawHomeIcon(color, selected) },
                        onClick = { onItemSelected(0) }
                    )
                    NavItem(
                        index = 1, selectedItem = selectedItem,
                        label = "Chat", primary = primary,
                        iconPainter = { color, selected -> DrawChatIcon(color, selected) },
                        onClick = { onItemSelected(1) }
                    )
                    NavItem(
                        index = 3, selectedItem = selectedItem,
                        label = "Jobs", primary = primary,
                        iconPainter = { color, selected -> DrawJobsIcon(color, selected) },
                        onClick = { onItemSelected(3) }
                    )
                    NavItem(
                        index = 4, selectedItem = selectedItem,
                        label = "You", primary = primary,
                        iconPainter = { color, selected -> DrawProfileIcon(color, selected) },
                        onClick = { onItemSelected(4) }
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // ── Floating Action Button ───────────────────────────────────
            Box(contentAlignment = Alignment.Center) {
                // Glow halo
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(primary.copy(alpha = fabGlow * 0.25f))
                        .blur(12.dp)
                )
                // Main FAB
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .scale(fabPulse)
                        .shadow(16.dp, CircleShape, ambientColor = primary.copy(alpha = 0.5f), spotColor = primary)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    primary,
                                    Color(0xFF8B5CF6) // purple accent
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(100f, 100f)
                            )
                        )
                        .clickableWithNoIndication { onItemSelected(2) },
                    contentAlignment = Alignment.Center
                ) {
                    // Draw a custom "+" icon via Canvas
                    Canvas(modifier = Modifier.size(22.dp)) {
                        val s = size.minDimension
                        val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        // horizontal bar
                        drawLine(
                            color = Color.White,
                            start = Offset(0f, s / 2f),
                            end = Offset(s, s / 2f),
                            strokeWidth = stroke.width,
                            cap = StrokeCap.Round
                        )
                        // vertical bar
                        drawLine(
                            color = Color.White,
                            start = Offset(s / 2f, 0f),
                            end = Offset(s / 2f, s),
                            strokeWidth = stroke.width,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

// ── Generic nav item shell ──────────────────────────────────────────────────

@Composable
private fun NavItem(
    index: Int,
    selectedItem: Int,
    label: String,
    primary: Color,
    iconPainter: @Composable (color: Color, selected: Boolean) -> Unit,
    onClick: () -> Unit
) {
    val selected = selectedItem == index

    val iconColor by animateColorAsState(
        targetValue = if (selected) primary else LiquidColors.TextMuted,
        animationSpec = tween(300),
        label = "iconColor_$index"
    )

    val bgAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(300),
        label = "bgAlpha_$index"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickableWithNoIndication(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Glow blob behind selected icon
            if (bgAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(primary.copy(alpha = bgAlpha * 0.15f))
                )
            }
            // Canvas icon
            Box(modifier = Modifier.size(26.dp)) {
                iconPainter(iconColor, selected)
            }
        }

        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = iconColor,
            modifier = Modifier.padding(top = 3.dp)
        )

        // Active dot indicator
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .height(3.dp)
                .width(if (selected) 16.dp else 0.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(listOf(primary, Color(0xFF8B5CF6)))
                )
        )
    }
}

// ── Custom icon drawers ────────────────────────────────────────────────────

/** House icon */
@Composable
private fun DrawHomeIcon(color: Color, selected: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = size.minDimension
        val stroke = Stroke(width = if (selected) 2.2.dp.toPx() else 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Roof (triangle path)
        val roofPath = Path().apply {
            moveTo(s * 0.5f, s * 0.05f)
            lineTo(s * 0.97f, s * 0.47f)
            lineTo(s * 0.03f, s * 0.47f)
            close()
        }
        drawPath(roofPath, color = color, style = if (selected) Fill else stroke)
        if (selected) drawPath(roofPath, color = Color.White.copy(alpha = 0.15f), style = stroke)

        // Door & walls
        val wallPath = Path().apply {
            moveTo(s * 0.1f, s * 0.47f)
            lineTo(s * 0.1f, s * 0.97f)
            lineTo(s * 0.9f, s * 0.97f)
            lineTo(s * 0.9f, s * 0.47f)
        }
        drawPath(wallPath, color = color, style = stroke)

        // Door
        val doorPath = Path().apply {
            addRoundRect(
                roundRect = androidx.compose.ui.geometry.RoundRect(
                    left = s * 0.38f, top = s * 0.65f,
                    right = s * 0.62f, bottom = s * 0.97f,
                    topLeftCornerRadius = CornerRadius(s * 0.05f),
                    topRightCornerRadius = CornerRadius(s * 0.05f)
                )
            )
        }
        drawPath(doorPath, color = if (selected) Color.White.copy(alpha = 0.3f) else color.copy(alpha = 0.4f), style = Fill)
        drawPath(doorPath, color = color, style = stroke)
    }
}

/** Chat bubble icon */
@Composable
private fun DrawChatIcon(color: Color, selected: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = size.minDimension
        val stroke = Stroke(width = if (selected) 2.2.dp.toPx() else 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val r = s * 0.18f

        // Main bubble
        val bubblePath = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = s * 0.04f, top = s * 0.04f,
                    right = s * 0.92f, bottom = s * 0.72f,
                    radiusX = r, radiusY = r
                )
            )
        }
        drawPath(bubblePath, color = if (selected) color else Color.Transparent, style = Fill)
        drawPath(bubblePath, color = color, style = stroke)

        // Tail
        val tailPath = Path().apply {
            moveTo(s * 0.2f, s * 0.72f)
            lineTo(s * 0.08f, s * 0.96f)
            lineTo(s * 0.42f, s * 0.72f)
        }
        drawPath(tailPath, color = if (selected) color else Color.Transparent, style = Fill)
        drawPath(tailPath, color = color, style = stroke)

        // Dots inside bubble
        val dotY = s * 0.36f
        val dotR = s * 0.045f
        listOf(s * 0.28f, s * 0.48f, s * 0.68f).forEach { x ->
            drawCircle(
                color = if (selected) Color.White.copy(alpha = 0.8f) else color.copy(alpha = 0.5f),
                radius = dotR,
                center = Offset(x, dotY)
            )
        }
    }
}

/** Briefcase / jobs icon */
@Composable
private fun DrawJobsIcon(color: Color, selected: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = size.minDimension
        val stroke = Stroke(width = if (selected) 2.2.dp.toPx() else 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Case body
        val casePath = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = s * 0.04f, top = s * 0.33f,
                    right = s * 0.96f, bottom = s * 0.92f,
                    radiusX = s * 0.12f, radiusY = s * 0.12f
                )
            )
        }
        drawPath(casePath, color = if (selected) color else Color.Transparent, style = Fill)
        drawPath(casePath, color = color, style = stroke)

        // Handle
        val handlePath = Path().apply {
            moveTo(s * 0.33f, s * 0.33f)
            lineTo(s * 0.33f, s * 0.18f)
            quadraticBezierTo(s * 0.33f, s * 0.06f, s * 0.5f, s * 0.06f)
            quadraticBezierTo(s * 0.67f, s * 0.06f, s * 0.67f, s * 0.18f)
            lineTo(s * 0.67f, s * 0.33f)
        }
        drawPath(handlePath, color = color, style = stroke)

        // Center divider line
        drawLine(
            color = if (selected) Color.White.copy(alpha = 0.4f) else color.copy(alpha = 0.35f),
            start = Offset(s * 0.04f, s * 0.62f),
            end = Offset(s * 0.96f, s * 0.62f),
            strokeWidth = 1.5.dp.toPx()
        )

        // Tie / clip in center
        drawRoundRect(
            color = if (selected) Color.White.copy(alpha = 0.6f) else color.copy(alpha = 0.4f),
            topLeft = Offset(s * 0.41f, s * 0.55f),
            size = Size(s * 0.18f, s * 0.15f),
            cornerRadius = CornerRadius(s * 0.04f),
            style = Fill
        )
    }
}

/** Person / profile icon */
@Composable
private fun DrawProfileIcon(color: Color, selected: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = size.minDimension
        val stroke = Stroke(width = if (selected) 2.2.dp.toPx() else 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Head circle
        val headR = s * 0.2f
        val headCenter = Offset(s * 0.5f, s * 0.28f)
        drawCircle(
            color = if (selected) color else Color.Transparent,
            radius = headR,
            center = headCenter
        )
        drawCircle(
            color = color,
            radius = headR,
            center = headCenter,
            style = stroke
        )

        // Shoulders arc
        val shoulderPath = Path().apply {
            moveTo(s * 0.02f, s * 0.98f)
            quadraticBezierTo(s * 0.02f, s * 0.58f, s * 0.5f, s * 0.58f)
            quadraticBezierTo(s * 0.98f, s * 0.58f, s * 0.98f, s * 0.98f)
        }
        drawPath(shoulderPath, color = if (selected) color.copy(alpha = 0.5f) else Color.Transparent, style = Fill)
        drawPath(shoulderPath, color = color, style = stroke)
    }
}
