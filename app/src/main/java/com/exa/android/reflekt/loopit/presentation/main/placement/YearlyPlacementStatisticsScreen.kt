package com.exa.android.reflekt.loopit.presentation.main.placement

import androidx.activity.compose.BackHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.composed
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.core.utils.ShareUtils
import com.exa.android.reflekt.loopit.presentation.main.placement.components.*
import com.exa.android.reflekt.loopit.presentation.main.placement.model.CompanyDetail
import com.exa.android.reflekt.loopit.presentation.main.placement.model.RoleType
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacedStudentEvent
import com.exa.android.reflekt.loopit.presentation.main.placement.vm.PlacedStudentViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle



// ── Data models used by section composables ───────────────────────────────────
data class SalaryBucket(val range: String, val count: Int, val maxCount: Int, val isHighlighted: Boolean = false, val minLpa: Float, val maxLpa: Float)
data class TimelineEvent(val company: String, val type: String, val date: String, val offers: Int, val isPrimary: Boolean = false)

fun Modifier.shimmerLoadingAnimation(isLoading: Boolean = true): Modifier = composed {
    if (!isLoading) return@composed this
    
    var size by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "shimmer_x"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.08f),
                Color.Transparent
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    )
    .onGloballyPositioned { size = it.size }
}

/**
 * Derives [SalaryBucket] counts from the [packageDistribution] map.
 */
private fun buildSalaryBuckets(packageDistribution: Map<String, Int>): List<SalaryBucket> {
    if (packageDistribution.isEmpty()) return emptyList()

    var lt4 = 0; var b4_5 = 0; var b5_7 = 0; var b7_10 = 0; var b10_15 = 0; var b15_20 = 0; var gt20 = 0

    packageDistribution.forEach { (packageStr, count) ->
        val lpa = packageStr.toFloatOrNull() ?: return@forEach
        when {
            lpa < 4f  -> lt4 += count
            lpa < 5f  -> b4_5 += count
            lpa < 7f  -> b5_7 += count
            lpa < 10f -> b7_10 += count
            lpa < 15f -> b10_15 += count
            lpa < 20f -> b15_20 += count
            else      -> gt20 += count
        }
    }

    val maxBucket = maxOf(lt4, b4_5, b5_7, b7_10, b10_15, b15_20, gt20)
    val peak = maxBucket.coerceAtLeast(1)

    return listOf(
        SalaryBucket("<4", lt4, peak, isHighlighted = lt4 == maxBucket && lt4 > 0, minLpa = 0f, maxLpa = 3.99f),
        SalaryBucket("4-5", b4_5, peak, isHighlighted = b4_5 == maxBucket && b4_5 > 0, minLpa = 4f, maxLpa = 4.99f),
        SalaryBucket("5-7", b5_7, peak, isHighlighted = b5_7 == maxBucket && b5_7 > 0, minLpa = 5f, maxLpa = 6.99f),
        SalaryBucket("7-10", b7_10, peak, isHighlighted = b7_10 == maxBucket && b7_10 > 0, minLpa = 7f, maxLpa = 9.99f),
        SalaryBucket("10-15", b10_15, peak, isHighlighted = b10_15 == maxBucket && b10_15 > 0, minLpa = 10f, maxLpa = 14.99f),
        SalaryBucket("15-20", b15_20, peak, isHighlighted = b15_20 == maxBucket && b15_20 > 0, minLpa = 15f, maxLpa = 19.99f),
        SalaryBucket("20+", gt20, peak, isHighlighted = gt20 == maxBucket && gt20 > 0, minLpa = 20f, maxLpa = 100f)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearlyPlacementStatisticsScreen(
    onBack: () -> Unit = {},
    onViewTimelineClick: () -> Unit = {},
    onViewCompaniesClick: () -> Unit = {},
    onViewAlumniClick: () -> Unit = {},
    onViewAlumniWithFiltersClick: (Int?, Float, Float) -> Unit = { _, _, _ -> },
    onCompareBatchesClick: () -> Unit = {},
    onAlumniBatchSelected: (Int?) -> Unit = {},
    alumniSelectedBatch: Int? = null,
    isAdmin: Boolean = false,
    onAddCompanyClick: () -> Unit = {},
    onSeedDataClick: () -> Unit = {},
    isSeeding: Boolean = false,
    seedResult: String? = null,
    onDismissSeedResult: () -> Unit = {},
    companies: List<CompanyDetail> = emptyList(),
    onCompanyClick: (String) -> Unit = {},
    onEventClick: (String) -> Unit = {},
    onAiPredictionClick: () -> Unit = {},
    placedStudentViewModel: PlacedStudentViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val pullState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    BackHandler {
        onBack()
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            placedStudentViewModel.onEvent(PlacedStudentEvent.Load()) // Refresh data
            scope.launch {
                delay(2000)
                isRefreshing = false
            }
        },
        state = pullState,
        modifier = Modifier.fillMaxSize().background(LiquidColors.BgBase),
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullState,
                isRefreshing = isRefreshing,
                color = LiquidColors.Primary,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
            )
        }
    ) {
        // Animated Mesh Background

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 160.dp)
        ) {
            val studentState by placedStudentViewModel.state.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                placedStudentViewModel.onEvent(PlacedStudentEvent.Load())
            }
            LaunchedEffect(alumniSelectedBatch) {
                placedStudentViewModel.onEvent(PlacedStudentEvent.BatchSelected(alumniSelectedBatch))
            }

            HeaderSection(
                onBack = onBack,
                onCompareClick = onCompareBatchesClick,
                onAiPredictionClick = onAiPredictionClick,
                availableBatches = studentState.availableBatches,
                selectedBatch = studentState.selectedBatch,
                onBatchSelected = { batch ->
                    placedStudentViewModel.onEvent(PlacedStudentEvent.BatchSelected(batch))
                    onAlumniBatchSelected(batch)
                }
            )



            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                KPISummarySection(studentState)

                LiveOnCampusSection(
                    liveEvents = studentState.liveEvents, 
                    onCompanyClick = { onCompanyClick(it) },
                    onEventClick = { onEventClick(it) }
                )


                // ── Salary Distribution derived from packageDistribution
                val buckets = buildSalaryBuckets(studentState.packageDistribution)
                if (buckets.isNotEmpty()) {
                    SalaryDistributionSection(
                        buckets = buckets,
                        isLoading = studentState.isLoading,
                        onBucketClick = { bucket ->
                            onViewAlumniWithFiltersClick(studentState.selectedBatch, bucket.minLpa, bucket.maxLpa)
                        }
                    )
                }

                Column {
                    SectionTitle(
                        title = "Top Recruiting Companies",
                        trailingContent = {
                            Text(
                                text = "View All",
                                color = LiquidColors.Primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickableWithNoIndication { onViewCompaniesClick() }
                                    .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                            )
                        }
                    )
                    if (companies.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(companies.take(8)) { company ->
                                FirestoreCompanyCard(
                                    company = company,
                                    onClick = { onCompanyClick(company.id) },
                                    onShareClick = {
                                        scope.launch {
                                            val shareData = ShareUtils.prepareCompanyShareData(context, company)
                                            ShareUtils.executeShare(context, shareData)
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        // Empty state — data comes from Firestore after seeding
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(LiquidColors.GlassSurface)
                                .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No companies yet — seed data to populate",
                                fontSize = 13.sp,
                                color = LiquidColors.TextSecondary
                            )
                        }
                    }
                }

                val timelinePreview = listOf(
                    TimelineEvent(
                        company = "EPAM",
                        type = "Software Engineer",
                        date = "Mid July",
                        offers = 0,
                        isPrimary = false
                    ),
                    TimelineEvent(
                        company = "Infosys HWI",
                        type = "System Engineer",
                        date = "Mid July",
                        offers = 0,
                        isPrimary = false
                    ),
                    TimelineEvent(
                        company = "Cisco",
                        type = "Software Engineer",
                        date = "Late July",
                        offers = 0,
                        isPrimary = false
                    ),
                    TimelineEvent(
                        company = "Quantiphi",
                        type = "Framework Engineer",
                        date = "Late July",
                        offers = 0,
                        isPrimary = false
                    ),
                    TimelineEvent(
                        company = "Carwale",
                        type = "SDE 1",
                        date = "Mid Sept",
                        offers = 0,
                        isPrimary = false
                    )
                )
                if (timelinePreview.isNotEmpty()) VisitTimelineSection(timelinePreview, onViewTimelineClick)

                if (studentState.students.isNotEmpty()) {
                    PlacedStudentsPreviewSection(
                        students = studentState.students,
                        availableBatches = studentState.availableBatches,
                        selectedBatch = studentState.selectedBatch,
                        onBatchSelected = { onAlumniBatchSelected(it) },
                        onViewAll = onViewAlumniClick
                    )
                }
            }
        }

        // Admin FABs (only visible when isAdmin = true)
        if (isAdmin) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 24.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                // ── Seed Data FAB
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSeeding)
                                Brush.radialGradient(listOf(Color(0xFF374151), Color(0xFF374151)))
                            else
                                Brush.radialGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                        )
                        .border(2.dp, Color(0xFF10B981).copy(alpha = 0.35f), CircleShape)
                        .clickableWithNoIndication(enabled = !isSeeding) { onSeedDataClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSeeding) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = "Seed Data",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // ── Add Company FAB
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Color(0xFF7C3AED), Color(0xFF7C3AED).copy(alpha = 0.6f))))
                        .border(2.dp, Color(0xFF7C3AED).copy(alpha = 0.3f), CircleShape)
                        .clickable { onAddCompanyClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, "Add Company", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }

        // Seed Result Snackbar
        seedResult?.let { msg ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 90.dp, start = 24.dp, end = 24.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (msg.startsWith("✅")) Color(0xFF065F46) else Color(0xFF7F1D1D))
                    .clickable { onDismissSeedResult() }
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(msg, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}


// ── 2. Header & Glassmorphism ─────────────────────────────────────────────────
@Composable
private fun HeaderSection(
    onBack: () -> Unit,
    onCompareClick: () -> Unit,
    onAiPredictionClick: () -> Unit,
    availableBatches: List<Int>,
    selectedBatch: Int?,
    onBatchSelected: (Int?) -> Unit
) {
    var isComparing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
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

            Text(
                text = "Placement Stats",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = LiquidColors.TextPrimary
            )

            IconButton(
                onClick = { onAiPredictionClick() },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LiquidColors.GlassSurface)
                    .border(1.dp, LiquidColors.GlassBorder, CircleShape)
            ) {
                Icon(androidx.compose.material.icons.Icons.Default.AutoAwesome, "AI Insights", tint = LiquidColors.TextPrimary)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Scrollable Batch chips
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50.dp))
                    .background(LiquidColors.GlassSurface)
                    .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(50.dp))
                    .padding(4.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                availableBatches.forEach { batch ->
                    val isSelected = selectedBatch == batch && !isComparing
                    val bg = if (isSelected) LiquidColors.GlassHighlight else Color.Transparent
                    
                    val shortYearStart = (batch - 1) % 100
                    val shortYearEnd = batch % 100
                    val batchText = "Batch '${shortYearStart}-${shortYearEnd}"
                    
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(bg)
                            .clickableWithNoIndication {
                                isComparing = false
                                onBatchSelected(batch)
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = batchText,
                            color = if (isSelected) LiquidColors.Primary else LiquidColors.TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Compare Batches Button
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isComparing) LiquidColors.Primary.copy(alpha = 0.15f) else LiquidColors.GlassSurface)
                    .border(1.dp, if (isComparing) LiquidColors.Primary.copy(alpha = 0.5f) else LiquidColors.GlassBorder, RoundedCornerShape(50.dp))
                    .clickableWithNoIndication {
                        isComparing = true
                        onCompareClick()
                    }
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Compare",
                    color = if (isComparing) LiquidColors.Primary else LiquidColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = if (isComparing) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

// ── 3. KPI Summary (Glass Cards) ───────────────────────────────────────────────
@Composable
private fun KPISummarySection(state: com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacedStudentState) {

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.shimmerLoadingAnimation(isLoading = state.isLoading)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avg Package Card
            GlassCard(modifier = Modifier.weight(1f).height(115.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Avg Package", fontSize = 13.sp, color = LiquidColors.TextSecondary)
                        Icon(Icons.Default.TrendingUp, null, tint = LiquidColors.Primary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        val formattedAvg = if (state.avgPackageLpa > 0) String.format("%.2f", state.avgPackageLpa) else "-"
                        Text("₹$formattedAvg", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
                        Text(" LPA", fontSize = 14.sp, color = LiquidColors.TextSecondary, modifier = Modifier.padding(bottom = 4.dp))
                    }
                }
            }

            // Placement % Card (or Total Placed)
            GlassCard(modifier = Modifier.weight(1f).height(115.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Placed", fontSize = 13.sp, color = LiquidColors.TextSecondary)
                        Icon(Icons.Default.Groups, null, tint = LiquidColors.Primary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("${state.totalPlaced}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
                    Text("Students", fontSize = 12.sp, color = LiquidColors.TextSecondary, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        // Highest Package Card (Glowing Accent)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Brush.linearGradient(listOf(LiquidColors.Primary.copy(alpha=0.15f), Color.Transparent)))
                .border(1.dp, LiquidColors.Primary.copy(alpha=0.3f), RoundedCornerShape(32.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Highest Domestic Package", fontSize = 13.sp, color = LiquidColors.TextSecondary, modifier = Modifier.padding(bottom = 8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        val topVal = if (state.topPackageLpa > 0) String.format("%.1f", state.topPackageLpa) else "-"
                        Text("₹$topVal", fontSize = 36.sp, fontWeight = FontWeight.Black, color = LiquidColors.TextPrimary)
                        Text(" LPA", fontSize = 16.sp, color = LiquidColors.Primary, modifier = Modifier.padding(bottom = 5.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(LiquidColors.Green))
                        Spacer(modifier = Modifier.width(8.dp))
                        val offeredText = if (state.topStudentName.isNotBlank() && state.topCompanyName.isNotBlank()) {
                            "${state.topStudentName} @ ${state.topCompanyName}"
                        } else if (state.topCompanyName.isNotBlank()) {
                            "Offered by ${state.topCompanyName}"
                        } else {
                            "Top Package Details TBA"
                        }
                        Text(offeredText, fontSize = 13.sp, color = LiquidColors.TextPrimary.copy(alpha=0.8f))
                    }
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(LiquidColors.Primary.copy(alpha=0.1f))
                        .border(1.dp, LiquidColors.Primary.copy(alpha=0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.EmojiEvents, null, tint = LiquidColors.Primary, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

// ── 4. Salary Distribution (Spring Animated Bars) ─────────────────────────────
@Composable
private fun SalaryDistributionSection(
    buckets: List<SalaryBucket>,
    isLoading: Boolean = false,
    onBucketClick: (SalaryBucket) -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnimation = true }

    Column(modifier = Modifier.shimmerLoadingAnimation(isLoading = isLoading)) {
        SectionTitle("Salary Range Distribution")

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                buckets.forEach { bucket ->
                    val targetHeight = if (startAnimation) {
                        val ratio = bucket.count.toFloat() / bucket.maxCount.toFloat()
                        if (bucket.count > 0) ratio.coerceAtLeast(0.12f) else 0f
                    } else 0f
                    val animatedHeight by animateFloatAsState(
                        targetValue = targetHeight,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "BarHeight"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickableWithNoIndication { onBucketClick(bucket) }
                    ) {
                        val barWeight = animatedHeight.coerceAtLeast(0.001f)
                        val spacerWeight = (1f - animatedHeight).coerceAtLeast(0.001f)

                        Spacer(modifier = Modifier.weight(spacerWeight))

                        Text(
                            text = bucket.count.toString(),
                            fontSize = 12.sp,
                            fontWeight = if (bucket.isHighlighted) FontWeight.Bold else FontWeight.Medium,
                            color = if (bucket.isHighlighted) LiquidColors.Primary else LiquidColors.TextPrimary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        // Glowing Pill Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .weight(barWeight)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    if (bucket.isHighlighted) Brush.verticalGradient(listOf(LiquidColors.Primary, LiquidColors.Primary.copy(alpha = 0.5f)))
                                    else Brush.verticalGradient(listOf(LiquidColors.Primary.copy(alpha = 0.5f), LiquidColors.Primary.copy(alpha = 0.15f)))
                                )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = bucket.range, 
                            fontSize = 10.sp, 
                            color = if (bucket.isHighlighted) LiquidColors.Primary else LiquidColors.TextSecondary, 
                            maxLines = 1,
                            fontWeight = if (bucket.isHighlighted) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
        Text(
            text = "*Note: Click on bars to see placed students",
            fontSize = 11.sp,
            color = LiquidColors.TextSecondary,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
        )
    }
}


@Composable
private fun FirestoreCompanyCard(
    company: com.exa.android.reflekt.loopit.presentation.main.placement.model.CompanyDetail,
    onClick: () -> Unit,
    onShareClick: () -> Unit = {}
) {
    val packageText = company.packageLpa?.let { lpaString ->
        if (lpaString.contains("LPA", ignoreCase = true) || lpaString.contains("₹")) {
            lpaString.trim()
        } else {
            "₹${lpaString.trim()} LPA"
        }
    } ?: company.stipendPerMonth?.let { "₹$it/mo" } ?: "TBA"

    val isLive = company.isLiveProcess

    Box(
        modifier = Modifier
            .width(200.dp) // increased slightly to prevent text cutoff
            .height(210.dp) // increased height to prevent vertical clipping
            .clip(RoundedCornerShape(20.dp))
            .background(LiquidColors.GlassSurface)
            .border(1.dp, if (isLive) LiquidColors.Primary.copy(alpha = 0.5f) else LiquidColors.GlassBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Row: Logo & Save Icon
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Logo Container
                CompanyLogo(
                    logoUrl = company.logoUrl,
                    companyName = company.name,
                    initials = company.name.take(2).uppercase(),
                    containerSize = 48.dp,
                    imageSize = 32.dp,
                    cornerRadius = 12.dp
                )
                
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = LiquidColors.TextSecondary,
                    modifier = Modifier
                        .size(24.dp)
                        .clickableWithNoIndication { onShareClick() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Company Name & Role
            Text(
                text = company.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = LiquidColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = company.jobRole.ifBlank { "Software Engineer" },
                fontSize = 14.sp,
                color = LiquidColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(color = LiquidColors.GlassBorder.copy(alpha=0.3f), thickness = 1.dp, modifier = Modifier.padding(bottom = 12.dp))
            
            // Bottom Row: Package & AVG tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = packageText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LiquidColors.Primary
                )
                
//                Box(
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(6.dp))
//                        .background(androidx.compose.ui.graphics.Color(0xFF334155)) // Dark grey tag background
//                        .padding(horizontal = 8.dp, vertical = 4.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = "AVG",
//                        color = androidx.compose.ui.graphics.Color.White,
//                        fontSize = 11.sp,
//                        fontWeight = FontWeight.Bold,
//                        letterSpacing = 0.5.sp
//                    )
//                }
            }
        }
    }
}

@Composable
private fun ViewMoreCompanyCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(100.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(LiquidColors.GlassSurface)
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(LiquidColors.Primary.copy(alpha = 0.1f))
                    .border(1.dp, LiquidColors.Primary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "View More", tint = LiquidColors.Primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("View All", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LiquidColors.Primary)
        }
    }
}

// ── 6. Visit Timeline (Organic Nodes) ─────────────────────────────────────────
@Composable
private fun VisitTimelineSection(events: List<TimelineEvent>, onViewTimelineClick: () -> Unit = {}) {
    Column {
        SectionTitle("Visit Timeline", subtitle = " • July - Dec")

        Box(modifier = Modifier.padding(start = 24.dp, top = 8.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                events.forEachIndexed { index, event ->
                    val isLast = index == events.lastIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                    ) {
                        // Node & Line Column
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(24.dp)
                        ) {
                            // Node
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(LiquidColors.BgBase)
                                    .border(
                                        4.dp,
                                        if (event.isPrimary) LiquidColors.Primary else LiquidColors.GlassBorder,
                                        CircleShape
                                    )
                            )

                            // Line (if not last)
                            if (!isLast) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .weight(1f)
                                        .background(LiquidColors.GlassBorder)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Content
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = if (isLast) 0.dp else 24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(event.company, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
                                    Text(event.type, fontSize = 13.sp, color = LiquidColors.TextSecondary, modifier = Modifier.padding(top=2.dp))
                                }
                                Text(event.date, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (event.isPrimary) LiquidColors.Primary else LiquidColors.TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onViewTimelineClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.GlassSurface, contentColor = LiquidColors.Primary),
            border = BorderStroke(1.dp, LiquidColors.GlassBorder)
        ) {
            Text("View Full Timeline", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
@Composable
private fun SectionTitle(
    title: String, 
    subtitle: String? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
            subtitle?.let {
                Text(it, fontSize = 14.sp, color = LiquidColors.TextSecondary, modifier = Modifier.padding(start = 4.dp))
            }
        }
        trailingContent?.invoke()
    }
}

@Preview(name = "Liquid UI – Placement Stats", device = "spec:width=412dp,height=892dp,dpi=560,isRound=false,chinSize=0dp,orientation=portrait")
@Composable
private fun YearlyPlacementStatisticsScreenLiquidPreview() {
    MaterialTheme {
        YearlyPlacementStatisticsScreen()
    }
}

// ─── Placed Students Preview Section (for home screen) ────────────────────────
@Composable
private fun PlacedStudentsPreviewSection(
    students: List<com.exa.android.reflekt.loopit.presentation.main.placement.model.PlacedStudent>,
    availableBatches: List<Int>,
    selectedBatch: Int?,
    onBatchSelected: (Int?) -> Unit,
    onViewAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Section header with "View All" link
        SectionTitle(
            title = "🎓 Placed Students",
            trailingContent = {
                Box(
                    modifier = Modifier
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(50.dp))
                        .background(LiquidColors.Primary.copy(alpha = 0.12f))
                        .clickable { onViewAll() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("View All", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LiquidColors.Primary)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = LiquidColors.Primary, modifier = Modifier.size(12.dp))
                    }
                }
            }
        )

        // Batch chips
        if (availableBatches.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    HomeBatchChip(label = "All", isSelected = selectedBatch == null, onClick = { onBatchSelected(null) })
                }
                items(availableBatches) { batch ->
                    HomeBatchChip(label = "$batch", isSelected = selectedBatch == batch, onClick = { onBatchSelected(batch) })
                }
            }
        }

        // Top 5 student cards
        if (students.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    .background(LiquidColors.GlassSurface)
                    .border(1.dp, LiquidColors.GlassBorder, androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("No placement records yet", fontSize = 13.sp, color = LiquidColors.TextSecondary)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                students.take(10).forEach { student ->
                    PlacedStudentCard(
                        student = student,
                        isAdmin = false,
                        onLinkedIn = {},
                        compact = true
                    )
                }
                // View All button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                        .background(LiquidColors.GlassSurface)
                        .border(1.dp, LiquidColors.GlassBorder, androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                        .clickable { onViewAll() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("View All Placed Students", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LiquidColors.Primary)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = LiquidColors.Primary, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeBatchChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(50.dp))
            .background(if (isSelected) LiquidColors.Primary.copy(0.18f) else LiquidColors.GlassSurface)
            .border(1.dp, if (isSelected) LiquidColors.Primary else LiquidColors.GlassBorder, androidx.compose.foundation.shape.RoundedCornerShape(50.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            "$label Batch".trim(),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) LiquidColors.Primary else LiquidColors.TextSecondary
        )
    }
}

// ── Success Stories Section ───────────────────────────────────────────────
@Composable
private fun SuccessStoriesSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Success Stories",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = LiquidColors.TextPrimary
            )
            Text(
                text = "View All",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = LiquidColors.Primary,
                modifier = Modifier.clickable { /* TODO: handle view all */ }
            )
        }

        val dummyData = listOf(
            Triple("Sarah Jenkins", "Software Engineer III", "₹45 LPA"),
            Triple("David Chen", "Product Manager", "₹42 LPA"),
            Triple("Priya Patel", "Cloud Architect", "₹38 LPA")
        )
        val dummyImages = listOf(
            "https://lh3.googleusercontent.com/aida-public/AB6AXuAQLXh9izKg51xQavkee7Z5nGwV9vDKceMVmUE3ECLByRPtEesG8wSsvBil_3nXfDcEj_kbztI9MQ6dCXbAQ6uxqoYwSZh2r82x1fi200whAB02kNe-KJMEZbaBd24RBXQEt8qsqW8C6c8Cb4crFoqMPqQ7mg_JJLgtV2V0ki3hCJYhm9hkEuDF3DTaXXlLO_LWm0n-5Nb9W-q1bxvINXd1WWp_iQYYrL97oHzzp38DU3NXTKuecm4rjT1gNucOO7MbYVmHTZfEC5nZ",
            "https://lh3.googleusercontent.com/aida-public/AB6AXuBLkzE0cAtF3jJdRziSlaRVbHR4QPgLsgi9jwCfCxQfsvnYCBpR1CFnTpTvHxm9Z92tu1LLSMnmXC73UO8SjIiktS23zu-JLYEb9iiLERZ9NoqMX1zsLn8I60mDZqlq4NxT4cXhUoYMLAD8D6i2w9Z3sR7Tm_KQHizbSUB4VX-u_uXfHxRTscwohpDSqxt3ASqoLgclQBe2FMVdrBXVHLpa2GsozjWJoQzuv1BSmbMA0whf3rLWcaz4RguC83mmFfBsqsTGHahv8cY9",
            "https://lh3.googleusercontent.com/aida-public/AB6AXuAfw4YOBL0J9feP_1vNA66OOmygh0Q80YnNKA4bNmsRerZG5bn-aLs4tVf819m8jPBRJMbU_CRGABouQs9FIpIsdwskCEnW3usrLQq_QSUwldW5dQ--PQEKg5aWifU5q5Z0V1tn_tNyy2n2mKzfDw0gJ3yqSONMpTSdd2gpZbjNJ87K8THWsJAMSJUqQPRksIFh4f8r9qGXWYIwE5psNzlwVoc0l8EYsT84oh5c4Btanyb9-Wj6Peybh3UfH12yUw2biXZxqtI4yReV"
        )
        val dummyLogos = listOf(
            "https://lh3.googleusercontent.com/aida-public/AB6AXuCylcjKdPos5U5ipJnRkea1gh_InGIyEa_NM0J2BkrHUewLl1ZpsrbF2py4hUFnjdVoeRaNJz5OJNdE7cc_XwJNQx5OvqI7ev6ITcMrj8LxWOE3AqdOz_bVCoDq4s-aDUe9zb4paH1sHQiPDVwvCIhHUWQZHZddmIcdl6lhyqyKYUviAZ_E_ComHuMIO5oT46nMlMwGodS7uzTfQUI70SZGknMsmTjBt_03QmPJ8iJe0Alf3RNIMwNMTxtDZjMZ9x7rU_5qe9Y-L7e4",
            "https://lh3.googleusercontent.com/aida-public/AB6AXuDvnQSYMzKoz2qi9y1ItPEON9L0siVLsTKyQfarA8CteuHZwTaplN5ebLkpfkgWLEmgYrqUQCo_8e3RWylSeheJ5qeZiYlQJxGaSF1ExYuq6VtSGD4B8HIB3fLaR-JG6YJ1eWrmcTJsKESaRh29HiGNtel3y7ejRcnhexezQMGaLRj9sPP20ZM4drpTvceB_6cEJ8NDM06I2tpmW7JwL5L3CEvM9dLJcnLnL3mtPAQjwQdKy_nufgiOU1GmJzavCetSZAJ_gfM6aWKT",
            "https://lh3.googleusercontent.com/aida-public/AB6AXuCjk6wN_QSmVDpiS2Sym_7ylbjOLLXM-4p9rD7ny7sOIe-CIbVa2Lb_qQA3mRostNnwR3-2uL4IlU0jenJ9V4VuXW5FH5cXsgNsT12R2HXQw0gzwmcuuawq54uBRrnSy1qfOnO3ILUY5ZacxUpSMUrCoIXLn0cjc_DS1kEqPehBUne8iTcMwX2uvkFIVORmA0DIOiURoiKsrVd9jK8xx9yKEMbl-sQ3SsF1aUtm5Q7LWJ5Au-MQxr-o6uOXNJihLgnnY5nwf7CanWDh"
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(3) { index ->
                Box(
                    modifier = Modifier
                        .width(256.dp)
                        .height(320.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = dummyImages[index],
                        contentDescription = "Success Story Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        androidx.compose.ui.graphics.Color.Transparent,
                                        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f),
                                        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.9f)
                                    ),
                                    startY = 300f 
                                )
                            )
                    )

                    // Bottom Content
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(androidx.compose.ui.graphics.Color.White)
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = dummyLogos[index],
                                    contentDescription = "Company Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(LiquidColors.Primary.copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = dummyData[index].third,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Text(
                            text = dummyData[index].first,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        Text(
                            text = dummyData[index].second,
                            fontSize = 14.sp,
                            color = androidx.compose.ui.graphics.Color(0xFFE2E8F0) // slate-200
                        )
                    }
                }
            }
        }
}

}

// ─── Live on Campus Section ───────────────────────────────────────────────────
@Composable
private fun LiveOnCampusSection(
    liveEvents: List<com.exa.android.reflekt.loopit.presentation.main.placement.model.LiveEventModel>,
    onCompanyClick: (String) -> Unit,
    onEventClick: (String) -> Unit
) {
    if (liveEvents.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle(
            title = "Live on Campus",
            trailingContent = {
                Box(
                    modifier = Modifier
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(50.dp))
                        .background(LiquidColors.Red.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(LiquidColors.Red)
                        )
                        Text(
                            "LIVE NOW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiquidColors.Red,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(liveEvents) { event ->
                LiveOnCampusCard(
                    event = event, 
                    onCompanyClick = { onCompanyClick(event.companyId) },
                    onEventClick = { onEventClick(event.linkedPostId) }
                )
            }
        }
    }
}

@Composable
private fun LiveOnCampusCard(
    event: com.exa.android.reflekt.loopit.presentation.main.placement.model.LiveEventModel,
    onCompanyClick: () -> Unit,
    onEventClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(320.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .background(LiquidColors.GlassSurface)
            .border(1.dp, LiquidColors.GlassBorder, androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .clickable { onCompanyClick() }
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Top: Logo + Info
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo
                CompanyLogo(
                    logoUrl = event.logoUrl,
                    companyName = event.companyName,
                    initials = event.companyName.take(2).uppercase(),
                    containerSize = 64.dp,
                    imageSize = 42.dp,
                    cornerRadius = 14.dp
                )

                // Info
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = event.companyName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidColors.TextPrimary
                    )
                    Text(
                        text = event.role.ifBlank { "Software Engineer" },
                        fontSize = 14.sp,
                        color = LiquidColors.TextSecondary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        // Location chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                                .background(LiquidColors.BgBase)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = LiquidColors.TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(event.location.ifBlank { "Campus" }, fontSize = 11.sp, color = LiquidColors.TextSecondary)
                        }
                        // Time chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                                .background(LiquidColors.BgBase)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = LiquidColors.TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(event.time.ifBlank { "TBA" }, fontSize = 11.sp, color = LiquidColors.TextSecondary)
                        }
                    }
                }
            }

            HorizontalDivider(color = LiquidColors.GlassBorder.copy(alpha = 0.5f))

            // Bottom: "View Details" action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.description.ifBlank { "Pre-placement talk & recruitment drive" },
                        fontSize = 13.sp,
                        color = LiquidColors.TextSecondary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (event.packageAmount.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Package: ₹${event.packageAmount} LPA",
                            fontSize = 12.sp,
                            color = LiquidColors.Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(
                    modifier = Modifier
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(50.dp))
                        .background(LiquidColors.Primary.copy(alpha = 0.1f))
                        .clickable { onEventClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("View Details", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LiquidColors.Primary)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = LiquidColors.Primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
