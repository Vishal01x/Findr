package com.exa.android.reflekt.loopit.presentation.main.placement.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.data.feature.main.placement.data.viewmodel.PlacementEvent
import com.exa.android.reflekt.loopit.data.feature.main.placement.data.viewmodel.PlacementViewModel
import com.exa.android.reflekt.loopit.data.feature.main.placement.data.viewmodel.SubmitState
import com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model.*
import com.exa.android.reflekt.loopit.presentation.main.placement.component.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitReviewScreen(
    onBackClick: () -> Unit,
    submitViewModel: SubmitReviewViewModel = hiltViewModel(),
    placementViewModel: PlacementViewModel = hiltViewModel()
) {
    val submitState by placementViewModel.submitState
    val snackbarHostState = remember { SnackbarHostState() }
    val stepTitles = listOf(
        "Company Info",
        "Eligibility & Stats",
        "Interview Rounds",
        "Experience & Tips"
    )

    // Reset submit state when entering the screen to avoid stuck success state
    LaunchedEffect(Unit) {
        placementViewModel.resetSubmitState()
    }

    // Handle submission success
    LaunchedEffect(submitState) {
        when (submitState) {
            is SubmitState.Success -> {
                // Launch snackbar in parallel so it doesn't block the delay/navigation
                launch {
                    snackbarHostState.showSnackbar(
                        message = "Review submitted successfully! It's under verification.",
                        duration = SnackbarDuration.Short
                    )
                }
                
                // Wait briefly for user to see the message
                kotlinx.coroutines.delay(1500)
                
                // Navigate back
                onBackClick()
            }
            is SubmitState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (submitState as SubmitState.Error).message,
                    duration = SnackbarDuration.Long
                )
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Bar
            TopAppBar(
                title = { Text("Submit Review") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )

            // Step Indicator
            StepIndicator(
                currentStep = submitViewModel.currentStep,
                totalSteps = 4,
                stepTitles = stepTitles,
                modifier = Modifier.padding(16.dp)
            )

            Divider()

            // Form Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (submitViewModel.currentStep) {
                    0 -> Step1CompanyInfo(submitViewModel)
                    1 -> Step2EligibilityStats(submitViewModel)
                    2 -> Step3Rounds(submitViewModel)
                    3 -> Step4Experience(submitViewModel)
                }

                // Loading overlay
                if (submitState is SubmitState.Loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Navigation Buttons
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Previous Button
                    if (submitViewModel.currentStep > 0) {
                        OutlinedButton(
                            onClick = { submitViewModel.previousStep() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Previous")
                        }
                    }

                    // Next/Submit Button
                    Button(
                        onClick = {
                            if (submitViewModel.currentStep < 3) {
                                submitViewModel.nextStep()
                            } else {
                                // Submit review with actual user data
                                submitViewModel.submitReview(placementViewModel)
                            }
                        },
                        modifier = Modifier.weight(if (submitViewModel.currentStep > 0) 1f else 1f),
                        enabled = submitViewModel.canProceedToNextStep()
                    ) {
                        Text(if (submitViewModel.currentStep < 3) "Next" else "Submit")
                    }
                }
            }
        }
    }
}

@Composable
private fun Step1CompanyInfo(viewModel: SubmitReviewViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            SectionTitle(
                title = "Company Information",
                subtitle = "Tell us about the company and package details"
            )
        }

        item {
            FormTextField(
                value = viewModel.companyName,
                onValueChange = { viewModel.companyName = it },
                label = "Company Name *",
                placeholder = "e.g., Google, Microsoft"
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Package Type *",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChip(
                        selected = viewModel.packageType == "range",
                        onClick = { viewModel.packageType = "range" },
                        label = { Text("Range", fontSize = 14.sp) }
                    )
                    FilterChip(
                        selected = viewModel.packageType == "discrete",
                        onClick = { viewModel.packageType = "discrete" },
                        label = { Text("Multiple Packages", fontSize = 14.sp) }
                    )
                }
            }
        }

        if (viewModel.packageType == "range") {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormTextField(
                        value = viewModel.packageMin,
                        onValueChange = { viewModel.packageMin = it },
                        label = "Min Package (LPA) *",
                        placeholder = "10",
                        modifier = Modifier.weight(1f)
                    )
                    FormTextField(
                        value = viewModel.packageMax,
                        onValueChange = { viewModel.packageMax = it },
                        label = "Max Package (LPA) *",
                        placeholder = "25",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Package Values (LPA) *",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        TextButton(onClick = { viewModel.addDiscretePackage() }) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add")
                        }
                    }

                    viewModel.discretePackages.forEachIndexed { index, value ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FormTextField(
                                value = value,
                                onValueChange = { viewModel.updateDiscretePackage(index, it) },
                                label = "Package ${index + 1}",
                                placeholder = "12",
                                modifier = Modifier.weight(1f)
                            )
                            if (viewModel.discretePackages.size > 1) {
                                IconButton(onClick = { viewModel.removeDiscretePackage(index) }) {
                                    Icon(Icons.Default.Close, "Remove")
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            MonthYearPicker(
                selectedMonth = viewModel.visitMonth,
                selectedYear = viewModel.visitYear,
                onMonthYearSelected = { month, year ->
                    viewModel.visitMonth = month
                    viewModel.visitYear = year
                },
                label = "Visit Month & Year *"
            )
        }

        item {
            AcademicYearPicker(
                selectedAcademicYear = viewModel.academicYear,
                onAcademicYearSelected = { viewModel.academicYear = it }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step2EligibilityStats(viewModel: SubmitReviewViewModel) {
    val branches = listOf("CSE", "IT", "ECE", "EEE", "Mechanical", "Civil", "Other")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionTitle(
                title = "Eligibility Criteria",
                subtitle = "Specify the eligibility requirements"
            )
        }

        item {
            FormTextField(
                value = viewModel.cgpa,
                onValueChange = { viewModel.cgpa = it },
                label = "Minimum CGPA",
                placeholder = "7.5"
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Eligible Branches",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    branches.forEach { branch ->
                        FilterChip(
                            selected = viewModel.selectedBranches.contains(branch),
                            onClick = { viewModel.toggleBranch(branch) },
                            label = { Text(branch) }
                        )
                    }
                }
            }
        }

        item {
            FormTextField(
                value = viewModel.maxBacklogs,
                onValueChange = { viewModel.maxBacklogs = it },
                label = "Maximum Backlogs Allowed",
                placeholder = "0"
            )
        }

        item {
            FormTextField(
                value = viewModel.otherCriteria,
                onValueChange = { viewModel.otherCriteria = it },
                label = "Other Criteria",
                placeholder = "Any additional requirements",
                singleLine = false,
                maxLines = 3
            )
        }

        item {
            Divider()
            SectionTitle(
                title = "Selection Statistics",
                subtitle = "How many students participated and got selected?"
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormTextField(
                    value = viewModel.studentsShortlisted,
                    onValueChange = { viewModel.studentsShortlisted = it },
                    label = "Students Shortlisted *",
                    placeholder = "150",
                    modifier = Modifier.weight(1f)
                )
                FormTextField(
                    value = viewModel.studentsSelected,
                    onValueChange = { viewModel.studentsSelected = it },
                    label = "Students Selected *",
                    placeholder = "25",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Overall Difficulty *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Difficulty.entries.forEach { difficulty ->
                        FilterChip(
                            selected = viewModel.overallDifficulty == difficulty,
                            onClick = { viewModel.overallDifficulty = difficulty },
                            label = { Text(difficulty.displayName) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step3Rounds(viewModel: SubmitReviewViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            SectionTitle(
                title = "Hiring Rounds",
                subtitle = "Add details for each round of the hiring process"
            )
        }

        itemsIndexed(viewModel.rounds) { index, round ->
            RoundInputCard(
                roundNumber = index + 1,
                round = round,
                onRoundChange = { viewModel.updateRound(index, it) },
                onRemove = if (viewModel.rounds.size > 1) {
                    { viewModel.removeRound(index) }
                } else null
            )
        }

        item {
            OutlinedButton(
                onClick = { viewModel.addRound() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Another Round", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoundInputCard(
    roundNumber: Int,
    round: Round,
    onRoundChange: (Round) -> Unit,
    onRemove: (() -> Unit)?
) {
    var specificTopicsText by remember { mutableStateOf(round.specificTopics.joinToString(", ")) }
    var commonTopicsText by remember { mutableStateOf(round.commonTopics.joinToString(", ")) }
    var keyPointsText by remember { mutableStateOf(round.keyPoints.joinToString("\n")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Round $roundNumber : ${round.roundType.displayName}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                onRemove?.let {
                    IconButton(onClick = it) {
                        Icon(
                            Icons.Default.Close,
                            "Remove Round",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Round Type Dropdown
            RoundTypeDropdown(
                selectedType = round.roundType,
                onTypeSelected = { onRoundChange(round.copy(roundType = it)) }
            )

            // Difficulty
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Difficulty Level",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Difficulty.entries.forEach { difficulty ->
                        FilterChip(
                            selected = round.difficulty == difficulty,
                            onClick = { onRoundChange(round.copy(difficulty = difficulty)) },
                            label = { Text(difficulty.displayName, fontSize = 14.sp) }
                        )
                    }
                }
            }

            // Topics
            FormTextField(
                value = specificTopicsText,
                onValueChange = {
                    specificTopicsText = it
                    onRoundChange(round.copy(specificTopics = it.split(",").map { s -> s.trim() }.filter { s -> s.isNotBlank() }))
                },
                label = "Specific Topics (comma-separated)",
                placeholder = "DSA, System Design, OOP",
                singleLine = false,
                maxLines = 2
            )

            FormTextField(
                value = commonTopicsText,
                onValueChange = {
                    commonTopicsText = it
                    onRoundChange(round.copy(commonTopics = it.split(",").map { s -> s.trim() }.filter { s -> s.isNotBlank() }))
                },
                label = "Common Topics (comma-separated)",
                placeholder = "Arrays, Strings, Trees",
                singleLine = false,
                maxLines = 2
            )

            FormTextField(
                value = keyPointsText,
                onValueChange = {
                    keyPointsText = it
                    onRoundChange(round.copy(keyPoints = it.split("\n").map { s -> s.trim() }.filter { s -> s.isNotBlank() }))
                },
                label = "Key Points (one per line)",
                placeholder = "Focus on time complexity\nPractice mock interviews",
                singleLine = false,
                maxLines = 4
            )

            FormTextField(
                value = round.description,
                onValueChange = { onRoundChange(round.copy(description = it)) },
                label = "Description",
                placeholder = "Describe the round format and experience",
                singleLine = false,
                maxLines = 3
            )
        }
    }
}

@Composable
private fun Step4Experience(viewModel: SubmitReviewViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionTitle(
                title = "Overall Experience",
                subtitle = "Share your experience and tips for future candidates"
            )
        }

        item {
            FormTextField(
                value = viewModel.overallExperience,
                onValueChange = { viewModel.overallExperience = it },
                label = "Overall Experience *",
                placeholder = "Describe your overall experience with the placement process...",
                singleLine = false,
                maxLines = 8
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tips for Future Candidates",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { viewModel.addTip() }) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Tip")
                }
            }
        }

        itemsIndexed(viewModel.tips) { index, tip ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FormTextField(
                    value = tip,
                    onValueChange = { viewModel.updateTip(index, it) },
                    label = "Tip ${index + 1}",
                    placeholder = "Practice coding daily, revise core subjects...",
                    modifier = Modifier.weight(1f),
                    singleLine = false,
                    maxLines = 2
                )
                if (viewModel.tips.size > 1) {
                    IconButton(onClick = { viewModel.removeTip(index) }) {
                        Icon(Icons.Default.Close, "Remove")
                    }
                }
            }
        }
    }
}

