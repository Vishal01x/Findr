package com.exa.android.reflekt.loopit.presentation.main.profile.components.education

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.EditProfileViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.component.showLoader
import com.exa.android.reflekt.loopit.presentation.main.profile.components.extra_card.ExtracurricularState
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.model.Profile.CollegeInfo
import java.text.SimpleDateFormat
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

    // In EditEducationScreen
    var dateError by remember { mutableStateOf(false) }

    var school by rememberSaveable { mutableStateOf(initialData.instituteName) }
    var stream by rememberSaveable { mutableStateOf(initialData.stream) }
    var grade by rememberSaveable { mutableStateOf(initialData.grade) }

    var startDate by rememberSaveable { mutableStateOf(initialData.startDate ?: "") }
    var endDate by rememberSaveable { mutableStateOf(initialData.endDate ?: "") }

    var schoolError by remember { mutableStateOf(false) }
    var streamError by remember { mutableStateOf(false) }
    var startDateError by remember { mutableStateOf(false) }
    var endDateError by remember { mutableStateOf(false) }

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
                title = { Text("Edit Education",
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

                DatePickerField(
                    label = "Start date*",
                    dateText = startDate,
                    isError = startDateError,
                    onDateSelected = {
                        startDate = it
                        if (startDateError) startDateError = false
                    }
                )

                DatePickerField(
                    label = "End date (or expected)*",
                    dateText = endDate,
                    isError = endDateError,
                    onDateSelected = {
                        endDate = it
                        if (endDateError) endDateError = false
                    }
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
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Add this validation check in your save handler
                    Button(onClick = {
                        val datesValid = validateDates(startDate, endDate)

                        val isValid = school.isNotBlank().also { schoolError = !it } &&
                                stream.isNotBlank().also { streamError = !it } &&
                                startDate.isNotBlank().also { startDateError = !it } &&
                                endDate.isNotBlank().also { endDateError = !it }
                                //&& datesValid.also { dateError = !it }

                        if (isValid) {
                            editProfileViewModel.updateUserEducation(
                                CollegeInfo(school, stream, startDate, endDate, grade)
                            )
                        }
                    }) {
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
fun EducationTextField(
    label: String,
    hint: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(hint) },
        singleLine = true,
        isError = isError,
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