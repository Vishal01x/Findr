package com.exa.android.reflekt.loopit.presentation.main.profile.components.education

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.loopit.data.feature.main.profile.data.viewmodel.EditProfileViewModel
import com.exa.android.reflekt.loopit.presentation.main.component.showLoader
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.CollegeInfo
import com.exa.android.reflekt.loopit.presentation.main.component.textfield.AppTextField
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidMeshBackground
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEducationScreen(
    navController: NavController,
    initialData: CollegeInfo = CollegeInfo(),
    editProfileViewModel: EditProfileViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val response = editProfileViewModel.responseState

    var school by rememberSaveable { mutableStateOf(initialData.instituteName) }
    var stream by rememberSaveable { mutableStateOf(initialData.stream) }
    var grade by rememberSaveable { mutableStateOf(initialData.grade) }
    var year by rememberSaveable { mutableStateOf(initialData.year) }
    var semester by rememberSaveable { mutableStateOf(initialData.semester) }
    var classWithSection by rememberSaveable { mutableStateOf(initialData.classWithSection) }

    var startDate by rememberSaveable { mutableStateOf(initialData.startDate ?: "") }
    var endDate by rememberSaveable { mutableStateOf(initialData.endDate ?: "") }

    var schoolError by remember { mutableStateOf(false) }
    var streamError by remember { mutableStateOf(false) }
    var startDateError by remember { mutableStateOf(false) }
    var endDateError by remember { mutableStateOf<String?>(null) }

    val formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)

    LaunchedEffect(response) {
        when (val state = response) {
            is Response.Success -> navController.popBackStack()
            is Response.Error -> snackbarHostState.showSnackbar(state.message)
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Education",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = LiquidColors.TextPrimary
                        )
                    )
                },
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
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                EducationTextField(
                    label = "School*",
                    hint = "Ex: Boston University",
                    value = school,
                    isError = schoolError,
                    onValueChange = {
                        school = it
                        if (schoolError) schoolError = false
                    }
                )

                EducationTextField(
                    label = "Field of study*",
                    hint = "Ex: Computer Science",
                    value = stream,
                    isError = streamError,
                    onValueChange = {
                        stream = it
                        if (streamError) streamError = false
                    }
                )

                EducationTextField(
                    label = "Year (optional)",
                    hint = "Ex: 3rd Year",
                    value = year,
                    onValueChange = { year = it }
                )

                EducationTextField(
                    label = "Semester (optional)",
                    hint = "Ex: Semester 5",
                    value = semester,
                    onValueChange = { semester = it }
                )

                EducationTextField(
                    label = "Class with section (optional)",
                    hint = "Ex: CSE-A",
                    value = classWithSection,
                    onValueChange = { classWithSection = it }
                )

                DatePickerField(
                    label = "Start date*",
                    dateText = startDate,
                    isError = startDateError,
                    onDateSelected = {
                        startDate = it
                        startDateError = false
                    }
                )

                DatePickerField(
                    label = "End Date*",
                    dateText = endDate,
                    onDateSelected = { selectedDate ->
                        try {
                            val start = YearMonth.parse(startDate, formatter)
                            val end = YearMonth.parse(selectedDate, formatter)

                            if (end.isBefore(start)) {
                                endDateError = "End date can't be before start date"
                                endDate = ""
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

                EducationTextField(
                    label = "Grade (optional)",
                    hint = "Ex: 8.9",
                    value = grade,
                    keyboardType = KeyboardType.Number,
                    onValueChange = { grade = it }
                )

                Spacer(Modifier.height(32.dp))

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

                        val isValid = school.isNotBlank().also { schoolError = !it } &&
                                stream.isNotBlank().also { streamError = !it } &&
                                startDate.isNotBlank().also { startDateError = !it } &&
                                endDate.isNotBlank().also {
                                    if (!it) endDateError = "End date required"
                                } && areDatesValid

                        if (isValid) {
                            editProfileViewModel.updateUserEducation(
                                CollegeInfo(
                                    instituteName = school,
                                    stream = stream,
                                    startDate = startDate,
                                    endDate = endDate,
                                    grade = grade,
                                    year = year,
                                    semester = semester,
                                    classWithSection = classWithSection
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

@Composable
fun EducationTextField(
    label: String,
    hint: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    onValueChange: (String) -> Unit
) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = hint,
        leadingIcon = if (keyboardType == KeyboardType.Number) Icons.Default.Star
                      else Icons.Default.School,
        isError = isError,
        errorMessage = if (isError) "This field is required" else null,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
fun DatePickerField(
    label: String,
    dateText: String,
    isError: Boolean = false,
    onDateSelected: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    AppTextField(
        value = dateText,
        onValueChange = {},
        label = label,
        readOnly = true,
        isError = isError,
        errorMessage = if (isError) "End date must be after start date" else null,
        leadingIcon = Icons.Default.DateRange,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Select date",
                    tint = LiquidColors.Primary
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

// Validation function
private fun validateDates(start: String, end: String): Boolean {
    return try {
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val startDate = dateFormat.parse(start)
        val endDate = dateFormat.parse(end)
        startDate?.before(endDate) ?: false
    } catch (e: Exception) {
        false
    }
}