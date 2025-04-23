package com.exa.android.reflekt.loopit.presentation.main.profile.components.education

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.EditProfileViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.component.showLoader
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.model.Profile.ExperienceInfo
import java.text.SimpleDateFormat
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
                        color = MaterialTheme.colorScheme.onSurface
                    )) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = null
                    },
                    label = { Text("Title*") },
                    placeholder = { Text("Ex: Software Engineer") },
                    isError = titleError != null,
                    supportingText = { titleError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = employmentType,
                    onValueChange = { employmentType = it },
                    label = { Text("Employment Type") },
                    placeholder = { Text("Ex: Full-time") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                OutlinedTextField(
                    value = company,
                    onValueChange = {
                        company = it
                        companyError = null
                    },
                    label = { Text("Company*") },
                    placeholder = { Text("Ex: Google") },
                    isError = companyError != null,
                    supportingText = { companyError?.let { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = {
                        location = it
                        locationError = null
                    },
                    label = { Text("Location*") },
                    placeholder = { Text("Ex: Bengaluru, India") },
                    isError = locationError != null,
                    supportingText = { locationError?.let { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                DatePickerFieldd(
                    label = "Start Date*",
                    dateText = startDate,
                    onDateSelected = {
                        startDate = it
                        startDateError = null
                    },
                    isError = !startDateError.isNullOrEmpty()
                )

                if (!currentlyWorking) {
                    DatePickerField(
                        label = "End Date*",
                        dateText = endDate,
                        onDateSelected = {
                            endDate = it
                            endDateError = null
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
                    Text("Currently working here")
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Describe your role, responsibilities, and achievements") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            var valid = true
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
                                viewModel.updateUserEducation(
                                    ExperienceInfo(
                                    title,
                                    employmentType,
                                    company,
                                    location,
                                    startDate,
                                    if (currentlyWorking) "" else endDate,
                                    currentlyWorking,
                                    description
                                    )
                                )
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }

            if (response is Response.Loading) {
                showLoader()
            }
        }
    }
}

@Composable
fun DatePickerFieldd(
    label: String,
    dateText: String,
    onDateSelected: (String) -> Unit,
    isError: Boolean = false
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember {
        SimpleDateFormat("MMM yyyy", Locale.getDefault())
    }

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