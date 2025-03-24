package com.exa.android.reflekt.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ColorDemoScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 🔹 Primary Usage (Chat bubble)
        ChatBubble(message = "Hello, this is a sent message!", isSentByUser = true)

        // 🔹 Surface Usage (Received Chat Bubble)
        ChatBubble(message = "Hey! I received your message.", isSentByUser = false)

        // 🔹 Secondary Usage (Icon with Label)
        IconWithLabel()

        // 🔹 Primary Container (Highlighted Message)
        HighlightedMessage("This is a pinned message.")

        // 🔹 Secondary Container (TextField Example)
        InputField(label = "Enter Email")

        // 🔹 Buttons Demonstrating Primary, Secondary, and Tertiary
        ActionButtons()
    }
}

@Composable
fun ChatBubble(message: String, isSentByUser: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .background(
                if (isSentByUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        Text(
            text = message,
            color = if (isSentByUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun IconWithLabel() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Icon(Icons.Default.Email, contentDescription = "Email", tint = MaterialTheme.colorScheme.onSecondary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Secondary Color Example", color = MaterialTheme.colorScheme.onSecondary)
    }
}

@Composable
fun HighlightedMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(text, color = MaterialTheme.colorScheme.onPrimary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(label: String) {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        label = { Text(label) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedIndicatorColor = Color.White,
            unfocusedIndicatorColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ActionButtons() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Primary Button", color = MaterialTheme.colorScheme.onPrimary)
        }

        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Secondary Button", color = MaterialTheme.colorScheme.onSecondary)
        }

        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text("Tertiary Button", color = MaterialTheme.colorScheme.onTertiary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ColorDemoPreview() {
    ColorDemoScreen()
}
