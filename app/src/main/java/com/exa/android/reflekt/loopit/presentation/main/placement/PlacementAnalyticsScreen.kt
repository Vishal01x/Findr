package com.exa.android.reflekt.loopit.presentation.main.placement

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import com.exa.android.reflekt.loopit.presentation.navigation.component.PlacementRoute
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import com.exa.android.reflekt.loopit.presentation.main.placement.components.GlassCard
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidMeshBackground
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidTopBar
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementAnalyticsEvent
import com.exa.android.reflekt.loopit.presentation.main.placement.vm.PlacementAnalyticsViewModel
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacementAnalyticsScreen(
    navController: NavController,
    viewModel: PlacementAnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Change 1: Wrap root in Box, add LiquidMeshBackground, pass Column underneath
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LiquidColors.BgBase)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Change 2: Replace manual Row app bar with LiquidTopBar
            LiquidTopBar(
                title = "Placement Analytics",
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = LiquidColors.TextPrimary
                        )
                    }
                }
            )

            // Change 3: Wrap TabRow in a GlassCard-style container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LiquidColors.GlassSurface)
                    .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(16.dp))
            ) {
                TabRow(
                    selectedTabIndex = state.selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = LiquidColors.Primary,
                    divider = {}
                ) {
                    Tab(
                        selected = state.selectedTab == 0,
                        onClick = { viewModel.onEvent(PlacementAnalyticsEvent.TabSelected(0)) },
                        text = { Text("Overall Trends", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = state.selectedTab == 1,
                        onClick = { viewModel.onEvent(PlacementAnalyticsEvent.TabSelected(1)) },
                        text = { Text("Company Insights", fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (state.selectedTab == 0) {
                    // Overall Trends
                    if (state.overallStats.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                if (state.isLoading) CircularProgressIndicator(color = LiquidColors.Primary)
                                else Text("No placement statistics available.", color = LiquidColors.TextSecondary)
                            }
                        }
                    } else {
                        item {
                            GlassCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                                Column(Modifier.padding(20.dp)) {
                                    Text("Average Package Over Time", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
                                    Text("Macro view of average LPA growth", fontSize = 12.sp, color = LiquidColors.TextMuted)
                                    Spacer(Modifier.height(24.dp))
                                    LineChart(
                                        data = state.overallStats.map { it.batchYear to it.averagePackage.toFloat() },
                                        yLabel = "LPA"
                                    )
                                }
                            }
                        }
                        item {
                            GlassCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                                Column(Modifier.padding(20.dp)) {
                                    Text("Total Students Placed", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
                                    Text("Batch-wise hiring volume", fontSize = 12.sp, color = LiquidColors.TextMuted)
                                    Spacer(Modifier.height(24.dp))
                                    BarChart(
                                        data = state.overallStats.map { it.batchYear to it.totalPlaced.toFloat() },
                                        yLabel = "Hires",
                                        onBarClick = { batch ->
                                            navController.navigate(PlacementRoute.PlacedStudents.createRoute(batch = batch))
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Company Insights
                    item {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.onEvent(PlacementAnalyticsEvent.SearchQueryChanged(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter company name (e.g. TCS)", color = LiquidColors.TextMuted) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LiquidColors.Primary,
                                unfocusedBorderColor = LiquidColors.GlassBorder
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                viewModel.onEvent(PlacementAnalyticsEvent.SearchCompany(state.searchQuery))
                            }),
                            trailingIcon = {
                                IconButton(onClick = { viewModel.onEvent(PlacementAnalyticsEvent.SearchCompany(state.searchQuery)) }) {
                                    Icon(Icons.Default.Search, contentDescription = "Search", tint = LiquidColors.Primary)
                                }
                            }
                        )
                    }

                    // Suggestions removed as search is now case-insensitive directly

                    if (state.isSearchingCompany) {
                        item {
                            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = LiquidColors.Primary)
                            }
                        }
                    } else if (state.searchedCompany != null) {
                        if (state.companyStats.isEmpty()) {
                            item {
                                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                    Text("No hiring data found for '${state.searchedCompany}'.", color = LiquidColors.TextSecondary)
                                }
                            }
                        } else {
                            item {
                                Text("Analytics for ${state.searchedCompany}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
                            }
                            item {
                                GlassCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                                    Column(Modifier.padding(20.dp)) {
                                        Text("Hiring Volume", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
                                        Spacer(Modifier.height(24.dp))
                                        BarChart(
                                            data = state.companyStats.map { it.batchYear to it.totalHired.toFloat() },
                                            yLabel = "Hires",
                                            barColor = Color(0xFF8B5CF6),
                                            onBarClick = { batch ->
                                                navController.navigate(PlacementRoute.PlacedStudents.createRoute(batch = batch, company = state.searchedCompany))
                                            }
                                        )
                                    }
                                }
                            }
                            item {
                                GlassCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                                    Column(Modifier.padding(20.dp)) {
                                        Text("Average Package", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
                                        Spacer(Modifier.height(24.dp))
                                        LineChart(
                                            data = state.companyStats.map { it.batchYear to it.averagePackage.toFloat() },
                                            yLabel = "LPA",
                                            lineColor = Color(0xFF10B981)
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

// ─── Custom Canvas Charts ──────────────────────────────────────────────────

@OptIn(ExperimentalTextApi::class)
@Composable
fun LineChart(
    data: List<Pair<Int, Float>>,
    yLabel: String,
    lineColor: Color = LiquidColors.Primary,
    modifier: Modifier = Modifier.fillMaxWidth().height(180.dp)
) {
    if (data.isEmpty()) return
    val textMeasurer = rememberTextMeasurer()
    val maxY = max(10f, data.maxOf { it.second } * 1.2f)

    // Change 4: Capture @Composable color before entering Canvas draw scope
    val textMuted = LiquidColors.TextMuted

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val paddingX = 40.dp.toPx()
        val paddingRight = 30.dp.toPx()
        val paddingY = 20.dp.toPx()

        val chartWidth = width - paddingX - paddingRight
        val chartHeight = height - paddingY

        // Draw Axes
        drawLine(
            color = textMuted.copy(alpha = 0.3f),
            start = Offset(paddingX, 0f),
            end = Offset(paddingX, chartHeight),
            strokeWidth = 2f
        )
        drawLine(
            color = textMuted.copy(alpha = 0.3f),
            start = Offset(paddingX, chartHeight),
            end = Offset(width, chartHeight),
            strokeWidth = 2f
        )

        // Draw points and line
        if (data.size > 1) {
            val stepX = chartWidth / (data.size - 1)
            val path = Path()

            data.forEachIndexed { index, pair ->
                val x = paddingX + index * stepX
                val y = chartHeight - (pair.second / maxY) * chartHeight

                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)

                // Draw Point
                drawCircle(color = lineColor, radius = 6.dp.toPx(), center = Offset(x, y))
                drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(x, y))

                // X Axis Label
                drawText(
                    textMeasurer = textMeasurer,
                    text = pair.first.toString(),
                    topLeft = Offset(x - 20f, chartHeight + 10f),
                    style = androidx.compose.ui.text.TextStyle(color = textMuted, fontSize = 10.sp)
                )

                // Value Label
                drawText(
                    textMeasurer = textMeasurer,
                    text = String.format("%.1f", pair.second),
                    topLeft = Offset(x - 20f, y - 40f),
                    style = androidx.compose.ui.text.TextStyle(color = lineColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        } else if (data.size == 1) {
            val pair = data.first()
            val x = paddingX + chartWidth / 2
            val y = chartHeight - (pair.second / maxY) * chartHeight
            drawCircle(color = lineColor, radius = 6.dp.toPx(), center = Offset(x, y))
            drawText(
                textMeasurer = textMeasurer,
                text = pair.first.toString(),
                topLeft = Offset(x - 20f, chartHeight + 10f),
                style = androidx.compose.ui.text.TextStyle(color = textMuted, fontSize = 10.sp)
            )
            drawText(
                textMeasurer = textMeasurer,
                text = String.format("%.1f", pair.second),
                topLeft = Offset(x - 20f, y - 40f),
                style = androidx.compose.ui.text.TextStyle(color = lineColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun BarChart(
    data: List<Pair<Int, Float>>,
    yLabel: String,
    barColor: Color = LiquidColors.Primary,
    modifier: Modifier = Modifier.fillMaxWidth().height(180.dp),
    onBarClick: ((Int) -> Unit)? = null
) {
    if (data.isEmpty()) return
    val textMeasurer = rememberTextMeasurer()
    val maxY = max(10f, data.maxOf { it.second } * 1.2f)

    // Change 4: Capture @Composable color before entering Canvas draw scope
    val textMuted = LiquidColors.TextMuted

    Canvas(modifier = modifier.pointerInput(data) {
        detectTapGestures { offset ->
            val width = size.width
            val paddingX = 40.dp.toPx()
            val paddingRight = 30.dp.toPx()
            val chartWidth = width - paddingX - paddingRight
            val stepX = chartWidth / data.size

            data.forEachIndexed { index, pair ->
                val startX = paddingX + index * stepX
                val endX = startX + stepX
                if (offset.x >= startX && offset.x < endX) {
                    onBarClick?.invoke(pair.first)
                }
            }
        }
    }) {
        val width = size.width
        val height = size.height
        val paddingX = 40.dp.toPx()
        val paddingRight = 30.dp.toPx()
        val paddingY = 20.dp.toPx()

        val chartWidth = width - paddingX - paddingRight
        val chartHeight = height - paddingY

        // Draw Axes
        drawLine(
            color = textMuted.copy(alpha = 0.3f),
            start = Offset(paddingX, 0f),
            end = Offset(paddingX, chartHeight),
            strokeWidth = 2f
        )
        drawLine(
            color = textMuted.copy(alpha = 0.3f),
            start = Offset(paddingX, chartHeight),
            end = Offset(width, chartHeight),
            strokeWidth = 2f
        )

        val stepX = chartWidth / data.size
        val barWidth = (stepX * 0.4f).coerceAtMost(40.dp.toPx())

        data.forEachIndexed { index, pair ->
            val x = paddingX + (index * stepX) + (stepX / 2)
            val barHeight = (pair.second / maxY) * chartHeight
            val y = chartHeight - barHeight

            drawRoundRect(
                color = barColor.copy(alpha = 0.8f),
                topLeft = Offset(x - barWidth / 2, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // X Axis Label
            drawText(
                textMeasurer = textMeasurer,
                text = pair.first.toString(),
                topLeft = Offset(x - 20f, chartHeight + 10f),
                style = androidx.compose.ui.text.TextStyle(color = textMuted, fontSize = 10.sp)
            )

            // Value Label
            drawText(
                textMeasurer = textMeasurer,
                text = pair.second.toInt().toString(),
                topLeft = Offset(x - 10f, y - 40f),
                style = androidx.compose.ui.text.TextStyle(color = barColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}
