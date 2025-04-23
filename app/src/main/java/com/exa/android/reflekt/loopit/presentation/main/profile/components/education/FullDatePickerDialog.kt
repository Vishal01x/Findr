package com.exa.android.reflekt.loopit.presentation.main.profile.components.education

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FullDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Trigger the dialog only once on composition
    LaunchedEffect(Unit) {
        val datePickerDialog = DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                calendar.set(year, month, dayOfMonth)
                val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
                onDateSelected(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Optional: To mimic green theme (if not default)
        // datePickerDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        datePickerDialog.setOnCancelListener {
            onDismiss()
        }

        datePickerDialog.show()
    }
}

