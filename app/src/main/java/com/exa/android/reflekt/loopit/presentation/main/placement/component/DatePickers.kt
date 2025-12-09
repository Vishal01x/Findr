package com.exa.android.reflekt.loopit.presentation.main.placement.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthYearPicker(
    selectedMonth: String,
    selectedYear: String,
    onMonthYearSelected: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Month & Year"
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    OutlinedTextField(
        value = if (selectedMonth.isNotBlank() && selectedYear.isNotBlank()) {
            "$selectedMonth $selectedYear"
        } else "",
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.CalendarToday, "Select Date")
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledTextColor = MaterialTheme.colorScheme.onSurface
        ),
        enabled = false
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = millis
                            }
                            val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
                            val month = monthFormat.format(calendar.time)
                            val year = calendar.get(Calendar.YEAR).toString()
                            onMonthYearSelected(month, year)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Select Visit Date",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                headline = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = millis
                        }
                        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                        Text(
                            text = dateFormat.format(calendar.time),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                showModeToggle = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicYearPicker(
    selectedAcademicYear: String,
    onAcademicYearSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    OutlinedTextField(
        value = selectedAcademicYear,
        onValueChange = {},
        label = { Text("Academic Year *") },
        readOnly = true,
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.CalendarToday, "Select Academic Year")
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledTextColor = MaterialTheme.colorScheme.onSurface
        ),
        enabled = false
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = millis
                            }
                            val year = calendar.get(Calendar.YEAR)
                            // Academic year format: 2023-24
                            val academicYear = "$year-${(year + 1).toString().takeLast(2)}"
                            onAcademicYearSelected(academicYear)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Select Academic Year",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                headline = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = millis
                        }
                        val year = calendar.get(Calendar.YEAR)
                        val academicYear = "$year-${(year + 1).toString().takeLast(2)}"
                        Text(
                            text = academicYear,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                showModeToggle = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundTypeDropdown(
    selectedType: com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model.RoundType,
    onTypeSelected: (com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model.RoundType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedType.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Round Type *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model.RoundType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.displayName) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

