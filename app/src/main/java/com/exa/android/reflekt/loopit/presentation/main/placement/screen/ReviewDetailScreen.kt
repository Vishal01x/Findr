package com.exa.android.reflekt.loopit.presentation.main.placement.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.data.feature.main.placement.data.viewmodel.PlacementViewModel
import com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model.PlacementReview
import com.exa.android.reflekt.loopit.presentation.main.placement.component.RoundCard
import com.exa.android.reflekt.loopit.presentation.main.placement.component.VerificationBadge
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDetailScreen(
    reviewId: String,
    onBackClick: () -> Unit,
    viewModel: PlacementViewModel = hiltViewModel()
) {
    var review by remember { mutableStateOf<PlacementReview?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(reviewId) {
        scope.launch {
            // In a real app, you'd fetch from repository
            // For now, find from current state
            viewModel.state.collect { state ->
                review = state.reviews.find { it.id == reviewId }
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                review == null -> {
                    Text(
                        text = "Review not found",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Company Header
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = review!!.companyName,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        VerificationBadge(status = review!!.verificationStatus)
                                    }

                                    Text(
                                        text = review!!.packageInfo.getDisplayText(),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Text(
                                        text = "${review!!.month} ${review!!.year} • ${review!!.academicYear}",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Stats
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    icon = Icons.Default.Group,
                                    label = "Shortlisted",
                                    value = review!!.studentsShortlisted.toString(),
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    icon = Icons.Default.Group,
                                    label = "Selected",
                                    value = review!!.studentsSelected.toString(),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Eligibility
                        item {
                            SectionCard(title = "Eligibility Criteria") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    review!!.eligibilityCriteria.cgpa?.let {
                                        InfoRow("CGPA Cutoff", "$it+")
                                    }
                                    if (review!!.eligibilityCriteria.branches.isNotEmpty()) {
                                        InfoRow("Eligible Branches", review!!.eligibilityCriteria.branches.joinToString(", "))
                                    }
                                    review!!.eligibilityCriteria.maxBacklogs?.let {
                                        InfoRow("Max Backlogs", it.toString())
                                    }
                                    if (review!!.eligibilityCriteria.otherCriteria.isNotEmpty()) {
                                        InfoRow("Other Criteria", review!!.eligibilityCriteria.otherCriteria)
                                    }
                                }
                            }
                        }

                        // Rounds
                        item {
                            Text(
                                text = "Selection Process",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(review!!.rounds) { round ->
                            RoundCard(round = round)
                        }

                        // Overall Experience
                        item {
                            SectionCard(title = "Overall Experience") {
                                Text(
                                    text = review!!.overallExperience,
                                    fontSize = 15.sp,
                                    lineHeight = 24.sp, // Improved readability
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                )
                            }
                        }

                        // Tips
                        if (review!!.tips.isNotEmpty()) {
                            item {
                                SectionCard(
                                    title = "Tips for Juniors",
                                    icon = Icons.Default.Lightbulb,
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        review!!.tips.forEach { tip ->
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lightbulb,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp).padding(top = 4.dp)
                                                )
                                                Text(
                                                    text = tip,
                                                    fontSize = 15.sp,
                                                    lineHeight = 22.sp,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Submitted By
                        item {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = "Submitted by ${review!!.submittedByName}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                                Text(
                                    text = "Verified on ${review!!.verifiedAt?.toDate().toString().take(10) ?: "Pending"}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
