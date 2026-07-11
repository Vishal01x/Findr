package com.exa.android.reflekt.loopit.presentation.main.placement.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.reflekt.loopit.presentation.main.placement.model.*

// ══════════════════════════════════════════════════════════════════════════════
// ── Design Tokens — exact Stitch spec, dual-mode
// ── Dark  : background-dark #101D22 / surface-dark #18272E / primary #13B6EC
// ── Light : background-light #F6F8F8 / surface white / primary #13B6EC
// ══════════════════════════════════════════════════════════════════════════════
object PlacementColors {
    val Background      @Composable get() = LiquidColors.BgBase
    val Surface         @Composable get() = LiquidColors.GlassSurface
    val SurfaceElevated @Composable get() = LiquidColors.GlassHighlight
    val Border          @Composable get() = LiquidColors.GlassBorder
    val Primary         @Composable get() = LiquidColors.Primary
    val PrimaryDim      @Composable get() = LiquidColors.Primary.copy(alpha = 0.10f)
    val Success         @Composable get() = LiquidColors.Green
    val SuccessDim      @Composable get() = LiquidColors.Green.copy(alpha = 0.15f)
    val Warning         @Composable get() = LiquidColors.Amber
    val TextPrimary     @Composable get() = LiquidColors.TextPrimary
    val TextSecondary   @Composable get() = LiquidColors.TextSecondary
    val TextMuted       @Composable get() = LiquidColors.TextMuted
    val HiringGreen     @Composable get() = LiquidColors.Green
    val TabSelected     @Composable get() = LiquidColors.GlassHighlight
    val TabUnselected   @Composable get() = LiquidColors.TextSecondary
    val CurrentStageBg  @Composable get() = LiquidColors.GlassSurface
    val CurrentStageBorder @Composable get() = LiquidColors.Primary.copy(alpha = 0.3f)
    val ClearedColor    @Composable get() = LiquidColors.Green
    val UpcomingColor   @Composable get() = LiquidColors.TextMuted
    val CardStroke      @Composable get() = LiquidColors.GlassBorder
    val Divider         @Composable get() = LiquidColors.GlassBorder

    @Composable fun bg(dark: Boolean)           = Background
    @Composable fun surface(dark: Boolean)      = Surface
    @Composable fun surface2(dark: Boolean)     = SurfaceElevated
    @Composable fun border(dark: Boolean)       = Border
    @Composable fun border2(dark: Boolean)      = Border
    @Composable fun textPrimary(dark: Boolean)  = TextPrimary
    @Composable fun textSecond(dark: Boolean)   = TextSecondary
    @Composable fun textMuted(dark: Boolean)    = TextMuted
    @Composable fun tabSelected(dark: Boolean)  = TabSelected
    @Composable fun tabBg(dark: Boolean)        = Surface
    @Composable fun divider(dark: Boolean)      = Divider
}

// ── Hiring Badge ───────────────────────────────────────────────────────────────
@Composable
fun HiringBadge(modifier: Modifier = Modifier, isDark: Boolean = true) {
    val bg = PlacementColors.Background
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = PlacementColors.HiringGreen,
        border = BorderStroke(2.dp, bg)
    ) {
        Text(
            text = "HIRING",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

// ── Stat Pill ──────────────────────────────────────────────────────────────────
@Composable
fun StatPill(
    label: String,
    icon: ImageVector? = null,
    color: Color = PlacementColors.Primary,
    isPrimary: Boolean = false,
    isDark: Boolean = true
) {
    val bgColor     = if (isPrimary) color.copy(alpha = 0.1f) else PlacementColors.surface(isDark)
    val textColor   = if (isPrimary) color else PlacementColors.textPrimary(isDark)
    val borderColor = if (isPrimary) color.copy(alpha = 0.2f) else PlacementColors.border(isDark)
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            icon?.let {
                Icon(it, null, tint = textColor, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(label, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ── Preparation Progress Card ──────────────────────────────────────────────────
@Composable
fun PreparationCard(
    percent: Int,
    skillsMatched: Int,
    totalSkills: Int,
    onViewTasks: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    val animProg by animateFloatAsState(
        targetValue = percent / 100f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "prep_progress"
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PlacementColors.surface2(isDark),
        border = BorderStroke(1.dp, PlacementColors.border(isDark)),
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "MY PREPARATION LEVEL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = PlacementColors.textMuted(isDark),
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$percent% Ready",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PlacementColors.textPrimary(isDark)
                )
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.FlashOn, null, tint = PlacementColors.Warning, modifier = Modifier.size(16.dp))
                Spacer(Modifier.weight(1f))
                Text(
                    "View Tasks",
                    fontSize = 12.sp,
                    color = PlacementColors.Primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickableWithNoIndication(onClick = onViewTasks)
                        .padding(4.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(PlacementColors.border(isDark))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animProg)
                        .clip(CircleShape)
                        .background(PlacementColors.Primary)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "You're matching $skillsMatched/$totalSkills key skills required for this role.",
                fontSize = 11.sp,
                color = PlacementColors.textSecond(isDark)
            )
        }
    }
}

// ── Segmented Tab Row ──────────────────────────────────────────────────────────
@Composable
fun CompanyTabRow(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = PlacementColors.tabBg(isDark),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(Modifier.padding(4.dp)) {
            tabs.forEachIndexed { idx, tab ->
                val sel = idx == selected
                val bg      = if (sel) PlacementColors.tabSelected(isDark) else Color.Transparent
                val textCol = if (sel) {
                    if (isDark) Color.White else PlacementColors.Primary
                } else {
                    PlacementColors.textSecond(isDark)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .then(
                            if (sel && !isDark) Modifier.border(
                                1.dp,
                                PlacementColors.Border,
                                RoundedCornerShape(8.dp)
                            ) else Modifier
                        )
                        .clickable { onSelect(idx) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        tab,
                        fontSize = 13.sp,
                        fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                        color = textCol
                    )
                }
            }
        }
    }
}

// ── Recruitment Stage Item ─────────────────────────────────────────────────────
@Composable
fun StageItem(stage: RecruitmentStage, showConnector: Boolean, isDark: Boolean = true) {
    val isCurrent  = stage.status == StageStatus.CURRENT
    val isCleared  = stage.status == StageStatus.CLEARED
    val dotColor   = when (stage.status) {
        StageStatus.CLEARED  -> PlacementColors.ClearedColor
        StageStatus.CURRENT  -> PlacementColors.Primary
        StageStatus.UPCOMING -> if (isDark) PlacementColors.UpcomingColor else Color(0xFFCBD5E1)
    }
    // timeline dot bg — in light: white with primary border; dark: surface
    val dotBg = if (isCurrent || isCleared) {
        PlacementColors.Surface
    } else {
        if (isDark) PlacementColors.SurfaceElevated else Color(0xFFF1F5F9)
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(dotBg)
                    .border(2.dp, dotColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isCleared) {
                    Icon(Icons.Default.Check, null, tint = dotColor, modifier = Modifier.size(14.dp))
                } else {
                    Text("${stage.order}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dotColor)
                }
            }
            if (showConnector) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(if (stage.description.isNotEmpty()) 72.dp else 36.dp)
                        .background(PlacementColors.divider(isDark))
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        val cardBg = if (isCurrent) {
            if (isDark) PlacementColors.Surface else Color(0xFFF0F9FF)
        } else {
            PlacementColors.surface(isDark)
        }
        val cardBorder = if (isCurrent) {
            PlacementColors.CurrentStageBorder
        } else {
            PlacementColors.border(isDark)
        }
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder),
            shadowElevation = 0.dp
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stage.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PlacementColors.textPrimary(isDark),
                        modifier = Modifier.weight(1f)
                    )
                    if (isCurrent) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PlacementColors.Primary
                        ) {
                            Text(
                                "Current",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (isCleared && stage.date.isNotEmpty()) {
                        Text(stage.date, fontSize = 10.sp, color = PlacementColors.textMuted(isDark))
                    }
                }
                if (stage.status == StageStatus.CLEARED) {
                    Spacer(Modifier.height(4.dp))
                    Text("Status: Cleared", fontSize = 11.sp, color = PlacementColors.ClearedColor, fontWeight = FontWeight.Medium)
                }
                if (stage.description.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    if (stage.platform.isNotEmpty()) {
                        Text("Platform: ${stage.platform}", fontSize = 11.sp, color = PlacementColors.textSecond(isDark))
                        Spacer(Modifier.height(2.dp))
                    }
                    Text(stage.description, fontSize = 12.sp, color = PlacementColors.textSecond(isDark), lineHeight = 17.sp)
                }
                if (stage.durationMins > 0 || stage.tags.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (stage.durationMins > 0) {
                            val chipBg = if (isDark) PlacementColors.SurfaceElevated else Color(0xFFF1F5F9)
                            Surface(shape = RoundedCornerShape(4.dp), color = chipBg) {
                                Text(
                                    "${stage.durationMins} Mins",
                                    fontSize = 10.sp,
                                    color = PlacementColors.textSecond(isDark),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                        stage.tags.forEach { tag ->
                            val chipBg = if (isDark) PlacementColors.PrimaryDim else Color(0xFFE0F2FE)
                            Surface(shape = RoundedCornerShape(4.dp), color = chipBg) {
                                Text(
                                    tag,
                                    fontSize = 10.sp,
                                    color = PlacementColors.Primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (showConnector) Spacer(Modifier.height(8.dp))
}

// ── Eligibility Grid (CGPA + Backlog) ─────────────────────────────────────────
@Composable
fun CgpaBacklogRow(
    cgpa: String,
    yourCgpa: String,
    backlogs: Int,
    allowedBacklogs: Int,
    isDark: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(
            Triple("CGPA CUTOFF", cgpa, yourCgpa.let { if (it.isNotEmpty()) "Your CGPA: $it" else "" }),
            Triple("BACKLOGS", if (backlogs == 0) "None" else "$backlogs", "Active Allowed: $allowedBacklogs")
        ).forEach { (label, value, sub) ->
            val cardBg = if (isDark) PlacementColors.Surface
                         else PlacementColors.Primary.copy(alpha = 0.05f)
            val cardBorder = if (isDark) PlacementColors.CardStroke
                              else PlacementColors.Primary.copy(alpha = 0.1f)
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                shadowElevation = 0.dp
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(label, fontSize = 9.sp, color = PlacementColors.textMuted(isDark), letterSpacing = 0.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) PlacementColors.TextPrimary else PlacementColors.Primary)
                    if (sub.isNotEmpty())
                        Text(sub, fontSize = 10.sp, color = PlacementColors.textMuted(isDark))
                }
            }
        }
    }
}

// ── Section Header with left accent bar (light mode) ─────────────────────────
@Composable
fun SectionHeader(
    title: String,
    trailingText: String = "",
    isDark: Boolean = true,
    onTrailingClick: (() -> Unit)? = null
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // left accent bar (matches Stitch light design)
        if (!isDark) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PlacementColors.Primary)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            title,
            fontSize = if (isDark) 16.sp else 14.sp,
            fontWeight = FontWeight.Bold,
            color = PlacementColors.textPrimary(isDark),
            modifier = Modifier.weight(1f)
        )
        if (trailingText.isNotEmpty() && onTrailingClick != null) {
            Text(
                trailingText,
                fontSize = 12.sp,
                color = PlacementColors.Primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickableWithNoIndication(onClick = onTrailingClick)
                    .padding(4.dp)
            )
        }
    }
}

// ── Placement Contact Card ─────────────────────────────────────────────────────
@Composable
fun PlacementContactCard(
    contact: PlacementContact,
    onLinkedIn: () -> Unit,
    onEmail: () -> Unit,
    isDark: Boolean = true
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = PlacementColors.surface(isDark),
        border = BorderStroke(1.dp, PlacementColors.border(isDark)),
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PlacementColors.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = PlacementColors.Primary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(contact.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PlacementColors.textPrimary(isDark))
                Text(contact.role, fontSize = 11.sp, color = PlacementColors.textSecond(isDark), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            val actionBg = if (isDark) PlacementColors.PrimaryDim else Color(0xFFE0F2FE)
            Surface(shape = RoundedCornerShape(20.dp), color = actionBg, modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickableWithNoIndication(onClick = onEmail)) {
                Text(
                    "Message",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PlacementColors.Primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// ── Alumni Card ────────────────────────────────────────────────────────────────
@Composable
fun AlumniCard(insight: AlumniInsight, modifier: Modifier = Modifier, isDark: Boolean = true) {
    val gradStart = PlacementColors.SurfaceElevated
    val gradEnd   = PlacementColors.Surface
    Surface(
        modifier = modifier.width(220.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, PlacementColors.border(isDark)),
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Box(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(gradStart, gradEnd)))
                .padding(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PlacementColors.Primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(insight.name.first().toString(), color = PlacementColors.Primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(insight.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PlacementColors.textPrimary(isDark))
                        Text(insight.batchInfo, fontSize = 10.sp, color = PlacementColors.textSecond(isDark))
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.FormatQuote, null, tint = PlacementColors.Primary.copy(alpha = 0.35f), modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.height(10.dp))
                Text(insight.quote, fontSize = 11.sp, color = PlacementColors.textSecond(isDark), lineHeight = 17.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ── Prep Resource Item ─────────────────────────────────────────────────────────
@Composable
fun PrepResourceItem(resource: PrepResource, onClick: () -> Unit, isDark: Boolean = true) {
    val (iconBg, iconTint) = when (resource.icon) {
        PrepResourceIcon.DOCUMENT -> Pair(Color(0xFFFFF1F2), Color(0xFFF43F5E))
        PrepResourceIcon.LINK     -> Pair(Color(0xFFEFF6FF), Color(0xFF3B82F6))
        PrepResourceIcon.CODE     -> Pair(Color(0xFFFFFBEB), Color(0xFFF59E0B))
    }
    val cardBg = PlacementColors.Surface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickableWithNoIndication(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = cardBg,
        border = BorderStroke(1.dp, PlacementColors.border(isDark)),
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (resource.icon) {
                        PrepResourceIcon.DOCUMENT -> Icons.Default.PictureAsPdf
                        PrepResourceIcon.LINK     -> Icons.Default.Link
                        PrepResourceIcon.CODE     -> Icons.Default.FlashOn
                    },
                    null, tint = iconTint, modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(resource.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PlacementColors.textPrimary(isDark))
                Text(resource.subtitle, fontSize = 11.sp, color = PlacementColors.textSecond(isDark))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = PlacementColors.textMuted(isDark), modifier = Modifier.size(16.dp))
        }
    }
}

// ── Feedback Card ──────────────────────────────────────────────────────────────
@Composable
fun FeedbackCard(feedback: AlumniFeedback, isDark: Boolean = true) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = PlacementColors.surface(isDark),
        border = BorderStroke(1.dp, PlacementColors.border(isDark)),
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { i ->
                    Icon(
                        if (i < feedback.rating) Icons.Default.Star else Icons.Default.StarBorder,
                        null,
                        tint = PlacementColors.Warning,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text("• ${feedback.daysAgo} days ago", fontSize = 10.sp, color = PlacementColors.textMuted(isDark))
            }
            Spacer(Modifier.height(8.dp))
            Text(feedback.quote, fontSize = 12.sp, color = PlacementColors.textSecond(isDark), lineHeight = 18.sp)
            Spacer(Modifier.height(6.dp))
            Text("— ${feedback.batch}", fontSize = 10.sp, color = PlacementColors.textMuted(isDark))
        }
    }
}

// ── Eligibility Branch Row ─────────────────────────────────────────────────────
@Composable
fun EligibilityBranchRow(branches: List<String>, isDark: Boolean = true) {
    val chipBg     = PlacementColors.SurfaceElevated
    val chipBorder = PlacementColors.border(isDark)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        branches.forEach { branch ->
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = chipBg,
                border = BorderStroke(1.dp, chipBorder)
            ) {
                Text(
                    branch,
                    fontSize = 11.sp,
                    color = PlacementColors.textSecond(isDark),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
