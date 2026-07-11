package com.exa.android.reflekt.loopit.presentation.main.placement

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exa.android.reflekt.loopit.presentation.main.component.textfield.liquidTextFieldColors
import com.exa.android.reflekt.loopit.presentation.main.placement.components.*
import com.exa.android.reflekt.loopit.presentation.main.placement.model.*
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.*
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.*
import com.exa.android.reflekt.loopit.presentation.main.placement.vm.BatchReviewViewModel
import com.exa.android.reflekt.loopit.presentation.main.placement.vm.CompanyDetailViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch

// ─── Entry Point ──────────────────────────────────────────────────────────────
@Composable
fun CompanyDetailScreen(
    companyId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEligibilityForm: () -> Unit = {},
    onNavigateToPlacedStudents: (Int, String) -> Unit = { _, _ -> },
    viewModel: CompanyDetailViewModel = hiltViewModel(),
    batchReviewViewModel: BatchReviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    LaunchedEffect(companyId) { viewModel.onEvent(CompanyDetailEvent.LoadCompany(companyId)) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CompanyDetailSideEffect.NavigateBack          -> onNavigateBack()
                is CompanyDetailSideEffect.ShowToast             -> Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                is CompanyDetailSideEffect.OpenUrl               -> uriHandler.openUri(effect.url)
                is CompanyDetailSideEffect.NavigateToTaskList    -> { /* TODO */ }
                is CompanyDetailSideEffect.NavigateToAlumniList  -> { /* TODO */ }
                is CompanyDetailSideEffect.NavigateToEligibilityForm -> onNavigateToEligibilityForm()
                is CompanyDetailSideEffect.NavigateToPlacedStudents  -> onNavigateToPlacedStudents(effect.batchYear, state.company?.name ?: companyId)
            }
        }
    }

    // Collect batch review toast effects
    LaunchedEffect(Unit) {
        batchReviewViewModel.effect.collect { effect ->
            when (effect) {
                is BatchReviewSideEffect.ShowToast -> Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    CompanyDetailContent(state = state, onEvent = viewModel::onEvent, batchReviewViewModel = batchReviewViewModel)
}

// ─── Stateless root ───────────────────────────────────────────────────────────
@Composable
fun CompanyDetailContent(
    state: CompanyDetailState,
    onEvent: (CompanyDetailEvent) -> Unit,
    forceDark: Boolean? = null,
    batchReviewViewModel: BatchReviewViewModel? = null
) {
    Box(modifier = Modifier.fillMaxSize().background(LiquidColors.BgBase)) {
        
        when {
            state.isLoading     -> FullScreenLoader()
            state.error != null -> ErrorState(state.error) {
                onEvent(CompanyDetailEvent.LoadCompany("stripe"))
            }
            state.company != null -> MainContent(state, onEvent, batchReviewViewModel)
        }
    }
}

// ─── Loader ───────────────────────────────────────────────────────────────────
@Composable
private fun FullScreenLoader() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = LiquidColors.Primary)
    }
}

// ─── Error ────────────────────────────────────────────────────────────────────
@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.ErrorOutline, null, tint = LiquidColors.TextMuted, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text(message, color = LiquidColors.TextSecondary)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary)) {
            Text("Retry")
        }
    }
}

// ─── Main Content ──────────────────────────────────────────────────────────────
@Composable
private fun MainContent(
    state: CompanyDetailState,
    onEvent: (CompanyDetailEvent) -> Unit,
    batchReviewViewModel: BatchReviewViewModel? = null
) {
    val company = state.company!!
    val scroll  = rememberScrollState()

    val tabs = buildList {
        add(CompanyDetailTab.OVERVIEW)
        if (company.eligibilityCriteria.minCgpa.isNotEmpty()) add(CompanyDetailTab.ELIGIBILITY)
        add(CompanyDetailTab.PROCESS)
        if (company.preparationResources.isNotEmpty() || company.referenceLinks.isNotEmpty() || company.pdfAttachments.isNotEmpty())
            add(CompanyDetailTab.PREP_GUIDE)
        if (company.workCulture.isNotBlank() || company.referenceLinks.isNotEmpty() ||
            company.pdfAttachments.isNotEmpty() || company.website.isNotBlank())
            add(CompanyDetailTab.CULTURE)
        add(CompanyDetailTab.REVIEWS)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(bottom = 120.dp) // space for floating CTA
        ) {
            CompanyDetailHeader(
                onBack = { onEvent(CompanyDetailEvent.NavigateBack) },
                onShare = { onEvent(CompanyDetailEvent.ToggleShareSheet) }
            )
            
            LiquidCompanyHero(company)
            Spacer(Modifier.height(24.dp))

            if (company.preparationPercent > 0) {
                PreparationCard(
                    percent = company.preparationPercent,
                    skillsMatched = company.skillsMatched,
                    totalSkills = company.totalSkills,
                    onViewTasks = { onEvent(CompanyDetailEvent.ViewTasksTapped) },
                    modifier = Modifier.padding(horizontal = 24.dp),
                    isDark = isSystemInDarkTheme()
                )
                Spacer(Modifier.height(24.dp))
            }

            LiquidSegmentedTabs(
                tabs = tabs,
                selectedTab = state.selectedTab,
                onSelect = { onEvent(CompanyDetailEvent.TabSelected(it)) },
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(24.dp))

            when (state.selectedTab) {
                CompanyDetailTab.OVERVIEW    -> OverviewTab(company, state.batchPlacementStats, onEvent)
                CompanyDetailTab.PROCESS     -> ProcessTab(company)
                CompanyDetailTab.PREP_GUIDE  -> PrepGuideTab(company, onEvent)
                CompanyDetailTab.ELIGIBILITY -> EligibilityTab(company)
                CompanyDetailTab.CULTURE     -> CultureTab(company, onEvent)
                CompanyDetailTab.REVIEWS     -> {
                    if (batchReviewViewModel != null) {
                        val reviewState by batchReviewViewModel.state.collectAsStateWithLifecycle()
                        LaunchedEffect(company.id) {
                            batchReviewViewModel.onEvent(BatchReviewEvent.Load(company.id))
                        }
                        BatchReviewTab(
                            state = reviewState,
                            onEvent = batchReviewViewModel::onEvent,
                            onViewPlacedStudents = { batch ->
                                onEvent(CompanyDetailEvent.ViewPlacedStudents(batch.toIntOrNull() ?: 0))
                            }
                        )
                    }
                }
            }
        }
        
        // ── Share sheet ───────────────────────────────────────────────────────
        if (state.showShareSheet && state.company != null) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val scope = rememberCoroutineScope()
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            // Start background prep (short link) as soon as the sheet appears
            LaunchedEffect(Unit) { onEvent(CompanyDetailEvent.PrepareShare(context)) }
            
            com.exa.android.reflekt.loopit.presentation.main.component.sheet.AppShareSheet(
                onCopyLink = {
                    scope.launch {
                        val shortLink = com.exa.android.reflekt.loopit.core.utils.ShareUtils.buildShortCompanyFdlUrl(context, state.company.id)
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(shortLink))
                        onEvent(CompanyDetailEvent.CloseShareSheet)
                        android.widget.Toast.makeText(context, "Link copied!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                onSystemShare = { onEvent(CompanyDetailEvent.ExecuteShare(context)) },
                onDismiss = { onEvent(CompanyDetailEvent.CloseShareSheet) }
            )
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────
@Composable
private fun CompanyDetailHeader(onBack: () -> Unit, onShare: () -> Unit) {
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = LiquidColors.TextPrimary)
        }
        
        Text("Company Details", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
        
        IconButton(
            onClick = onShare,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(LiquidColors.GlassSurface)
                .border(1.dp, LiquidColors.GlassBorder, CircleShape)
        ) {
            Icon(Icons.Default.Share, "Share", tint = LiquidColors.TextPrimary)
        }
    }
}

// ─── Hero Section ──────────────────────────────────────────────────────────────
@Composable
private fun LiquidCompanyHero(company: CompanyDetail) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            CompanyLogo(
                logoUrl = company.logoUrl,
                companyName = company.name,
                initials = company.name.take(2).uppercase()
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f).padding(end = 40.dp)) {
                Text(company.name, fontSize = 24.sp, fontWeight = FontWeight.Black, color = LiquidColors.TextPrimary)
                Text(company.tagline, fontSize = 13.sp, color = LiquidColors.TextSecondary, modifier = Modifier.padding(top = 2.dp))
                
                Spacer(Modifier.height(16.dp))

                Column {
                    Text("PACKAGE", fontSize = 10.sp, color = LiquidColors.TextMuted, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    Text(company.packageLpa ?: "-", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun LiquidStatPill(label: String, isPrimary: Boolean = false) {
    val bgColor = if (isPrimary) LiquidColors.Primary.copy(alpha=0.15f) else LiquidColors.GlassSurface
    val borderColor = if (isPrimary) LiquidColors.Primary.copy(alpha=0.3f) else LiquidColors.GlassBorder
    val textColor = if (isPrimary) LiquidColors.Primary else LiquidColors.TextPrimary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(50.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(label, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ─── Segmented Tabs ───────────────────────────────────────────────────────────
@Composable
private fun LiquidSegmentedTabs(
    tabs: List<CompanyDetailTab>,
    selectedTab: CompanyDetailTab,
    onSelect: (CompanyDetailTab) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(tabs) { tab ->
            val isSelected = tab == selectedTab
            val label = when(tab) {
                CompanyDetailTab.OVERVIEW    -> "Overview"
                CompanyDetailTab.PROCESS     -> "Rounds"
                CompanyDetailTab.PREP_GUIDE  -> "Prep Guide"
                CompanyDetailTab.ELIGIBILITY -> "Eligibility"
                CompanyDetailTab.CULTURE     -> "Culture & Links"
                CompanyDetailTab.REVIEWS     -> "📋 Batch Reviews"
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isSelected) LiquidColors.Primary.copy(alpha=0.15f) else LiquidColors.GlassSurface)
                    .border(1.dp, if (isSelected) LiquidColors.Primary else LiquidColors.GlassBorder, RoundedCornerShape(50.dp))
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) LiquidColors.Primary else LiquidColors.TextSecondary
                )
            }
        }
    }
}

// ─── Floating Bottom CTA ──────────────────────────────────────────────────────
@Composable
private fun FloatingBottomCTA(
    company: CompanyDetail,
    isChecking: Boolean,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(LiquidColors.GlassHighlight)
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(40.dp))
            .padding(8.dp)
    ) {
        Button(
            onClick = onPrimaryAction,
            enabled = !isChecking,
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isChecking) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                val label = if (company.isEligible == null) "Check Eligibility" else "Apply Now"
                Text(label, fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp), tint = Color.White)
            }
        }
    }
}

// ─── Tabs Content ─────────────────────────────────────────────────────────────
@Composable
private fun OverviewTab(
    company: CompanyDetail,
    batchPlacementStats: List<CompanyBatchStats>,
    onEvent: (CompanyDetailEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        // ── 1. About the Company ───────────────────────────────────────────────
        if (company.description.isNotBlank()) {
            Column {
                LiquidSectionHeader(title = "About the Company")
                GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            company.description,
                            fontSize = 14.sp,
                            color = LiquidColors.TextPrimary,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }

        // ── 2. Quick Stats: Employees + Location ──────────────────────────────
        val hasStats = company.employeeCount.isNotBlank() || company.location.isNotBlank()
        if (hasStats) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (company.employeeCount.isNotBlank()) {
                    OverviewStatCard(
                        icon = Icons.Default.People,
                        label = "EMPLOYEES",
                        value = company.employeeCount,
                        iconColor = Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (company.location.isNotBlank()) {
                    OverviewStatCard(
                        icon = Icons.Default.LocationOn,
                        label = "LOCATION",
                        value = company.location,
                        iconColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── 3. Package, Role & Joining Dates ──────────────────────────────────
        Column {
            LiquidSectionHeader(title = "Role & Compensation", isLive = company.isLiveProcess)
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {

                    // Role offered
                    if (company.jobRole.isNotBlank()) {
                        OverviewDetailRow(
                            icon = Icons.Default.Work,
                            label = "Role Offered",
                            value = company.jobRole,
                            iconColor = LiquidColors.Primary
                        )
                        Spacer(Modifier.height(4.dp))
                        OverviewDivider()
                        Spacer(Modifier.height(4.dp))
                    }

                    // Role type (Intern / Full Time / Both)
                    val roleLabel = when (company.roleType) {
                        com.exa.android.reflekt.loopit.presentation.main.placement.model.RoleType.INTERN   -> "Internship"
                        com.exa.android.reflekt.loopit.presentation.main.placement.model.RoleType.FULLTIME -> "Full Time"
                        com.exa.android.reflekt.loopit.presentation.main.placement.model.RoleType.BOTH     -> "Internship + Full Time"
                    }
                    OverviewDetailRow(
                        icon = Icons.Default.Badge,
                        label = "Hiring Type",
                        value = roleLabel,
                        iconColor = Color(0xFF7C3AED)
                    )

                    // Package
                    if (company.packageLpa != null) {
                        Spacer(Modifier.height(4.dp))
                        OverviewDivider()
                        Spacer(Modifier.height(4.dp))
                        OverviewDetailRow(
                            icon = Icons.Default.CurrencyRupee,
                            label = "Package (CTC)",
                            value = "${company.packageLpa} LPA",
                            iconColor = Color(0xFF10B981),
                            valueColor = Color(0xFF10B981)
                        )
                    }

                    // Stipend
                    if (company.stipendPerMonth != null) {
                        Spacer(Modifier.height(4.dp))
                        OverviewDivider()
                        Spacer(Modifier.height(4.dp))
                        OverviewDetailRow(
                            icon = Icons.Default.Payments,
                            label = "Stipend",
                            value = "₹${company.stipendPerMonth}/mo",
                            iconColor = Color(0xFF10B981),
                            valueColor = Color(0xFF10B981)
                        )
                    }

                    // Hires count
                    if (company.hiresCount > 0) {
                        Spacer(Modifier.height(4.dp))
                        OverviewDivider()
                        Spacer(Modifier.height(4.dp))
                        OverviewDetailRow(
                            icon = Icons.Default.Group,
                            label = "Hires Per Season",
                            value = "~${company.hiresCount} students",
                            iconColor = Color(0xFF3B82F6)
                        )
                    }

                    // Difficulty
                    Spacer(Modifier.height(4.dp))
                    OverviewDivider()
                    Spacer(Modifier.height(4.dp))
                    val diffColor = when (company.level) {
                        com.exa.android.reflekt.loopit.presentation.main.placement.model.DifficultyLevel.EASY   -> Color(0xFF10B981)
                        com.exa.android.reflekt.loopit.presentation.main.placement.model.DifficultyLevel.MEDIUM -> Color(0xFFF59E0B)
                        com.exa.android.reflekt.loopit.presentation.main.placement.model.DifficultyLevel.HARD   -> Color(0xFFEF4444)
                    }
                    OverviewDetailRow(
                        icon = Icons.Default.TrendingUp,
                        label = "Difficulty",
                        value = company.level.name.lowercase().replaceFirstChar { it.uppercase() },
                        iconColor = diffColor,
                        valueColor = diffColor
                    )
                }
            }
        }

        // ── 4b. Batch-wise Placements ──────────────────────────────────────────
        if (batchPlacementStats.isNotEmpty()) {
            Column {
                LiquidSectionHeader(title = "Placements by Batch")
                GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            "Students placed at ${company.name} in the last batches",
                            fontSize = 13.sp,
                            color = LiquidColors.TextSecondary
                        )
                        Spacer(Modifier.height(20.dp))
                        Box(Modifier.horizontalScroll(rememberScrollState())) {
                            BarChart(
                                data = batchPlacementStats.map { it.batchYear to it.totalHired.toFloat() },
                                yLabel = "Hires",
                                barColor = LiquidColors.Primary,
                                modifier = Modifier
                                    .width((70 * batchPlacementStats.size).dp.coerceAtLeast(280.dp))
                                    .height(180.dp),
                                onBarClick = { batch -> onEvent(CompanyDetailEvent.ViewPlacedStudents(batch)) }
                            )
                        }
                    }
                }
            }
        }

        // ── 5. Eligibility Summary ─────────────────────────────────────────────
        val hasEligibility = company.eligibilityCriteria.minCgpa.isNotBlank() ||
            company.minCgpa.isNotBlank() || company.allowedBacklogs >= 0
        if (hasEligibility) {
            Column {
                LiquidSectionHeader(title = "Eligibility Criteria")
                GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        CgpaBacklogRow(
                            cgpa = company.cgpaCutoff.ifEmpty { company.eligibilityCriteria.minCgpa.ifEmpty { company.minCgpa } },
                            yourCgpa = company.yourCgpa,
                            backlogs = company.activeBacklogs,
                            allowedBacklogs = company.allowedBacklogs,
                            isDark = isSystemInDarkTheme()
                        )
                        if (company.eligibilityCriteria.allowedBranches.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            Box(Modifier.fillMaxWidth().height(1.dp).background(LiquidColors.GlassBorder))
                            Spacer(Modifier.height(16.dp))
                            Text("ELIGIBLE BRANCHES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextMuted, letterSpacing = 0.5.sp)
                            Spacer(Modifier.height(8.dp))
                            EligibilityBranchRow(company.eligibilityCriteria.allowedBranches, isDark = isSystemInDarkTheme())
                        }
                    }
                }
            }
        }

        // ── 6. Recruitment Rounds Summary ─────────────────────────────────────
        if (company.recruitmentStages.isNotEmpty()) {
            Column {
                LiquidSectionHeader(title = "Recruitment Rounds", isLive = company.isLiveProcess)
                GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        company.recruitmentStages.forEachIndexed { idx, stage ->
                            StageItem(stage, showConnector = idx != company.recruitmentStages.lastIndex, isDark = isSystemInDarkTheme())
                        }
                    }
                }
            }
        }

        // ── 7. Connect & Learn ─────────────────────────────────────────────────
        if (company.placementContacts.isNotEmpty()) {
            Column {
                LiquidSectionHeader(title = "Connect & Learn")
                company.placementContacts.forEach { contact ->
                    PlacementContactCard(
                        contact = contact,
                        onLinkedIn = { onEvent(CompanyDetailEvent.OpenLinkedIn(contact.linkedinUrl)) },
                        onEmail = { onEvent(CompanyDetailEvent.OpenEmail(contact.emailUrl)) },
                        isDark = isSystemInDarkTheme()
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        // ── 8. Alumni Insights ─────────────────────────────────────────────────
        if (company.alumniInsights.isNotEmpty()) {
            Column {
                LiquidSectionHeader(title = "Alumni Insights", action = "View All", onAction = { onEvent(CompanyDetailEvent.ViewAllAlumniTapped) })
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(company.alumniInsights) { AlumniCard(it, isDark = isSystemInDarkTheme()) }
                }
            }
        }
    }
}

// ─── Overview Helper Components ───────────────────────────────────────────────
@Composable
private fun OverviewStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
            }
            Text(label, fontSize = 9.sp, color = LiquidColors.TextMuted, letterSpacing = 0.6.sp, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun OverviewDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    valueColor: Color = LiquidColors.TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(15.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 10.sp, color = LiquidColors.TextMuted, letterSpacing = 0.4.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
        }
    }
}

@Composable
private fun OverviewDivider() {
    Box(Modifier.fillMaxWidth().height(0.5.dp).background(LiquidColors.GlassBorder))
}


@Composable
private fun ProcessTab(company: CompanyDetail) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        company.recruitmentStages.forEachIndexed { idx, stage ->
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(20.dp)) {
                    StageItem(stage, showConnector = false, isDark = isSystemInDarkTheme())
                }
            }
        }
    }
}

@Composable
private fun PrepGuideTab(company: CompanyDetail, onEvent: (CompanyDetailEvent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Preparation resources
        if (company.preparationResources.isNotEmpty()) {
            Column {
                LiquidSectionHeader(title = "Preparation Guide")
                company.preparationResources.forEach { res ->
                    PrepResourceItem(res, onClick = { onEvent(CompanyDetailEvent.OpenResource(res.url)) }, isDark = isSystemInDarkTheme())
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        // Reference links
        if (company.referenceLinks.isNotEmpty()) {
            Column {
                LiquidSectionHeader(title = "Reference Links")
                company.referenceLinks.forEach { link ->
                    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEvent(CompanyDetailEvent.OpenResource(link.url)) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(LiquidColors.Primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Link, null, tint = LiquidColors.Primary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(link.label.ifBlank { link.url }, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LiquidColors.TextPrimary)
                                Text(link.url, fontSize = 11.sp, color = LiquidColors.TextSecondary, maxLines = 1)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = LiquidColors.Primary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        // PDF attachments
        if (company.pdfAttachments.isNotEmpty()) {
            Column {
                LiquidSectionHeader(title = "PDF Resources")
                company.pdfAttachments.forEach { pdf ->
                    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEvent(CompanyDetailEvent.OpenResource(pdf.storageUrl)) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF7C3AED).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PictureAsPdf, null, tint = Color(0xFF7C3AED), modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(pdf.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LiquidColors.TextPrimary)
                                if (pdf.sizeKb > 0)
                                    Text("${pdf.sizeKb} KB", fontSize = 11.sp, color = LiquidColors.TextSecondary)
                            }
                            Icon(Icons.Default.Download, null, tint = LiquidColors.Primary, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun EligibilityTab(company: CompanyDetail) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        val criteria = company.eligibilityCriteria
        Column {
            LiquidSectionHeader(title = "Eligibility Criteria")
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(20.dp)) {
                    CgpaBacklogRow(
                        cgpa = criteria.minCgpa,
                        yourCgpa = "",
                        backlogs = 0,
                        allowedBacklogs = 0,
                        isDark = isSystemInDarkTheme()
                    )
                    
                    if (criteria.allowedBranches.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Box(Modifier.fillMaxWidth().height(1.dp).background(LiquidColors.GlassBorder))
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountTree, null, tint = LiquidColors.Primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("ALLOWED BRANCHES", fontSize = 10.sp, color = LiquidColors.TextMuted, letterSpacing = 0.6.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(10.dp))
                        EligibilityBranchRow(criteria.allowedBranches, isDark = isSystemInDarkTheme())
                    }
                }
            }
        }
    }
}

// ─── Culture & Links Tab ───────────────────────────────────────────────────────────
@Composable
private fun CultureTab(company: CompanyDetail, onEvent: (CompanyDetailEvent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Work & Life Culture
        if (company.workCulture.isNotBlank()) {
            Column {
                LiquidSectionHeader(title = "Work & Life Culture")
                GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Apartment, null, tint = LiquidColors.Primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("CULTURE OVERVIEW", fontSize = 10.sp, color = LiquidColors.TextMuted, letterSpacing = 0.6.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(company.workCulture, fontSize = 14.sp, color = LiquidColors.TextPrimary, lineHeight = 22.sp)
                    }
                }
            }
        }

        // Website
        if (company.website.isNotBlank()) {
            Column {
                LiquidSectionHeader(title = "Company Website")
                GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEvent(CompanyDetailEvent.OpenResource(company.website)) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                                .background(LiquidColors.Primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Language, null, tint = LiquidColors.Primary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(company.website, fontSize = 13.sp, color = LiquidColors.Primary, modifier = Modifier.weight(1f), maxLines = 1)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = LiquidColors.Primary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Reference links
        if (company.referenceLinks.isNotEmpty()) {
            Column {
                LiquidSectionHeader(title = "Reference Links")
                company.referenceLinks.forEach { link ->
                    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEvent(CompanyDetailEvent.OpenResource(link.url)) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape)
                                    .background(LiquidColors.Primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Link, null, tint = LiquidColors.Primary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(link.label.ifBlank { link.url }, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LiquidColors.TextPrimary)
                                Text(link.url, fontSize = 11.sp, color = LiquidColors.TextSecondary, maxLines = 1)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = LiquidColors.Primary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        // PDF attachments
        if (company.pdfAttachments.isNotEmpty()) {
            Column {
                LiquidSectionHeader(title = "PDF Resources")
                company.pdfAttachments.forEach { pdf ->
                    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEvent(CompanyDetailEvent.OpenResource(pdf.storageUrl)) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape)
                                    .background(Color(0xFF7C3AED).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PictureAsPdf, null, tint = Color(0xFF7C3AED), modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(pdf.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LiquidColors.TextPrimary)
                                if (pdf.sizeKb > 0)
                                    Text("${pdf.sizeKb} KB", fontSize = 11.sp, color = LiquidColors.TextSecondary)
                            }
                            Icon(Icons.Default.Download, null, tint = LiquidColors.Primary, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun LiquidSectionHeader(title: String, isLive: Boolean = false, action: String? = null, onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary, modifier = Modifier.weight(1f))
        
        if (isLive) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(LiquidColors.Primary.copy(alpha=0.15f))
                    .border(1.dp, LiquidColors.Primary.copy(alpha=0.3f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("LIVE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = LiquidColors.Primary)
            }
        }
        
        if (action != null) {
            Text(
                text = action,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = LiquidColors.Primary,
                modifier = Modifier.clickable { onAction() }.padding(4.dp)
            )
        }
    }
}

// ── PREVIEWS
private const val PIXEL6 = "spec:width=412dp,height=892dp,dpi=560,isRound=false,chinSize=0dp,orientation=portrait"

@Preview(name = "Liquid UI · Overview", device = PIXEL6)
@Composable private fun PreviewLiquidOverview() = MaterialTheme {
    CompanyDetailContent(CompanyDetailState(isLoading = false, company = SampleCompanyData.google, selectedTab = CompanyDetailTab.OVERVIEW), {})
}

@Preview(name = "Liquid UI · Process", device = PIXEL6)
@Composable private fun PreviewLiquidProcess() = MaterialTheme {
    CompanyDetailContent(CompanyDetailState(isLoading = false, company = SampleCompanyData.stripe, selectedTab = CompanyDetailTab.PROCESS), {})
}


// ═══════════════════════════════════════════════════════════════════════════════
// BATCH REVIEWS TAB
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun BatchReviewTab(
    state: BatchReviewState,
    onEvent: (BatchReviewEvent) -> Unit,
    onViewPlacedStudents: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // ── Header row: title + admin "Add Batch" button ──────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LiquidSectionHeader(title = "Batch-wise Reviews")
            if (state.isAdmin) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(LiquidColors.Primary.copy(alpha = 0.15f))
                        .border(1.dp, LiquidColors.Primary.copy(alpha = 0.4f), RoundedCornerShape(50.dp))
                        .clickable { onEvent(BatchReviewEvent.OpenAddBatchSheet) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Add, null, tint = LiquidColors.Primary, modifier = Modifier.size(14.dp))
                        Text("Add Batch", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LiquidColors.Primary)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        // ── Loading state ─────────────────────────────────────────────────────
        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LiquidColors.Primary, modifier = Modifier.size(32.dp))
            }
            return@Column
        }

        // ── Empty state ───────────────────────────────────────────────────────
        if (state.batchReports.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("📭", fontSize = 40.sp)
                    Text("No batch data yet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
                    Text(
                        "Batch-wise shortlist counts, questions, and student reviews will appear here once added.",
                        fontSize = 13.sp,
                        color = LiquidColors.TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
            return@Column
        }

        // ── Batch Selector Chips ──────────────────────────────────────────────
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            items(state.batchReports) { report ->
                val isSelected = report.batchYear == state.selectedBatchYear
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (isSelected) LiquidColors.Primary.copy(alpha = 0.2f) else LiquidColors.GlassSurface)
                        .border(1.dp, if (isSelected) LiquidColors.Primary else LiquidColors.GlassBorder, RoundedCornerShape(50.dp))
                        .clickable { onEvent(BatchReviewEvent.SelectBatch(report.batchYear)) }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        "${report.batchYear} Batch",
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) LiquidColors.Primary else LiquidColors.TextSecondary
                    )
                }
            }
        }

        // ── Selected batch details ────────────────────────────────────────────
        val selectedReport = state.batchReports.find { it.batchYear == state.selectedBatchYear }
        if (selectedReport != null) {
            BatchReportCard(
                report = selectedReport,
                onViewPlacedStudents = { onViewPlacedStudents(selectedReport.batchYear) }
            )
        }

        // ── Approved Feedback for selected batch ──────────────────────────────
        val feedbackCount = state.approvedFeedback.size
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LiquidSectionHeader(title = "Student Reviews ($feedbackCount)")

            if (state.approvedFeedback.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No approved reviews for this batch yet.", fontSize = 13.sp, color = LiquidColors.TextSecondary, textAlign = TextAlign.Center)
                    }
                }
            } else {
                // Average rating banner
                val avg = state.approvedFeedback.map { it.rating }.average()
                GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(String.format("%.1f", avg), fontSize = 36.sp, fontWeight = FontWeight.Black, color = LiquidColors.Primary)
                            StarRatingRow(avg.toFloat(), size = 14.dp)
                            Text("avg rating", fontSize = 10.sp, color = LiquidColors.TextMuted)
                        }
                        Box(Modifier.width(1.dp).height(64.dp).background(LiquidColors.GlassBorder))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                state.approvedFeedback.first().experience.take(120) + if (state.approvedFeedback.first().experience.length > 120) "…" else "",
                                fontSize = 13.sp, color = LiquidColors.TextPrimary, lineHeight = 20.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("${state.approvedFeedback.first().batchYear} Batch", fontSize = 11.sp, color = LiquidColors.TextMuted)
                        }
                    }
                }

                // Individual feedback cards
                state.approvedFeedback.forEach { feedback ->
                    ApprovedFeedbackCard(feedback)
                }
            }
        }

        // ── Submit Review CTA ─────────────────────────────────────────────────
        if (state.isLoggedIn && !state.hasUserSubmitted) {
            Button(
                onClick = { onEvent(BatchReviewEvent.OpenSubmitSheet) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RateReview, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text("Submit Your Review", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }
                }
            }
        } else if (state.isLoggedIn && state.hasUserSubmitted) {
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                    Text("You've already submitted a review for this company.", fontSize = 13.sp, color = LiquidColors.TextSecondary)
                }
            }
        }

        // ── Admin: Pending Submissions ────────────────────────────────────────
        if (state.isAdmin && state.pendingFeedback.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier.size(22.dp).clip(CircleShape).background(Color(0xFFF59E0B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.pendingFeedback.size.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Text("Pending Reviews", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    Text("(Admin Only)", fontSize = 11.sp, color = LiquidColors.TextMuted)
                }

                state.pendingFeedback.forEach { submission ->
                    PendingFeedbackCard(
                        submission = submission,
                        isReviewing = state.isReviewing,
                        onApprove = { onEvent(BatchReviewEvent.ApproveFeedback(submission.id)) },
                        onReject = { onEvent(BatchReviewEvent.RejectFeedback(submission.id)) }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }

    // ── Submit Feedback Bottom Sheet ──────────────────────────────────────────
    if (state.showSubmitSheet) {
        SubmitFeedbackSheet(
            form = state.submitForm,
            isSubmitting = state.isSubmitting,
            submitError = state.submitError,
            onEvent = onEvent
        )
    }

    // ── Admin: Add Batch Report Sheet ─────────────────────────────────────────
    if (state.showAddBatchSheet && state.isAdmin) {
        AddBatchReportSheet(
            form = state.addBatchForm,
            isSaving = state.isSavingBatch,
            onEvent = onEvent
        )
    }
}

// ─── Batch Report Detail Card ─────────────────────────────────────────────────
@Composable
private fun BatchReportCard(report: BatchReport, onViewPlacedStudents: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BatchStatChip(
                    label = "SHORTLISTED",
                    value = if (report.shortlistedCount > 0) report.shortlistedCount.toString() else "—",
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
                BatchStatChip(
                    label = "SELECTED",
                    value = if (report.selectedCount > 0) report.selectedCount.toString() else "—",
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }

            // Questions asked
            if (report.questionsAsked.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "QUESTIONS ASKED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidColors.TextMuted,
                        letterSpacing = 0.5.sp
                    )
                    report.questionsAsked.forEachIndexed { idx, question ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(LiquidColors.Primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${idx + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LiquidColors.Primary)
                            }
                            Text(question, fontSize = 13.sp, color = LiquidColors.TextPrimary, lineHeight = 20.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Other info
            if (report.otherInfo.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("OTHER INFO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextMuted, letterSpacing = 0.5.sp)
                    Text(report.otherInfo, fontSize = 13.sp, color = LiquidColors.TextPrimary, lineHeight = 20.sp)
                }
            }

            // Action buttons
            Button(
                onClick = onViewPlacedStudents,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary.copy(alpha = 0.1f)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text("View Placed Students", fontWeight = FontWeight.Bold, color = LiquidColors.Primary)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = LiquidColors.Primary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun BatchStatChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = color)
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color.copy(alpha = 0.7f), letterSpacing = 0.4.sp)
        }
    }
}

// ─── Approved Feedback Card ───────────────────────────────────────────────────
@Composable
private fun ApprovedFeedbackCard(feedback: FeedbackSubmission) {
    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Author + rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape)
                            .background(LiquidColors.Primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(feedback.userName.take(1).uppercase(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LiquidColors.Primary)
                    }
                    Column {
                        Text(feedback.userName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LiquidColors.TextPrimary)
                        Text("${feedback.batchYear} Batch", fontSize = 11.sp, color = LiquidColors.TextSecondary)
                    }
                }
                StarRatingRow(feedback.rating.toFloat(), size = 13.dp)
            }

            // Experience
            Text(feedback.experience, fontSize = 13.sp, color = LiquidColors.TextPrimary, lineHeight = 20.sp)

            // Questions encountered
            if (feedback.questionsEncountered.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Questions Encountered:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextMuted)
                    feedback.questionsEncountered.forEach { q ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
                            Text("•", fontSize = 12.sp, color = LiquidColors.Primary)
                            Text(q, fontSize = 12.sp, color = LiquidColors.TextSecondary, lineHeight = 18.sp)
                        }
                    }
                }
            }

            // Other info
            if (feedback.otherInfo.isNotBlank()) {
                Text(feedback.otherInfo, fontSize = 12.sp, color = LiquidColors.TextMuted, lineHeight = 18.sp)
            }
        }
    }
}

// ─── Pending Feedback Card (Admin) ────────────────────────────────────────────
@Composable
private fun PendingFeedbackCard(
    submission: FeedbackSubmission,
    isReviewing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Pending badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("PENDING REVIEW", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B), letterSpacing = 0.5.sp)
            }

            // Author
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(submission.userName.take(1).uppercase(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                }
                Column {
                    Text(submission.userName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LiquidColors.TextPrimary)
                    Text("${submission.userEmail} · ${submission.batchYear} Batch", fontSize = 11.sp, color = LiquidColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            StarRatingRow(submission.rating.toFloat(), size = 13.dp)
            Text(submission.experience, fontSize = 13.sp, color = LiquidColors.TextPrimary, lineHeight = 20.sp)

            if (submission.questionsEncountered.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Questions:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextMuted)
                    submission.questionsEncountered.forEach { q ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("•", fontSize = 12.sp, color = LiquidColors.TextMuted)
                            Text(q, fontSize = 12.sp, color = LiquidColors.TextSecondary)
                        }
                    }
                }
            }

            if (submission.otherInfo.isNotBlank()) {
                Text(submission.otherInfo, fontSize = 12.sp, color = LiquidColors.TextMuted)
            }

            // Approve / Reject buttons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onApprove,
                    enabled = !isReviewing,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    if (isReviewing) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    else { Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text("Approve", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                }
                OutlinedButton(
                    onClick = onReject,
                    enabled = !isReviewing,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Close, null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reject", fontSize = 13.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── Star Rating Row ──────────────────────────────────────────────────────────
@Composable
private fun StarRatingRow(rating: Float, size: androidx.compose.ui.unit.Dp = 14.dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(5) { i ->
            Icon(
                if (i < rating.toInt()) Icons.Default.Star else if (i < rating) Icons.Default.StarHalf else Icons.Default.StarBorder,
                null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(size)
            )
        }
    }
}

// ─── Submit Feedback Bottom Sheet ────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubmitFeedbackSheet(
    form: SubmitFeedbackForm,
    isSubmitting: Boolean,
    submitError: String?,
    onEvent: (BatchReviewEvent) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { onEvent(BatchReviewEvent.CloseSubmitSheet) },
        containerColor = LiquidColors.BgBase,
        dragHandle = {
            Box(Modifier.padding(vertical = 12.dp)) {
                Box(Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(50.dp)).background(LiquidColors.GlassBorder))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Sheet handle label ────────────────────────────────────────────
            Text(
                "Submit Your Review",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = LiquidColors.TextPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                "Your review will be visible after admin approval.",
                fontSize = 13.sp,
                color = LiquidColors.TextSecondary
            )

            // ── Batch Year ────────────────────────────────────────────────────
            OutlinedTextField(
                value = form.batchYear,
                onValueChange = { onEvent(BatchReviewEvent.BatchYearChanged(it)) },
                label = { Text("Your Batch Year (e.g. 2025) *", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.School, null, tint = LiquidColors.Primary, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = liquidTextFieldColors()
            )

            // Star Rating Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Overall Rating *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LiquidColors.TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(5) { i ->
                        val star = i + 1
                        Icon(
                            if (star <= form.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            null,
                            tint = if (star <= form.rating) Color(0xFFF59E0B) else LiquidColors.TextMuted,
                            modifier = Modifier.size(36.dp).clickable { onEvent(BatchReviewEvent.RatingChanged(star)) }
                        )
                    }
                }
            }

            // ── Experience ────────────────────────────────────────────────────
            OutlinedTextField(
                value = form.experience,
                onValueChange = { onEvent(BatchReviewEvent.ExperienceChanged(it)) },
                label = { Text("Your Experience *", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.RateReview, null, tint = LiquidColors.Primary, modifier = Modifier.size(18.dp)) },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = liquidTextFieldColors()
            )

            // Questions Encountered
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Questions You Encountered", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LiquidColors.TextPrimary)
                if (form.questionsEncountered.isNotEmpty()) {
                    form.questionsEncountered.forEachIndexed { idx, q ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(LiquidColors.GlassSurface)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(q, fontSize = 13.sp, color = LiquidColors.TextPrimary, modifier = Modifier.weight(1f))
                            Icon(
                                Icons.Default.Close, null,
                                tint = LiquidColors.TextMuted,
                                modifier = Modifier.size(16.dp).clickable { onEvent(BatchReviewEvent.RemoveQuestion(idx)) }
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = form.currentQuestion,
                        onValueChange = { onEvent(BatchReviewEvent.CurrentQuestionChanged(it)) },
                        label = { Text("Add a question", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = liquidTextFieldColors()
                    )
                    Box(
                        modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp))
                            .background(LiquidColors.Primary.copy(alpha = 0.15f))
                            .border(1.dp, LiquidColors.Primary.copy(0.4f), RoundedCornerShape(10.dp))
                            .clickable { onEvent(BatchReviewEvent.AddQuestion) },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Add, null, tint = LiquidColors.Primary) }
                }
            }

            // ── Other info ────────────────────────────────────────────────────
            OutlinedTextField(
                value = form.otherInfo,
                onValueChange = { onEvent(BatchReviewEvent.OtherInfoChanged(it)) },
                label = { Text("Other Info (optional)", fontSize = 12.sp) },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = liquidTextFieldColors()
            )

            // Error
            if (submitError != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEF4444).copy(0.1f)).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    Text(submitError, fontSize = 12.sp, color = Color(0xFFEF4444))
                }
            }

            // Submit button
            Button(
                onClick = { onEvent(BatchReviewEvent.ConfirmSubmit) },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Submit for Review", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                }
            }
        }
    }
}

// ─── Admin: Add Batch Report Sheet ───────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBatchReportSheet(
    form: AddBatchReportForm,
    isSaving: Boolean,
    onEvent: (BatchReviewEvent) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { onEvent(BatchReviewEvent.CloseAddBatchSheet) },
        containerColor = LiquidColors.BgBase,
        dragHandle = {
            Box(Modifier.padding(vertical = 12.dp)) {
                Box(Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(50.dp)).background(LiquidColors.GlassBorder))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Add Batch Report",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = LiquidColors.TextPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text("Admin only — visible to all students.", fontSize = 13.sp, color = LiquidColors.TextSecondary)

            val fieldColors = liquidTextFieldColors()

            OutlinedTextField(
                value = form.batchYear, onValueChange = { onEvent(BatchReviewEvent.AddBatchYearChanged(it)) },
                label = { Text("Batch Year * (e.g. 2025)", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = LiquidColors.Primary, modifier = Modifier.size(18.dp)) },
                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = form.shortlistedCount, onValueChange = { onEvent(BatchReviewEvent.AddShortlistedChanged(it)) },
                    label = { Text("Shortlisted", fontSize = 12.sp) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = fieldColors
                )
                OutlinedTextField(
                    value = form.selectedCount, onValueChange = { onEvent(BatchReviewEvent.AddSelectedChanged(it)) },
                    label = { Text("Selected", fontSize = 12.sp) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = fieldColors
                )
            }

            // Questions
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Questions Asked", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LiquidColors.TextPrimary)
                form.questionsAsked.forEachIndexed { idx, q ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(LiquidColors.GlassSurface).padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(q, fontSize = 13.sp, color = LiquidColors.TextPrimary, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Close, null, tint = LiquidColors.TextMuted,
                            modifier = Modifier.size(16.dp).clickable { onEvent(BatchReviewEvent.RemoveBatchQuestion(idx)) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = form.currentQuestion, onValueChange = { onEvent(BatchReviewEvent.AddBatchQuestionChanged(it)) },
                        label = { Text("Add question", fontSize = 12.sp) }, singleLine = true,
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = fieldColors
                    )
                    Box(
                        modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp))
                            .background(LiquidColors.Primary.copy(0.15f)).border(1.dp, LiquidColors.Primary.copy(0.4f), RoundedCornerShape(10.dp))
                            .clickable { onEvent(BatchReviewEvent.AddBatchQuestion) },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Add, null, tint = LiquidColors.Primary) }
                }
            }

            OutlinedTextField(
                value = form.otherInfo, onValueChange = { onEvent(BatchReviewEvent.AddBatchOtherInfoChanged(it)) },
                label = { Text("Other Info (optional)", fontSize = 12.sp) }, minLines = 2, maxLines = 4,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors
            )

            Button(
                onClick = { onEvent(BatchReviewEvent.ConfirmAddBatch) },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Save Batch Report", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            }
        }
    }
}
