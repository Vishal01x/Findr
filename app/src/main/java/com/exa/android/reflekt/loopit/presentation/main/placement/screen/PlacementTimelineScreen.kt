package com.exa.android.reflekt.loopit.presentation.main.placement.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.data.feature.main.placement.data.viewmodel.PlacementEvent
import com.exa.android.reflekt.loopit.data.feature.main.placement.data.viewmodel.PlacementViewModel
import com.exa.android.reflekt.loopit.presentation.main.placement.component.YearSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacementTimelineScreen(
    onReviewClick: (String) -> Unit,
    onAddReviewClick: () -> Unit,
    viewModel: PlacementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Placement Reviews",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.reviews.size} reviews",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Content
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error loading reviews",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.error ?: "",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                state.reviews.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No reviews yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Be the first to share your placement experience!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Group reviews by year and display
                        val sortedYears = state.groupedByYear.keys.sortedDescending()
                        
                        items(sortedYears) { year ->
                            val yearReviews = state.groupedByYear[year] ?: emptyList()
                            YearSection(
                                year = year,
                                reviews = yearReviews,
                                isExpanded = state.expandedYears.contains(year),
                                onToggleExpand = {
                                    viewModel.onEvent(PlacementEvent.ToggleYearExpansion(year))
                                },
                                onReviewClick = { review ->
                                    onReviewClick(review.id)
                                }
                            )
                        }
                        
                        // Bottom spacing for FAB
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = onAddReviewClick,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Review"
            )
        }
    }
}
