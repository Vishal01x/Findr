@file:OptIn(ExperimentalLayoutApi::class)

package com.exa.android.reflekt.loopit.presentation.main.placement

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exa.android.reflekt.loopit.presentation.main.placement.components.*
import com.exa.android.reflekt.loopit.presentation.main.placement.model.*
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.CompanyDetailEvent
import com.exa.android.reflekt.loopit.presentation.main.placement.vm.CompanyDetailViewModel

// ─── Design Tokens (from Stitch "Detailed Company Insights") ──────────────────
private object InsightColors {
    val Background   @Composable get() = LiquidColors.BgBase
    val Surface      @Composable get() = LiquidColors.GlassSurface
    val Border       @Composable get() = LiquidColors.GlassBorder
    val Primary      @Composable get() = LiquidColors.Primary
    val PrimaryDark  @Composable get() = LiquidColors.Primary.copy(alpha = 0.8f)
    val TextPrimary  @Composable get() = LiquidColors.TextPrimary
    val TextSecondary@Composable get() = LiquidColors.TextSecondary
    val TextMuted    @Composable get() = LiquidColors.TextMuted
    val Success      @Composable get() = LiquidColors.Green
    val Blue         @Composable get() = LiquidColors.MeshBlue
    val Rose         @Composable get() = LiquidColors.Red
    val Amber        @Composable get() = LiquidColors.Amber
    val Yellow       @Composable get() = LiquidColors.Amber
    val ChipActive   @Composable get() = LiquidColors.GlassHighlight
    val ChipInactive @Composable get() = LiquidColors.GlassSurface
    val Divider      @Composable get() = LiquidColors.GlassBorder
}

// ─── Data for nav sections ────────────────────────────────────────────────────
private val SECTION_LABELS = listOf("Eligibility", "Rounds", "Prep Guide", "Culture")

// ─── Main Stateless Screen ────────────────────────────────────────────────────
@Composable
fun DetailedCompanyInsightsScreen(
    companyId: String,
    onBack: () -> Unit = {},
    onCheckEligibility: () -> Unit = {},
    viewModel: CompanyDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(companyId) { viewModel.onEvent(CompanyDetailEvent.LoadCompany(companyId)) }

    val scrollState = rememberScrollState()
    var activeSection by remember { mutableIntStateOf(0) }

    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = LiquidColors.Primary)
        }
        state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    state.error ?: "Unknown error",
                    color = LiquidColors.TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = { viewModel.onEvent(CompanyDetailEvent.LoadCompany(companyId)) }) {
                    Text("Retry")
                }
            }
        }
        state.company != null -> {
            val company = state.company!!

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(InsightColors.Background)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // ── Sticky Top Bar ──────────────────────────────────────────────
                    InsightTopBar(onBack = onBack)

                    // ── Sticky Chip Nav ─────────────────────────────────────────────
                    InsightChipNav(
                        sections = SECTION_LABELS,
                        activeIndex = activeSection,
                        onChipClick = { activeSection = it }
                    )

                    // ── Scrollable Content ──────────────────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(bottom = 100.dp) // space for FAB
                    ) {
                        // Hero
                        InsightHeroSection(company)

                        Spacer(Modifier.height(8.dp))

                        // 1. Eligibility
                        InsightEligibilitySection(company)

                        // 2. Recruitment Rounds
                        InsightRoundsSection(company)

                        // 3. Prep Guide
                        InsightPrepGuideSection(company)

                        // 4. Alumni & Culture
                        InsightCultureSection(company)
                    }
                }
            }
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────
@Composable
private fun InsightTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = InsightColors.TextPrimary)
        }
        
        Text("Insights", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = InsightColors.TextPrimary)
        
        IconButton(
            onClick = {},
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(LiquidColors.GlassSurface)
                .border(1.dp, LiquidColors.GlassBorder, CircleShape)
        ) {
            Icon(Icons.Default.BookmarkBorder, "Bookmark", tint = InsightColors.TextPrimary)
        }
    }
}

// ─── Sticky Horizontal Chip Nav ────────────────────────────────────────────────
@Composable
private fun InsightChipNav(
    sections: List<String>,
    activeIndex: Int,
    onChipClick: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(sections) { idx, label ->
            val isActive = idx == activeIndex
            val bgColor by animateColorAsState(
                targetValue = if (isActive) LiquidColors.Primary.copy(alpha = 0.2f) else LiquidColors.GlassSurface,
                animationSpec = tween(200), label = "chip_bg"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isActive) LiquidColors.Primary.copy(alpha = 0.4f) else LiquidColors.GlassBorder,
                animationSpec = tween(200), label = "chip_border"
            )
            val textColor by animateColorAsState(
                targetValue = if (isActive) LiquidColors.Primary else LiquidColors.TextSecondary,
                animationSpec = tween(200), label = "chip_text"
            )
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(50.dp))
                    .clickable { onChipClick(idx) }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
            }
        }
    }
}

// ─── Hero Section ─────────────────────────────────────────────────────────────
@Composable
private fun InsightHeroSection(company: CompanyDetail) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            CompanyLogo(logoUrl = company.logoUrl, companyName = company.name, initials = company.name.take(2).uppercase())
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f).padding(end = 40.dp)) {
                Text(company.name, fontSize = 24.sp, fontWeight = FontWeight.Black, color = InsightColors.TextPrimary)
                Text(company.tagline, fontSize = 13.sp, color = LiquidColors.TextSecondary, modifier = Modifier.padding(top = 2.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        // Stats Row — Package + Difficulty
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            company.packageLpa?.let {
                InsightStatBadge(
                    label = "PACKAGE",
                    value = it,
                    valueColor = InsightColors.Primary,
                    bgColor = InsightColors.Primary.copy(alpha = 0.1f),
                    borderColor = InsightColors.Primary.copy(alpha = 0.2f)
                )
            }
            company.difficulty?.let {
                InsightStatBadge(
                    label = "DIFFICULTY",
                    value = it,
                    valueColor = InsightColors.Yellow,
                    bgColor = InsightColors.Surface,
                    borderColor = InsightColors.Border,
                    trailingIcon = Icons.Default.Star,
                    iconTint = InsightColors.Yellow
                )
            }
            company.stipendPerMonth?.let {
                InsightStatBadge(
                    label = "STIPEND",
                    value = it,
                    valueColor = InsightColors.Primary,
                    bgColor = InsightColors.Primary.copy(alpha = 0.1f),
                    borderColor = InsightColors.Primary.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun InsightStatBadge(
    label: String,
    value: String,
    valueColor: Color,
    bgColor: Color,
    borderColor: Color,
    trailingIcon: ImageVector? = null,
    iconTint: Color = InsightColors.TextPrimary
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(32.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(32.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextMuted, letterSpacing = 0.8.sp)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = valueColor)
                if (trailingIcon != null) {
                    Spacer(Modifier.width(4.dp))
                    Icon(trailingIcon, null, tint = iconTint, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// ─── 1. Eligibility Section ────────────────────────────────────────────────────
@Composable
private fun InsightEligibilitySection(company: CompanyDetail) {
    InsightSection(
        id = "eligibility",
        title = "Eligibility Criteria",
        icon = Icons.Default.VerifiedUser
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // CGPA Row
                InsightEligibilityRow(
                    iconBg = InsightColors.Success.copy(alpha = 0.1f),
                    icon = Icons.Default.School,
                    iconTint = InsightColors.Success,
                    label = "MINIMUM CGPA",
                    value = "${company.eligibilityCriteria.minCgpa} and above",
                    note = "No active backlogs allowed"
                )

                if (company.eligibilityCriteria.allowedBranches.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(InsightColors.Divider)
                    )
                    Spacer(Modifier.height(16.dp))

                    // Branches Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(InsightColors.Blue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AccountTree,
                                null,
                                tint = InsightColors.Blue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                "ALLOWED BRANCHES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = InsightColors.TextMuted,
                                letterSpacing = 0.6.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                company.eligibilityCriteria.allowedBranches.forEach { branch ->
                                    GlassCard(shape = RoundedCornerShape(8.dp)) {
                                        Text(
                                            branch,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = InsightColors.TextSecondary,
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 4.dp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightEligibilityRow(
    iconBg: Color,
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    note: String
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = InsightColors.TextMuted,
                letterSpacing = 0.6.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = InsightColors.TextPrimary
            )
            Text(
                note,
                fontSize = 11.sp,
                color = InsightColors.TextMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// ─── 2. Recruitment Rounds (Timeline) ────────────────────────────────────────
@Composable
private fun InsightRoundsSection(company: CompanyDetail) {
    InsightSection(
        id = "rounds",
        title = "Recruitment Process",
        icon = Icons.Default.Timeline
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Vertical connector line
            Box(
                modifier = Modifier
                    .padding(start = 19.dp)
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(InsightColors.Divider)
                    .matchParentSize() // trick to align behind items
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                company.recruitmentStages.forEachIndexed { idx, stage ->
                    InsightRoundItem(
                        stage = stage,
                        isLast = idx == company.recruitmentStages.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightRoundItem(stage: RecruitmentStage, isLast: Boolean) {
    val isActive = stage.status == StageStatus.CURRENT
    val dotBorder = if (isActive) InsightColors.Primary else InsightColors.Divider
    val dotText = if (isActive) InsightColors.Primary else InsightColors.TextMuted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isLast) 0.dp else 20.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Circle step indicator
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(InsightColors.Surface)
                .border(2.dp, dotBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${stage.order}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = dotText
            )
        }

        Spacer(Modifier.width(12.dp))

        // Card
        val modifier = Modifier.weight(1f).padding(top = 4.dp)
        if (stage.status == StageStatus.UPCOMING) {
            modifier.alpha(0.8f)
        }
        GlassCard(
            modifier = modifier,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    stage.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InsightColors.TextPrimary
                )
                if (stage.durationMins > 0 || stage.tags.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (stage.durationMins > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Schedule,
                                    null,
                                    tint = InsightColors.TextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    "${stage.durationMins} mins",
                                    fontSize = 11.sp,
                                    color = InsightColors.TextSecondary
                                )
                            }
                        }
                        stage.tags.forEach { tag ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Code,
                                    null,
                                    tint = InsightColors.TextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(tag, fontSize = 11.sp, color = InsightColors.TextSecondary)
                            }
                        }
                    }
                }
                if (stage.description.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stage.description,
                        fontSize = 13.sp,
                        color = InsightColors.TextSecondary,
                        lineHeight = 20.sp
                    )
                }
                if (stage.status == StageStatus.CLEARED) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Status: Cleared",
                        fontSize = 12.sp,
                        color = InsightColors.Success,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─── 3. Prep Guide Section ─────────────────────────────────────────────────────
@Composable
private fun InsightPrepGuideSection(company: CompanyDetail) {
    if (company.preparationResources.isEmpty()) return
    InsightSection(
        id = "prep",
        title = "Preparation Guide",
        icon = Icons.Default.MenuBook
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            company.preparationResources.forEach { resource ->
                InsightPrepResourceCard(resource)
            }
        }
    }
}

@Composable
private fun InsightPrepResourceCard(resource: PrepResource) {
    val (iconBg, iconTint, trailingIcon) = when (resource.icon) {
        PrepResourceIcon.DOCUMENT -> Triple(
            InsightColors.Rose.copy(alpha = 0.1f),
            InsightColors.Rose,
            Icons.Default.Download
        )
        PrepResourceIcon.LINK -> Triple(
            InsightColors.Blue.copy(alpha = 0.1f),
            InsightColors.Blue,
            Icons.AutoMirrored.Filled.OpenInNew
        )
        PrepResourceIcon.CODE -> Triple(
            InsightColors.Amber.copy(alpha = 0.1f),
            InsightColors.Amber,
            Icons.AutoMirrored.Filled.OpenInNew
        )
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable { },
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (resource.icon) {
                        PrepResourceIcon.DOCUMENT -> Icons.Default.PictureAsPdf
                        PrepResourceIcon.LINK -> Icons.Default.Link
                        PrepResourceIcon.CODE -> Icons.Default.FlashOn
                    },
                    null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    resource.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InsightColors.TextPrimary
                )
                Text(
                    resource.subtitle,
                    fontSize = 11.sp,
                    color = InsightColors.TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Icon(
                trailingIcon,
                null,
                tint = InsightColors.TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ─── 4. Alumni & Culture Section ──────────────────────────────────────────────
@Composable
private fun InsightCultureSection(company: CompanyDetail) {
    InsightSection(
        id = "culture",
        title = "Alumni & Insights",
        icon = Icons.Default.People
    ) {
        // Connect with alumni card
        if (company.alumniInsights.isNotEmpty()) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    // Glow blob
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 20.dp, y = (-20).dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        InsightColors.Primary.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    Column {
                        Text(
                            "CONNECT WITH ALUMNI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InsightColors.TextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Stacked avatars
                            Row(modifier = Modifier.weight(1f)) {
                                company.alumniInsights.take(3).forEachIndexed { idx, insight ->
                                    Box(
                                        modifier = Modifier
                                            .offset(x = (-idx * 10).dp)
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(InsightColors.Primary.copy(alpha = 0.15f))
                                            .border(2.dp, InsightColors.Surface, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            insight.name.first().toString(),
                                            color = InsightColors.Primary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                // +N badge
                                if (company.alumniInsights.size > 3) {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = (-30).dp)
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(InsightColors.Background)
                                            .border(2.dp, InsightColors.Surface, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "+${company.alumniInsights.size - 3}",
                                            color = InsightColors.TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                "VIEW ALL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = InsightColors.Primary,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Feedback
        if (company.recentFeedback.isNotEmpty()) {
            Text(
                "RECENT FEEDBACK",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = InsightColors.TextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            company.recentFeedback.forEach { feedback ->
                InsightFeedbackCard(feedback)
                Spacer(Modifier.height(10.dp))
            }
        }

        // Alumni quotes
        company.alumniInsights.filter { it.quote.isNotEmpty() }.forEach { insight ->
            InsightAlumniQuoteCard(insight)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun InsightFeedbackCard(feedback: AlumniFeedback) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { i ->
                    Icon(
                        if (i < feedback.rating) Icons.Default.Star else Icons.Default.StarBorder,
                        null,
                        tint = InsightColors.Yellow,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "• ${feedback.daysAgo} days ago",
                    fontSize = 11.sp,
                    color = InsightColors.TextMuted
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                feedback.quote,
                fontSize = 13.sp,
                color = InsightColors.TextSecondary,
                lineHeight = 20.sp,
                fontStyle = FontStyle.Italic
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "- ${feedback.batch}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = InsightColors.TextMuted,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun InsightAlumniQuoteCard(insight: AlumniInsight) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(InsightColors.Primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        insight.name.first().toString(),
                        color = InsightColors.Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        insight.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InsightColors.TextPrimary
                    )
                    Text(
                        insight.batchInfo,
                        fontSize = 11.sp,
                        color = InsightColors.TextSecondary
                    )
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Default.FormatQuote,
                    null,
                    tint = InsightColors.Primary.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                insight.quote,
                fontSize = 12.sp,
                color = InsightColors.TextSecondary,
                lineHeight = 18.sp,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

// ─── Section Shell ─────────────────────────────────────────────────────────────
@Composable
private fun InsightSection(
    id: String,
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = InsightColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                icon,
                null,
                tint = InsightColors.Primary.copy(alpha = 0.6f),
                modifier = Modifier.size(22.dp)
            )
        }
        content()
    }
}

// ─── Floating CTA ─────────────────────────────────────────────────────────────
@Composable
private fun InsightFloatingCTA(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, isActive: Boolean = true, isLoading: Boolean = false) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(LiquidColors.GlassSurface)
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(32.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("ACTION", fontSize = 10.sp, color = LiquidColors.TextMuted, letterSpacing = 0.8.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(if (label.contains("Check")) "Verify Profile" else "Apply Now", fontSize = 15.sp, color = InsightColors.TextPrimary, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onClick,
                enabled = isActive && !isLoading,
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isActive) LiquidColors.Primary else InsightColors.Surface),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isActive) InsightColors.Surface else InsightColors.TextPrimary)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp), tint = if (isActive) InsightColors.Surface else InsightColors.TextPrimary)
            }
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────
private const val INSIGHT_DEVICE =
    "spec:width=412dp,height=892dp,dpi=560,isRound=false,chinSize=0dp,orientation=portrait"

@Preview(name = "Detailed Insights · Google", device = INSIGHT_DEVICE, showSystemUi = true)
@Composable
private fun PreviewInsightsGoogle() {
    MaterialTheme {
        DetailedCompanyInsightsScreen(companyId = "google_preview")
    }
}

@Preview(name = "Detailed Insights · Stripe", device = INSIGHT_DEVICE, showSystemUi = true)
@Composable
private fun PreviewInsightsStripe() {
    MaterialTheme {
        DetailedCompanyInsightsScreen(companyId = "stripe_preview")
    }
}
