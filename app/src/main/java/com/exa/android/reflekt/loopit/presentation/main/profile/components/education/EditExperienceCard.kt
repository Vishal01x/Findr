package com.exa.android.reflekt.loopit.presentation.main.profile.components.education

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.loopit.data.feature.main.profile.data.viewmodel.EditProfileViewModel
import com.exa.android.reflekt.loopit.presentation.main.component.showLoader
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ExperienceInfo
import com.exa.android.reflekt.loopit.presentation.main.component.textfield.AppTextField
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidMeshBackground
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExperienceScreen(
    navController: NavController,
    initialData : ExperienceInfo,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val response = viewModel.responseState
    val snackbarHostState = remember { SnackbarHostState() }

    var title by rememberSaveable { mutableStateOf(initialData.title ?: "") }
    var employmentType by rememberSaveable { mutableStateOf(initialData.employmentType ?: "") }
    var company by rememberSaveable { mutableStateOf(initialData.companyName ?: "") }
    var location by rememberSaveable { mutableStateOf(initialData.location ?: "") }
    var startDate by rememberSaveable { mutableStateOf(initialData.startDate ?: "") }
    var endDate by rememberSaveable { mutableStateOf(initialData.endDate ?: "") }
    var currentlyWorking by rememberSaveable { mutableStateOf(initialData.currentlyWorking ?: false) }
    var description by rememberSaveable { mutableStateOf(initialData.description ?: "") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var companyError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var startDateError by remember { mutableStateOf<String?>(null) }
    var endDateError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(response) {
        when (response) {
            is Response.Success -> navController.popBackStack()
            is Response.Error -> snackbarHostState.showSnackbar(response.message)
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Experience",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = LiquidColors.TextPrimary
                    )) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Back", tint = LiquidColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LiquidColors.GlassSurface
                )
            )
        },
        containerColor = LiquidColors.BgBase,
        contentColor = LiquidColors.TextPrimary,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp,24.dp,24.dp,0.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                AppTextField(
                    value = title,
                    onValueChange = { title = it; titleError = null },
                    label = "Title*",
                    placeholder = "Ex: Software Engineer",
                    leadingIcon = Icons.Default.Work,
                    isError = titleError != null,
                    errorMessage = titleError,
                )

                Spacer(Modifier.height(8.dp))

                AppTextField(
                    value = employmentType,
                    onValueChange = { employmentType = it },
                    label = "Employment Type",
                    placeholder = "Ex: Full-time",
                    leadingIcon = Icons.Default.Badge,
                )

                Spacer(Modifier.height(8.dp))

                AppTextField(
                    value = company,
                    onValueChange = { company = it; companyError = null },
                    label = "Company*",
                    placeholder = "Ex: Google",
                    leadingIcon = Icons.Default.Business,
                    isError = companyError != null,
                    errorMessage = companyError,
                )

                Spacer(Modifier.height(8.dp))

                AppTextField(
                    value = location,
                    onValueChange = { location = it; locationError = null },
                    label = "Location*",
                    placeholder = "Ex: Bengaluru, India",
                    leadingIcon = Icons.Default.LocationOn,
                    isError = locationError != null,
                    errorMessage = locationError,
                )

                val formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)

                DatePickerField(
                    label = "Start Date*",
                    dateText = startDate,
                    onDateSelected = { selectedDate ->
                        startDate = selectedDate
                        startDateError = null
                    },
                    isError = !startDateError.isNullOrEmpty()
                )

                if (!currentlyWorking) {
                    DatePickerField(
                        label = "End Date*",
                        dateText = endDate,
                        onDateSelected = { selectedDate ->
                            try {
                                val start = YearMonth.parse(startDate, formatter)
                                val end = YearMonth.parse(selectedDate, formatter)
                                if (end.isBefore(start)) {
                                    endDateError = "End date can't be before start date"
                                } else {
                                    endDate = selectedDate
                                    endDateError = null
                                }
                            } catch (e: Exception) {
                                endDateError = "Invalid date format"
                            }
                        },
                        isError = !endDateError.isNullOrEmpty()
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Checkbox(
                        checked = currentlyWorking,
                        onCheckedChange = {
                            currentlyWorking = it
                            if (it) endDateError = null
                        }
                    )
                    Text("Currently working here", color = LiquidColors.TextPrimary)
                }

                AppTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description",
                    placeholder = "Describe your role, responsibilities, and achievements",
                    leadingIcon = Icons.Default.Description,
                    singleLine = false,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Cancel", color = LiquidColors.TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val areDatesValid = try {
                                val start = YearMonth.parse(startDate, formatter)
                                val end = YearMonth.parse(endDate, formatter)
                                if (end.isBefore(start)) {
                                    endDateError = "End date can't be before start date"
                                    false
                                } else {
                                    endDateError = null
                                    true
                                }
                            } catch (e: Exception) {
                                endDateError = "Invalid date format"
                                false
                            }
                            var valid = areDatesValid
                            if (title.isBlank()) {
                                titleError = "Job title is required"
                                valid = false
                            }
                            if (company.isBlank()) {
                                companyError = "Company name is required"
                                valid = false
                            }
                            if (location.isBlank()) {
                                locationError = "Location is required"
                                valid = false
                            }
                            if (startDate.isBlank()) {
                                startDateError = "Start date is required"
                                valid = false
                            }
                            if (!currentlyWorking && endDate.isBlank()) {
                                endDateError = "End date is required"
                                valid = false
                            }

                            if (valid) {
                                viewModel.updateUserExperience(
                                    ExperienceInfo(
                                        title = title,
                                        employmentType = employmentType,
                                        companyName = company,
                                        location = location,
                                        startDate = startDate,
                                        endDate = if (currentlyWorking) "" else endDate,
                                        currentlyWorking = currentlyWorking,
                                        description = description
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary)
                    ) {
                        Text("Save", color = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }

            if (response is Response.Loading) {
                showLoader()
            }
        }
    }
}
/*
@Composable
fun DatePickerField(
    label: String,
    dateText: String,
    onDateSelected: (String) -> Unit,
    isError: Boolean = false
) {
    var showDatePicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = dateText,
        onValueChange = {},
        readOnly = true,
        isError = isError,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Select date",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )

    if (showDatePicker) {
        CollegeDatePicker(
            selectedDate = dateText,
            onDateSelected = {
                onDateSelected(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}
*/