package com.exa.android.reflekt.loopit.theme
import androidx.compose.ui.graphics.Color

// ── Light theme — aligned with LiquidUI ──────────────────────────────────────
val PrimaryLight          = Color(0xFF13B6EC)  // LiquidColors.Primary
val PrimaryVariantLight   = Color(0xFFE0F5FD)  // light container (sky tint)
val SecondaryLight        = Color(0xFF475569)  // LiquidColors.TextSecondary light
val SecondaryVariantLight = Color(0xFFE2EBF5)  // soft secondary container

val BackgroundLight       = Color(0xFFF1F5F9)  // LiquidColors.BgBase light (slate-100)
val SurfaceLight          = Color(0xFFFFFFFF)  // white cards
val SurfaceVariantLight   = Color(0xFFF8FAFC)  // off-white for elevated elements

val ErrorLight            = Color(0xFFEF4444)

val OnPrimaryLight        = Color(0xFFFFFFFF)
val OnSecondaryLight      = Color(0xFFFFFFFF)
val OnBackgroundLight     = Color(0xFF0F172A)  // LiquidColors.TextPrimary light
val OnSurfaceLight        = Color(0xFF0F172A)
val OnErrorLight          = Color(0xFFFFFFFF)

// LIGHT TEXT SYSTEM (aligned with LiquidColors)
val TextPrimaryLight      = Color(0xFF0F172A)  // LiquidColors.TextPrimary light
val TextSecondaryLight    = Color(0xFF475569)  // LiquidColors.TextSecondary light
val TextTertiaryLight     = Color(0xFF94A3B8)  // LiquidColors.TextMuted light

val DividerLight          = Color(0xFFCBD5E1)  // slate-300 — glass border feel
val OutlineVariantLight   = Color(0xFFE2E8F0)  // slate-200 — subtle
val CardBackgroundLight   = Color(0xFFFFFFFF)
val IconTintLight         = Color(0xFF475569)  // matches TextSecondary

val AccentLight           = Color(0xFF22C55E)


//DARK THEME
val PrimaryDark = Color(0xFF13B6EC)
val PrimaryVariantDark = Color(0xFF197A97)
val SecondaryDark = Color(0xFF2EAED6)
val SecondaryVariantDark = Color(0xFF2499BE)

val BackgroundDark = Color(0xFF101D22)
val SurfaceDark = Color(0xFF16262C)
val SurfaceVariantDark = Color(0xFF1A2F3A)

val ErrorDark = Color(0xFFFF6B6B)

val OnPrimaryDark = Color(0xFFFFFFFF)
val OnSecondaryDark = Color(0xFFFFFFFF)
val OnBackgroundDark = Color(0xFFF8FAFC)
val OnSurfaceDark = Color(0xFFF8FAFC)
val OnErrorDark = Color(0xFF000000)

// DARK TEXT SYSTEM (aligned with EventDetailScreen)
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF9CA3AF)
val TextTertiaryDark = Color(0xFF6B7280)

val DividerDark = Color(0xFF233840)
val CardBackgroundDark = Color(0x6616262C)
val IconTintDark = Color(0xFFD1D5DB)

val AccentDark = Color(0xFF34D399)

// 🌈 PRIMARY CTA GRADIENT (matches button)
val GradientPrimaryStart = Color(0xFF197A97)
val GradientPrimaryMid = Color(0xFF13B6EC)
val GradientPrimaryEnd = Color(0xFF2EAED6)

// Subtle surface gradient
val GradientSoftStart = Color(0xFFF1F5F9)
val GradientSoftEnd = Color(0xFFE2E8F0)

// Dark header gradient
val HeaderGradientStart = Color(0xFF16262C)
val HeaderGradientEnd = Color(0xFF101D22)

// 💬 SOCIAL / STATUS
val LikeColor = Color(0xFFEF4444)
val LikeColorActive = Color(0xFFDC2626)

val CommentColor = PrimaryLight
val CommentColorActive = PrimaryVariantLight

val ShareColor = Color(0xFF22C55E)
val ShareColorActive = Color(0xFF16A34A)

// ✅ UTILITY
val SuccessColor = Color(0xFF22C55E)
val WarningColor = Color(0xFFF59E0B)
val InfoColor = PrimaryLight

// ✨ GLASS
val GlassLight = Color(0xCCFFFFFF)
val GlassDark = Color(0xCC16262C)


/*
// Light Theme Colors - Vibrant and Modern
val PrimaryLight = Color(0xFF6C63FF)
val PrimaryVariantLight = Color(0xFF5548E8)
val SecondaryLight = Color(0xFFFF6584)
val SecondaryVariantLight = Color(0xFFE85470)
val BackgroundLight = Color(0xFFF5F7FA)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF8F9FB)
val ErrorLight = Color(0xFFFF5252)
val OnPrimaryLight = Color(0xFFFFFFFF)
val OnSecondaryLight = Color(0xFFFFFFFF)
val OnBackgroundLight = Color(0xFF0F1419)
val OnSurfaceLight = Color(0xFF0F1419)
val OnErrorLight = Color(0xFFFFFFFF)

// Additional Light Theme Colors
val TextPrimaryLight = Color(0xFF0F1419)
val TextSecondaryLight = Color(0xFF536471)
val TextTertiaryLight = Color(0xFF8B98A5)
val DividerLight = Color(0xFFEFF3F4)
val CardBackgroundLight = Color(0xFFFFFFFF)
val IconTintLight = Color(0xFF536471)
val AccentLight = Color(0xFF00D9A3)

// Dark Theme Colors - Deep and Rich
val PrimaryDark = Color(0xFF8B7FFF)
val PrimaryVariantDark = Color(0xFF7C73FF)
val SecondaryDark = Color(0xFFFF7A96)
val SecondaryVariantDark = Color(0xFFFF6584)
val BackgroundDark = Color(0xFF0A0A0A)  // Deeper black
val SurfaceDark = Color(0xFF121212)     // Rich surface
val SurfaceVariantDark = Color(0xFF1A1A1A)  // Card background
val ErrorDark = Color(0xFFFF6B6B)
val OnPrimaryDark = Color(0xFFFFFFFF)
val OnSecondaryDark = Color(0xFFFFFFFF)
val OnBackgroundDark = Color(0xFFE7E9EA)
val OnSurfaceDark = Color(0xFFE7E9EA)
val OnErrorDark = Color(0xFF000000)

// Additional Dark Theme Colors
val TextPrimaryDark = Color(0xFFE7E9EA)
val TextSecondaryDark = Color(0xFF8B98A5)
val TextTertiaryDark = Color(0xFF6E767D)
val DividerDark = Color(0xFF2F3336)
val CardBackgroundDark = Color(0xFF16181C)  // Elevated card
val IconTintDark = Color(0xFF8B98A5)
val AccentDark = Color(0xFF00F5C4)

// Primary Gradient Colors - Purple/Blue Theme
val GradientPurpleStart = Color(0xFF6C63FF)
val GradientPurpleMid = Color(0xFF8B7FFF)
val GradientPurpleEnd = Color(0xFFB4A9FF)

// Secondary Gradient Colors - Pink/Red Theme
val GradientPinkStart = Color(0xFFFF6584)
val GradientPinkMid = Color(0xFFFF7A96)
val GradientPinkEnd = Color(0xFFFFB3C1)

// Accent Gradient Colors - Teal/Green Theme
val GradientTealStart = Color(0xFF00D9A3)
val GradientTealMid = Color(0xFF00F5C4)
val GradientTealEnd = Color(0xFF7FFFD4)

// Vibrant Gradient Colors - Multi-color Theme
val GradientVibeStart = Color(0xFF6C63FF)
val GradientVibeMid = Color(0xFFFF6584)
val GradientVibeEnd = Color(0xFF00D9A3)

// Header Gradient - Glassmorphism Effect
val HeaderGradientStart = Color(0xFF6C63FF)
val HeaderGradientEnd = Color(0xFF8B7FFF)

// Tag Gradient Colors
val TagGradient1Start = Color(0xFF6C63FF)
val TagGradient1End = Color(0xFF8B7FFF)

val TagGradient2Start = Color(0xFFFF6584)
val TagGradient2End = Color(0xFFFF7A96)

val TagGradient3Start = Color(0xFF00D9A3)
val TagGradient3End = Color(0xFF00F5C4)

// Social Interaction Colors - More Vibrant
val LikeColor = Color(0xFFFF6584)
val LikeColorActive = Color(0xFFFF3D6B)
val CommentColor = Color(0xFF6C63FF)
val CommentColorActive = Color(0xFF5548E8)
val ShareColor = Color(0xFF00D9A3)
val ShareColorActive = Color(0xFF00C28F)

// Utility Colors
val SuccessColor = Color(0xFF00D9A3)
val WarningColor = Color(0xFFFFB020)
val InfoColor = Color(0xFF6C63FF)

// Glassmorphism Colors
val GlassLight = Color(0xCCFFFFFF)  // 80% opacity white
val GlassDark = Color(0xCC121212)   // 80% opacity dark
 */