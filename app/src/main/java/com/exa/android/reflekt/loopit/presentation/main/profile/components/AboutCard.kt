package com.exa.android.reflekt.loopit.presentation.main.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.EditProfileViewModel
import com.exa.android.reflekt.loopit.util.Response

@Composable
fun AboutCard(
    isCurUser : Boolean,
    title: String,
    content: String,
    editProfileViewModel: EditProfileViewModel = hiltViewModel()
) {
    val response = editProfileViewModel.responseState

    var isEditing by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var localText by remember { mutableStateOf(content) }

    LaunchedEffect(response) {
        if (response is Response.Success) {
            isEditing = false
        }
    }

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiary)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Title + Edit Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onTertiary
                )
                if(isCurUser) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier
                            .clickable {
                                isEditing = true
                                localText = content
                            }
                            .padding(start = 8.dp),
                        tint = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (isEditing) {
                // Editable TextField
                OutlinedTextField(
                    value = localText,
                    onValueChange = {
                        localText = it
                        editProfileViewModel.updateAbout(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background),
                    maxLines = 5,
                    label = { Text("About") } // Optional label
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { isEditing = false },
                        enabled = response !is Response.Loading
                    ) {
                        Text("Cancel")
                    }

                    TextButton(
                        onClick = {
                            editProfileViewModel.updateUserAbout()
                        },
                        enabled = response !is Response.Loading
                    ) {
                        if (response is Response.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 8.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }

                if (response is Response.Error) {
                    Text(
                        text = response.message ?: "Something went wrong",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                // Normal about text with Show more/less
                Text(
                    text = content,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                )

                if (content.length > 100) {
                    Text(
                        text = if (expanded) "Show less" else "Show more",
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { expanded = !expanded }
                    )
                }
            }
        }
    }
}
