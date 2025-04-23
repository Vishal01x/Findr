package com.exa.android.reflekt.loopit.presentation.main.profile.components.education

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DatePickerDemo() {
    var showDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .height(100.dp)
            .width(70.dp)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { showDialog = true }) {
            Text("Select Date")
        }

        if (selectedDate.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Selected Date: $selectedDate")
        }

        if (showDialog) {
            FullDatePickerDialog(
                onDateSelected = {
                    selectedDate = it
                    showDialog = false
                },
                onDismiss = {
                    showDialog = false
                }
            )
        }
    }
}



@Preview
@Composable
private fun previoew() {
    DatePickerDemo()
}
