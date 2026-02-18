package com.exa.android.reflekt.loopit.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Chat-specific color extensions for MaterialTheme
 * Provides consistent colors for chat bubbles, indicators, media elements, etc.
 * Supports both light and dark themes with modern gradients and shimmer effects
 */

// Helper to determine if theme is light
val ColorScheme.isLight: Boolean
    get() = this.background.red > 0.5f

// ============================================================
// MESSAGE BUBBLE COLORS
// ============================================================

// Sent message bubble - Modern gradient colors
val ColorScheme.chatSentBubbleStart: Color
    get() = if (isLight) Color(0xFF6C63FF) else Color(0xFF8B7FFF)

val ColorScheme.chatSentBubbleEnd: Color
    get() = if (isLight) Color(0xFF5548E8) else Color(0xFF7C73FF)

// Sent message bubble gradient
val ColorScheme.chatSentBubbleGradient: Brush
    get() = Brush.horizontalGradient(
        colors = listOf(chatSentBubbleStart, chatSentBubbleEnd)
    )

// Sent message bubble - Solid color (for simple implementation)
val ColorScheme.chatSentBubble: Color
    get() = chatSentBubbleStart

// Received message bubble color
val ColorScheme.chatReceivedBubble: Color
    get() = if (isLight) Color(0xFFF1F3F5) else Color(0xFF2C2C2E)

// Text color on sent bubble
val ColorScheme.chatSentText: Color
    get() = Color.White // Always white on primary color

// Text color on received bubble
val ColorScheme.chatReceivedText: Color
    get() = if (isLight) Color(0xFF212529) else Color(0xFFF8F9FA)

// Timestamp color in chat
val ColorScheme.chatTimestamp: Color
    get() = if (isLight) Color(0xFF6C757D) else Color(0xFFADB5BD)

// ============================================================
// STATUS INDICATORS
// ============================================================

// Online indicator (green dot)
val ColorScheme.chatOnlineIndicator: Color
    get() = if (isLight) Color(0xFF00D9A3) else Color(0xFF00F5C4)

// Message read status (double check marks)
val ColorScheme.chatMessageRead: Color
    get() = if (isLight) Color(0xFF00D9A3) else Color(0xFF00F5C4)

// Message sent status (single check mark)
val ColorScheme.chatMessageSent: Color
    get() = if (isLight) Color(0xFFB0B0B0) else Color(0xFF6E767D)

// ============================================================
// MEDIA MESSAGE COLORS
// ============================================================

// Image message overlay background
val ColorScheme.chatImageOverlay: Color
    get() = Color(0x80000000) // 50% black overlay

// PDF message background
val ColorScheme.chatPdfBackground: Color
    get() = if (isLight) Color(0xFFFFEBEE) else Color(0xFF3A1F1F)

// PDF icon color
val ColorScheme.chatPdfIcon: Color
    get() = if (isLight) Color(0xFFE53935) else Color(0xFFFF6B6B)

// PDF text color
val ColorScheme.chatPdfText: Color
    get() = if (isLight) Color(0xFF212529) else Color(0xFFE7E9EA)

// Reply message accent line
val ColorScheme.chatReplyAccent: Color
    get() = if (isLight) Color(0xFF6C63FF) else Color(0xFF8B7FFF)

// Reply message background
val ColorScheme.chatReplyBackground: Color
    get() = if (isLight) Color(0xFFE8E6FF) else Color(0xFF2A2840)

// ============================================================
// DATE SEPARATOR COLORS
// ============================================================

// Date separator background
val ColorScheme.chatDateSeparatorBackground: Color
    get() = if (isLight) Color(0xFFE0E0E0) else Color(0xFF2C2C2E)

// Date separator text
val ColorScheme.chatDateSeparatorText: Color
    get() = if (isLight) Color(0xFF536471) else Color(0xFF8B98A5)

// ============================================================
// SHIMMER LOADING COLORS
// ============================================================

// Shimmer base color
val ColorScheme.chatShimmerBase: Color
    get() = if (isLight) Color(0xFFE0E0E0) else Color(0xFF2C2C2E)

// Shimmer highlight color
val ColorScheme.chatShimmerHighlight: Color
    get() = if (isLight) Color(0xFFF5F5F5) else Color(0xFF3A3A3C)

// Shimmer gradient
val ColorScheme.chatShimmerGradient: Brush
    get() = Brush.horizontalGradient(
        colors = listOf(
            chatShimmerBase,
            chatShimmerHighlight,
            chatShimmerBase
        )
    )

// ============================================================
// BACKGROUND COLORS
// ============================================================

// Chat background (for message list)
val ColorScheme.chatBackground: Color
    get() = if (isLight) Color(0xFFF8F9FA) else Color(0xFF0E0E10)

// Input section background
val ColorScheme.chatInputBackground: Color
    get() = if (isLight) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
