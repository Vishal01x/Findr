package com.exa.android.reflekt.loopit.presentation.main

import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.ChatViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SettingScreen(
    viewModel: ChatViewModel
) {
    // val viewModel: ChatViewModel = hiltViewModel()
    var name by remember { mutableStateOf("") } // Use `by` for mutable state
    var phone by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
//        // Profile Picture
//        Box(
//            modifier = Modifier
//                .size(120.dp)
//                .clip(CircleShape)
//                .background(Color.Gray),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = "No Image",
//                color = Color.White,
//                textAlign = TextAlign.Center
//            )
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))

        // Name Text Field
        OutlinedTextField(
            value = name,
            onValueChange = { newValue -> name = newValue }, // Correctly update the state
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phone Text Field
        OutlinedTextField(
            value = phone,
            onValueChange = { newValue -> phone = newValue }, // Correctly update the state
            label = { Text("Phone Number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = {
                viewModel.insertUser(name, phone) // Use the current values of `name` and `phone`
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Save")
        }
    }
}
//
//@Preview(showBackground = true)
//@Composable
//fun ShowPreview() {
//    val viewModel
//    SettingScreen()
//}