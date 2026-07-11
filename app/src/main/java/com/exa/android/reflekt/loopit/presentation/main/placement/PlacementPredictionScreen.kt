package com.exa.android.reflekt.loopit.presentation.main.placement

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.presentation.main.placement.components.GlassCard
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidMeshBackground
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidTopBar
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.DsaLevel
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.EligibilityStatus
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementPredictionEvent
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementPredictionSideEffect
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementPredictionState
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PredictionResult
import com.exa.android.reflekt.loopit.presentation.main.placement.vm.PlacementPredictionViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlacementPredictionScreen(
    onBack: () -> Unit = {},
    onNavigateToCompanyDetail: (String) -> Unit = {},
    viewModel: PlacementPredictionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val bgBase = LiquidColors.BgBase

    // Collect one-shot navigation side effects from the Channel
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PlacementPredictionSideEffect.NavigateToCompanyDetail ->
                    onNavigateToCompanyDetail(effect.companyId)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBase)
    ) {
        LiquidMeshBackground()

        Scaffold(
            topBar = {
                LiquidTopBar(
                    title = "Eligibility Predictor",
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            AnimatedContent(
                targetState = state.showResults,
                transitionSpec = {
                    if (targetState) {
                        // Navigating to results: slide in from right, out to left
                        (slideInHorizontally { it } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        // Going back to form: slide in from left, out to right
                        (slideInHorizontally { -it } + fadeIn()) togetherWith
                                (slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "PredictionContent",
                modifier = Modifier.padding(innerPadding)
            ) { showResults ->
                if (showResults) {
                    ResultsPhase(
                        state = state,
                        onEditPreferences = { viewModel.onEvent(PlacementPredictionEvent.ResetResults) },
                        onCompanyClick = viewModel::onCompanyClicked
                    )
                } else {
                    InputFormPhase(
                        state = state,
                        onEvent = viewModel::onEvent
                    )
                }
            }
        }
    }
}

// ─── Phase 1 — Input Form ────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InputFormPhase(
    state: PlacementPredictionState,
    onEvent: (PlacementPredictionEvent) -> Unit
) {
    val primary = LiquidColors.Primary
    val textPrimary = LiquidColors.TextPrimary
    val textMuted = LiquidColors.TextMuted
    val glassSurface = LiquidColors.GlassSurface
    val glassBorder = LiquidColors.GlassBorder

    val allInterests = listOf("Web Dev", "Mobile", "ML/AI", "Data", "DevOps", "Finance")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Header ──────────────────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎯", fontSize = 52.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Find Your Best Matches",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Tell us about yourself",
                    fontSize = 14.sp,
                    color = textMuted
                )
            }
        }

        // ── CGPA Card ───────────────────────────────────────────────────────
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "YOUR CGPA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        String.format("%.1f", state.cgpa),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary
                    )
                    Slider(
                        value = state.cgpa,
                        onValueChange = { onEvent(PlacementPredictionEvent.CgpaChanged(it)) },
                        valueRange = 4f..10f,
                        steps = 59,
                        colors = SliderDefaults.colors(
                            thumbColor = primary,
                            activeTrackColor = primary,
                            inactiveTrackColor = primary.copy(alpha = 0.2f)
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("4.0", fontSize = 11.sp, color = textMuted)
                        Text("10.0", fontSize = 11.sp, color = textMuted)
                    }
                }
            }
        }

        // ── DSA Level Card ──────────────────────────────────────────────────
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "DSA LEVEL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(DsaLevel.values().toList()) { level ->
                            val selected = level == state.dsaLevel
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) primary else glassSurface)
                                    .border(
                                        1.dp,
                                        if (selected) primary else glassBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onEvent(PlacementPredictionEvent.DsaLevelChanged(level)) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    level.label,
                                    color = if (selected) Color.White else textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Projects Card ────────────────────────────────────────────────────
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "PROJECTS COMPLETED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textMuted,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(primary.copy(alpha = 0.15f))
                                .clickable(enabled = state.projectsCount > 0) {
                                    onEvent(
                                        PlacementPredictionEvent.ProjectsCountChanged(
                                            state.projectsCount - 1
                                        )
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "−",
                                fontSize = 22.sp,
                                color = if (state.projectsCount > 0) primary else textMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(32.dp))
                        Text(
                            state.projectsCount.toString(),
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Spacer(Modifier.width(32.dp))
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(primary.copy(alpha = 0.15f))
                                .clickable(enabled = state.projectsCount < 10) {
                                    onEvent(
                                        PlacementPredictionEvent.ProjectsCountChanged(
                                            state.projectsCount + 1
                                        )
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "+",
                                fontSize = 22.sp,
                                color = if (state.projectsCount < 10) primary else textMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // ── Interests Card ───────────────────────────────────────────────────
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "AREAS OF INTEREST",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allInterests.forEach { interest ->
                            val selected = interest in state.interests
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selected) primary.copy(alpha = 0.15f) else glassSurface
                                    )
                                    .border(
                                        1.dp,
                                        if (selected) primary.copy(alpha = 0.5f) else glassBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onEvent(PlacementPredictionEvent.InterestToggled(interest)) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    interest,
                                    color = if (selected) primary else textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Backlogs Card ────────────────────────────────────────────────────
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Active Backlogs?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Having backlogs may affect eligibility",
                            fontSize = 12.sp,
                            color = textMuted
                        )
                    }
                    Switch(
                        checked = state.hasBacklogs,
                        onCheckedChange = { onEvent(PlacementPredictionEvent.BacklogsChanged(it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = primary,
                            uncheckedThumbColor = textMuted,
                            uncheckedTrackColor = glassSurface
                        )
                    )
                }
            }
        }

        // ── CTA Button ───────────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF13B6EC), Color(0xFF0EA5E9))
                        )
                    )
                    .clickable(enabled = !state.isLoading) {
                        onEvent(PlacementPredictionEvent.RunPrediction)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Find My Matches →",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── Phase 2 — Results ───────────────────────────────────────────────────────

@Composable
private fun ResultsPhase(
    state: PlacementPredictionState,
    onEditPreferences: () -> Unit,
    onCompanyClick: (String) -> Unit
) {
    val results = state.results ?: emptyList()
    val eligibleCount = results.count { it.eligibilityStatus == EligibilityStatus.ELIGIBLE }
    val prepareMoreCount = results.count { it.eligibilityStatus == EligibilityStatus.PREPARE_MORE }

    val eligibleResults = results.filter { it.eligibilityStatus == EligibilityStatus.ELIGIBLE }
    val prepareResults = results.filter { it.eligibilityStatus == EligibilityStatus.PREPARE_MORE }
    val notEligibleResults = results.filter { it.eligibilityStatus == EligibilityStatus.NOT_ELIGIBLE }

    val primary = LiquidColors.Primary
    val textPrimary = LiquidColors.TextPrimary
    val textMuted = LiquidColors.TextMuted
    val green = LiquidColors.Green
    val amber = LiquidColors.Amber
    val red = LiquidColors.Red

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ── Back to form ─────────────────────────────────────────────────────
        item {
            TextButton(
                onClick = onEditPreferences,
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
            ) {
                Text(
                    "← Edit Preferences",
                    color = primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // ── Summary Card ─────────────────────────────────────────────────────
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF13B6EC).copy(alpha = 0.18f),
                                    Color(0xFF0EA5E9).copy(alpha = 0.07f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            "${results.size} companies match your profile",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "$eligibleCount eligible now · $prepareMoreCount need more prep",
                            fontSize = 13.sp,
                            color = textMuted
                        )
                    }
                }
            }
        }

        // ── ELIGIBLE group ───────────────────────────────────────────────────
        if (eligibleResults.isNotEmpty()) {
            item { GroupHeader(label = "✓ You Qualify", accentColor = green) }
            items(eligibleResults, key = { "${it.companyId}_eligible" }) { result ->
                PredictionResultCard(result = result, accentColor = green, onCompanyClick = onCompanyClick)
            }
        }

        // ── PREPARE_MORE group ───────────────────────────────────────────────
        if (prepareResults.isNotEmpty()) {
            item { GroupHeader(label = "⚡ Almost There", accentColor = amber) }
            items(prepareResults, key = { "${it.companyId}_prepare" }) { result ->
                PredictionResultCard(result = result, accentColor = amber, onCompanyClick = onCompanyClick)
            }
        }

        // ── NOT_ELIGIBLE group ───────────────────────────────────────────────
        if (notEligibleResults.isNotEmpty()) {
            item { GroupHeader(label = "Requirements Not Met", accentColor = textMuted) }
            items(notEligibleResults, key = { "${it.companyId}_not" }) { result ->
                PredictionResultCard(result = result, accentColor = red, onCompanyClick = onCompanyClick)
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

// ─── Sub-components ──────────────────────────────────────────────────────────

@Composable
private fun GroupHeader(label: String, accentColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, fontWeight = FontWeight.Bold, color = accentColor, fontSize = 14.sp)
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(accentColor.copy(alpha = 0.3f))
        )
    }
}

@Composable
private fun PredictionResultCard(
    result: PredictionResult,
    accentColor: Color,
    onCompanyClick: (String) -> Unit
) {
    val textPrimary = LiquidColors.TextPrimary
    val textMuted = LiquidColors.TextMuted
    val primary = LiquidColors.Primary

    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        // IntrinsicSize.Min lets the left accent bar fill the card height correctly
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Left accent border — clipped to match card's rounded corners by GlassCard
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    result.companyName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${result.role} • ${result.packageDisplay}",
                    fontSize = 13.sp,
                    color = textMuted
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    result.reason,
                    fontSize = 12.sp,
                    color = textMuted,
                    fontStyle = FontStyle.Italic
                )
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { onCompanyClick(result.companyId) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "View Details →",
                        color = primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
