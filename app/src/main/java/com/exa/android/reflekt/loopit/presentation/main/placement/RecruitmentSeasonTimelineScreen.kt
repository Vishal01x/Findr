package com.exa.android.reflekt.loopit.presentation.main.placement

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.reflekt.loopit.presentation.main.placement.CompanyLogo
import com.exa.android.reflekt.loopit.presentation.main.placement.components.GlassCard
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidMeshBackground

// ─── Data Models ─────────────────────────────────────────────────────────────

enum class EventStatus { PAST, LIVE, UPCOMING }

data class TimelineEventModel(
    val id: String,
    val companyName: String,
    val role: String,
    val status: EventStatus,
    val logoInitials: String,
    val logoBgColor: Color = Color.Transparent,
    
    // Past specific
    val endedDate: String? = null,
    val selectedCount: Int? = null,
    
    // Live specific
    val packageInfo: String? = null,
    val location: String? = null,
    val closesIn: String? = null,
    
    // Upcoming specific
    val opensIn: String? = null,
    val dateLabel: String? = null
)

/**
 * Converts a list of [CompanyDetail] objects fetched from Firestore into
 * [TimelineEventModel] entries for the timeline screen.
 * Status is derived from [CompanyDetail.isLiveProcess] and deadline fields.
 */
internal fun companiesToTimeline(companies: List<com.exa.android.reflekt.loopit.presentation.main.placement.model.CompanyDetail>): List<TimelineEventModel> {
    return companies.mapIndexed { index, c ->
        val status = when {
            c.isLiveProcess -> EventStatus.LIVE
            else            -> EventStatus.UPCOMING
        }
        val dateLabel = when {
            c.internDeadline.isNotBlank()   -> c.internDeadline.take(24)
            c.fulltimeDeadline.isNotBlank() -> c.fulltimeDeadline.take(24)
            c.deadline.isNotBlank()         -> c.deadline.take(24)
            else                            -> "TBD"
        }
        val packageText = c.packageLpa?.let { "₹$it" } ?: c.stipendPerMonth ?: ""
        val initials = c.name.split(" ").take(2).joinToString("") { it.take(2) }.uppercase()

        TimelineEventModel(
            id          = c.id.ifBlank { "co_$index" },
            companyName = c.name,
            role        = c.jobRole.ifBlank { c.tagline },
            status      = status,
            logoInitials = initials,
            logoBgColor  = Color(0xFF1E3A5F),
            packageInfo  = packageText,
            location     = c.location,
            dateLabel    = dateLabel,
            opensIn      = dateLabel,
            selectedCount = c.hiresCount
        )
    }
}

// ─── Main Screen ─────────────────────────────────────────────────────────────

@Composable
fun RecruitmentSeasonTimelineScreen(
    onBack: () -> Unit = {},
    onCompanyClick: (String) -> Unit = {},
    companies: List<com.exa.android.reflekt.loopit.presentation.main.placement.model.CompanyDetail> = emptyList()
) {
    // Use static timeline for 2025-2026 batch as requested, but map real IDs if available
    val displayTimeline = remember(companies) {
        listOf(
            TimelineEventModel(
                id = companies.find { it.name.contains("EPAM", ignoreCase = true) }?.id ?: "epam",
                companyName = "EPAM", role = "Software Engineer", status = EventStatus.UPCOMING,
                logoInitials = "EP", dateLabel = "Mid July 2025", opensIn = "Mid July"
            ),
            TimelineEventModel(
                id = companies.find { it.name.contains("Infosys HWI", ignoreCase = true) }?.id ?: "infosys_hwi",
                companyName = "Infosys HWI", role = "System Engineer", status = EventStatus.UPCOMING,
                logoInitials = "IN", dateLabel = "Mid July 2025", opensIn = "Mid July"
            ),
            TimelineEventModel(
                id = companies.find { it.name.contains("Cisco", ignoreCase = true) }?.id ?: "cisco",
                companyName = "Cisco", role = "Software Engineer", status = EventStatus.UPCOMING,
                logoInitials = "CI", dateLabel = "Late July 2025", opensIn = "Late July"
            ),
            TimelineEventModel(
                id = companies.find { it.name.contains("Quantiphi", ignoreCase = true) }?.id ?: "quantiphi",
                companyName = "Quantiphi", role = "Framework Engineer", status = EventStatus.UPCOMING,
                logoInitials = "QU", dateLabel = "Late July 2025", opensIn = "Late July"
            ),
            TimelineEventModel(
                id = companies.find { it.name.contains("Carwale", ignoreCase = true) }?.id ?: "carwale",
                companyName = "Carwale", role = "SDE 1", status = EventStatus.UPCOMING,
                logoInitials = "CA", dateLabel = "Mid Sept 2025", opensIn = "Mid Sept"
            ),
            TimelineEventModel(
                id = companies.find { it.name.contains("Tejas", ignoreCase = true) }?.id ?: "tejas",
                companyName = "Tejas Network", role = "R&D Engineer", status = EventStatus.UPCOMING,
                logoInitials = "TE", dateLabel = "Mid Sept 2025", opensIn = "Mid Sept"
            ),
            TimelineEventModel(
                id = companies.find { it.name.contains("Cognizant", ignoreCase = true) }?.id ?: "cognizant",
                companyName = "Cognizant", role = "GenC / GenC Elevate", status = EventStatus.UPCOMING,
                logoInitials = "CO", dateLabel = "Mid Sept 2025", opensIn = "Mid Sept"
            ),
            TimelineEventModel(
                id = companies.find { it.name.contains("Capgemini", ignoreCase = true) }?.id ?: "capgemini",
                companyName = "Capgemini", role = "Analyst", status = EventStatus.UPCOMING,
                logoInitials = "CA", dateLabel = "Late Sept 2025", opensIn = "Late Sept"
            ),
            TimelineEventModel(
                id = companies.find { it.name.contains("Mindtree", ignoreCase = true) }?.id ?: "mindtree",
                companyName = "Mindtree", role = "Software Engineer", status = EventStatus.UPCOMING,
                logoInitials = "MI", dateLabel = "Late Sept 2025", opensIn = "Late Sept"
            ),
            TimelineEventModel(
                id = companies.find { it.name.contains("HSBC", ignoreCase = true) }?.id ?: "hsbc",
                companyName = "HSBC", role = "Striker / Trainee", status = EventStatus.UPCOMING,
                logoInitials = "HS", dateLabel = "End Oct 2025", opensIn = "End Oct"
            ),
            TimelineEventModel(
                id = companies.find { it.name.contains("TCS", ignoreCase = true) }?.id ?: "tcs",
                companyName = "TCS", role = "Ninja / Digital", status = EventStatus.UPCOMING,
                logoInitials = "TC", dateLabel = "Nov 2025", opensIn = "Nov"
            ),
            TimelineEventModel(
                id = companies.find { it.name.equals("Infosys", ignoreCase = true) }?.id ?: "infosys",
                companyName = "Infosys", role = "System Engineer", status = EventStatus.UPCOMING,
                logoInitials = "IN", dateLabel = "Late Nov 2025", opensIn = "Late Nov"
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(LiquidColors.BgBase)) {
        
        Column(modifier = Modifier.fillMaxSize()) {
            TimelineHeader(onBack = onBack)
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 100.dp)
            ) {
                itemsIndexed(displayTimeline) { index, event ->
                    TimelineNodeItem(
                        event = event, 
                        isLast = index == displayTimeline.lastIndex,
                        onCompanyClick = onCompanyClick
                    )
                }
                
                item {
                    EndStateNode()
                }
            }
        }
    }
}

// ─── Components ──────────────────────────────────────────────────────────────

@Composable
private fun TimelineHeader(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LiquidColors.BgBase.copy(alpha = 0.85f))
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Spacer(Modifier.windowInsetsPadding(WindowInsets.statusBars))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LiquidColors.GlassSurface)
                    .border(1.dp, LiquidColors.GlassBorder, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = LiquidColors.TextPrimary)
            }
            
            Text("Company Timeline", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
            
            Box(modifier = Modifier.size(44.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(LiquidColors.GlassBorder))
    }
}

@Composable
private fun StatBox(value: String, label: String, valueColor: Color = LiquidColors.TextPrimary, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = LiquidColors.TextSecondary, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun StatBoxActive(value: String, label: String, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.05f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "pulseAlpha"
    )
    val primaryColor = LiquidColors.Primary
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(LiquidColors.Primary.copy(alpha = 0.1f))
            .drawBehind { drawRect(primaryColor.copy(alpha = alpha)) }
            .border(1.dp, LiquidColors.Primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LiquidColors.Primary)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = LiquidColors.Primary, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun TimelineNodeItem(event: TimelineEventModel, isLast: Boolean, onCompanyClick: (String) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        // Timeline Track & Node
        TimelineTrack(event = event, isLast = isLast)
        
        Spacer(Modifier.width(16.dp))
        
        // Event Card
        val cardModifier = Modifier.weight(1f)
        when (event.status) {
            EventStatus.PAST -> PastEventCard(event, modifier = cardModifier)
            EventStatus.LIVE -> LiveEventCard(event, modifier = cardModifier)
            EventStatus.UPCOMING -> UpcomingEventCard(event, modifier = cardModifier)
        }
    }
}

@Composable
private fun TimelineTrack(event: TimelineEventModel, isLast: Boolean) {
    val status = event.status
    val primaryColor = LiquidColors.Primary
    val trackColor = LiquidColors.GlassBorder
    
    Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.TopCenter) {
        // Vertical Line
        if (!isLast) {
            Box(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .width(2.dp)
                    .fillMaxHeight(.7f) // Extend beyond the current item
                    .drawBehind {
                        val pathEffect = if (status == EventStatus.UPCOMING) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
                        val color = if (status == EventStatus.UPCOMING) trackColor else primaryColor
                        drawLine(
                            color = color,
                            start = androidx.compose.ui.geometry.Offset(size.width/2, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width/2, size.height + 200f),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = pathEffect
                        )
                    }
            )
        }
        
        // Node
        when (status) {
            EventStatus.PAST -> {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(LiquidColors.BgBase)
                        .border(2.dp, LiquidColors.Primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = LiquidColors.Primary, modifier = Modifier.size(20.dp))
                }
            }
            EventStatus.LIVE -> {
                val infiniteTransition = rememberInfiniteTransition(label = "pulseNode")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 1.6f,
                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                    label = "scale"
                )
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                    Box(modifier = Modifier.size(48.dp).alpha(0.3f).drawBehind {
                        drawCircle(color = primaryColor, radius = (size.width/2) * scale, style = Stroke(width = 4f))
                    })
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(LiquidColors.BgBase)
                            .border(4.dp, LiquidColors.Primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Bolt, null, tint = LiquidColors.Primary)
                    }
                }
            }
            EventStatus.UPCOMING -> {
                val icon = if (event.companyName == "Microsoft") Icons.Default.LockClock else Icons.Default.CalendarToday
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(LiquidColors.BgBase)
                        .border(2.dp, trackColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = LiquidColors.TextSecondary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ─── Event Cards ─────────────────────────────────────────────────────────────

@Composable
private fun PastEventCard(event: TimelineEventModel, modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier.alpha(0.8f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CompanyLogo(initials = event.logoInitials)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(event.companyName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
                        Text(event.role, fontSize = 13.sp, color = LiquidColors.TextSecondary)
                    }
                }
                Box(
                    modifier = Modifier
                        .background(LiquidColors.BgBase, RoundedCornerShape(50.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(event.endedDate ?: "", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = LiquidColors.TextSecondary)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(LiquidColors.GlassBorder))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Selected: ${event.selectedCount} students", fontSize = 12.sp, color = LiquidColors.TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { }) {
                    Text("Results", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = LiquidColors.Primary)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = LiquidColors.Primary, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun LiveEventCard(event: TimelineEventModel, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        // Glowing background pulse
        val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.05f, targetValue = 0.15f,
            animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
            label = "alpha"
        )
        val primaryColor = LiquidColors.Primary
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(4.dp)
                .drawBehind { drawRoundRect(primaryColor.copy(alpha = alpha), cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())) }
        )
        
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CompanyLogo(initials = event.logoInitials)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(event.companyName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
                            Text(event.role, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = LiquidColors.Primary)
                            Text("${event.packageInfo} • ${event.location}", fontSize = 12.sp, color = LiquidColors.TextSecondary, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(LiquidColors.Primary, RoundedCornerShape(50.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(Color.White, CircleShape))
                            Spacer(Modifier.width(4.dp))
                            Text("LIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LiquidColors.BgBase.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Schedule, null, tint = Color(0xFFF97316), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Closes in: ", fontSize = 12.sp, color = LiquidColors.TextSecondary)
                    Text(event.closesIn ?: "", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF97316), modifier = Modifier.padding(start = 4.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.GlassSurface, contentColor = LiquidColors.TextPrimary),
                        border = BorderStroke(1.dp, LiquidColors.GlassBorder)
                    ) {
                        Text("Details", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary, contentColor = Color.White)
                    ) {
                        Text("Apply Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.Send, null, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingEventCard(event: TimelineEventModel, modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier.alpha(0.6f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Grayscale/Muted logo representation
                    CompanyLogo(initials = event.logoInitials)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(event.companyName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = LiquidColors.TextPrimary)
                        Text(event.role, fontSize = 13.sp, color = LiquidColors.TextSecondary)
                    }
                }
            }
            
            if (event.opensIn != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LiquidColors.BgBase.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .border(1.dp, LiquidColors.GlassBorder.copy(alpha=0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("OPENS IN", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = LiquidColors.TextMuted, letterSpacing = 1.sp)
                    Text(event.opensIn, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
                }
            }
        }
    }
}

@Composable
fun EndStateNode() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp).alpha(0.5f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(8.dp).background(LiquidColors.TextSecondary, CircleShape))
        }
        Spacer(Modifier.width(16.dp))
        Text("More companies will be announced soon...", fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = LiquidColors.TextSecondary)
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun RecruitmentSeasonTimelineScreenPreviewLight() = MaterialTheme {
    RecruitmentSeasonTimelineScreen()
}

@Preview(showBackground = true, name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RecruitmentSeasonTimelineScreenPreviewDark() = MaterialTheme {
    RecruitmentSeasonTimelineScreen()
}
