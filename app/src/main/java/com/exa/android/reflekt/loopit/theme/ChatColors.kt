package com.exa.android.reflekt.loopit.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Chat-specific color extensions for MaterialTheme
 * Delegates to proper theme tokens so the chat detail screen matches the app theme.
 */

// Helper to determine if theme is light
val ColorScheme.isLight: Boolean
    get() = this.background.red > 0.5f

// ============================================================
// MESSAGE BUBBLE COLORS
// ============================================================

// Sent message bubble — uses theme primary
val ColorScheme.chatSentBubbleStart: Color
    get() = primary

val ColorScheme.chatSentBubbleEnd: Color
    get() = primaryContainer

// Sent message bubble gradient
val ColorScheme.chatSentBubbleGradient: Brush
    get() = Brush.horizontalGradient(
        colors = listOf(chatSentBubbleStart, chatSentBubbleEnd)
    )

// Sent message bubble - Solid color
val ColorScheme.chatSentBubble: Color
    get() = primary

// Received message bubble — uses theme surfaceVariant
val ColorScheme.chatReceivedBubble: Color
    get() = surfaceVariant

// Text color on sent bubble — uses theme onPrimary
val ColorScheme.chatSentText: Color
    get() = onPrimary

// Text color on received bubble — uses theme onSurfaceVariant
val ColorScheme.chatReceivedText: Color
    get() = onSurfaceVariant

// Timestamp color in chat — uses theme onSurfaceVariant
val ColorScheme.chatTimestamp: Color
    get() = onSurfaceVariant

// ============================================================
// STATUS INDICATORS
// ============================================================

// Online indicator (green dot) — semantic status color, kept distinct
val ColorScheme.chatOnlineIndicator: Color
    get() = if (isLight) Color(0xFF22C55E) else Color(0xFF34D399)

// Message read status (double check marks) — uses theme primary
val ColorScheme.chatMessageRead: Color
    get() = primary

// Message sent/delivered status — uses theme outline (muted)
val ColorScheme.chatMessageSent: Color
    get() = outline

// ============================================================
// MEDIA MESSAGE COLORS
// ============================================================

// Image message overlay background
val ColorScheme.chatImageOverlay: Color
    get() = Color(0x80000000) // 50% black overlay

// PDF message background — uses theme errorContainer
val ColorScheme.chatPdfBackground: Color
    get() = errorContainer

// PDF icon color — uses theme error
val ColorScheme.chatPdfIcon: Color
    get() = error

// PDF text color — uses theme onSurface
val ColorScheme.chatPdfText: Color
    get() = onSurface

// Reply message accent line — uses theme primary
val ColorScheme.chatReplyAccent: Color
    get() = primary

// Reply message background — uses theme primaryContainer with alpha
val ColorScheme.chatReplyBackground: Color
    get() = primaryContainer.copy(alpha = 0.3f)

// ============================================================
// DATE SEPARATOR COLORS
// ============================================================

// Date separator background — uses theme surfaceVariant
val ColorScheme.chatDateSeparatorBackground: Color
    get() = surfaceVariant

// Date separator text — uses theme onSurfaceVariant
val ColorScheme.chatDateSeparatorText: Color
    get() = onSurfaceVariant

// ============================================================
// SHIMMER LOADING COLORS
// ============================================================

// Shimmer base color — uses theme surfaceVariant
val ColorScheme.chatShimmerBase: Color
    get() = surfaceVariant

// Shimmer highlight color — uses theme surface
val ColorScheme.chatShimmerHighlight: Color
    get() = surface

// Shimmer gradient
val ColorScheme.chatShimmerGradient: Brush
    get() = Brush.horizontalGradient(
        colors = listOf(chatShimmerBase, chatShimmerHighlight, chatShimmerBase)
    )

// ============================================================
// BACKGROUND COLORS
// ============================================================

// Chat background (for message list) — uses theme background
val ColorScheme.chatBackground: Color
    get() = background

// Input section background — uses theme surface
val ColorScheme.chatInputBackground: Color
    get() = surface
