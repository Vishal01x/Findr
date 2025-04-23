package com.exa.android.reflekt.loopit.presentation.main.profile.components.education

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CollegeDatePicker(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    val dateFormat = remember {
        SimpleDateFormat("MMM yyyy", Locale.getDefault())
    }

    // Try parsing existing selected date
    LaunchedEffect(Unit) {
        try {
            val parsedDate = dateFormat.parse(selectedDate)
            if (parsedDate != null) {
                calendar.time = parsedDate
            }
        } catch (e: Exception) {
            // fallback: current date
        }
    }

    DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            calendar.set(year, month, dayOfMonth)
            val formattedDate = dateFormat.format(calendar.time)
            onDateSelected(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        setOnCancelListener { onDismiss() }
        show()
    }
}
