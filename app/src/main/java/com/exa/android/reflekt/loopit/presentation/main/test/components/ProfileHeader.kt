package com.exa.android.reflekt.loopit.presentation.main.test.components


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.presentation.main.Home.component.ImageUsingCoil

@Composable
fun ProfileHeader(imageUrl : String? = null){
    val context = LocalContext.current
    val imageUrl =

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomEnd) {
            ImageUsingCoil(context, imageUrl, R.drawable.placeholder, Modifier.size(100.dp))
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.Blue, CircleShape)
                    .padding(4.dp)
                    .align(Alignment.BottomEnd)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text("Ankit Raj", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Software Developer\nSan Francisco, CA", fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
fun EditableCard(title: String, content: String, onEdit: (String) -> Unit) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(title, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.clickable { showEditDialog = true })
            }
            Spacer(Modifier.height(8.dp))
            Text(content)
        }
    }

    if (showEditDialog) {
        var textState by remember { mutableStateOf(TextFieldValue(content)) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit $title") },
            text = {
                TextField(value = textState, onValueChange = { textState = it }, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                TextButton(onClick = {
                    onEdit(textState.text)
                    showEditDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillsCard(skills: List<String>) {
    Card(elevation = CardDefaults.cardElevation(4.dp), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Skills", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                skills.forEach {
                    Chip(text = it)
                }
            }
        }
    }
}

@Composable
fun Chip(text: String) {
    Box(
        modifier = Modifier
            .background(Color.LightGray, shape = RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 12.sp)
    }
}

@Composable
fun ExtracurricularCard(items: List<ExtracurricularActivity>, onAddClick: () -> Unit) {
    Card(elevation = CardDefaults.cardElevation(4.dp), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Extracurricular Activities", fontWeight = FontWeight.Bold)
                IconButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
            items.forEach {
                Text("• ${it.name} (${it.link}) - ${it.description}", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ExperienceCard() {
    Card(elevation = CardDefaults.cardElevation(4.dp), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Experience", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Senior Developer - ABC Corp\nDeveloped mobile applications using React Native and Firebase.")
        }
    }
}
