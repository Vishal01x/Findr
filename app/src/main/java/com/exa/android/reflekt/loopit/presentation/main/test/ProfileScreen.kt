package com.exa.android.reflekt.loopit.presentation.main.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.presentation.main.test.components.AddExtracurricularDialog
import com.exa.android.reflekt.loopit.presentation.main.test.components.EditableCard
import com.exa.android.reflekt.loopit.presentation.main.test.components.ExperienceCard
import com.exa.android.reflekt.loopit.presentation.main.test.components.ExtracurricularActivity
import com.exa.android.reflekt.loopit.presentation.main.test.components.ExtracurricularCard
import com.exa.android.reflekt.loopit.presentation.main.test.components.ProfileHeader
import com.exa.android.reflekt.loopit.presentation.main.test.components.SkillsCard

@Composable
fun ProfileScreen() {
    var aboutText by remember { mutableStateOf("Passionate React Native Developer with 3+ years of experience in mobile app development.") }
    var extracurriculars =  remember { mutableStateListOf<ExtracurricularActivity>() }
    var showDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            ProfileHeader()
            Spacer(Modifier.height(16.dp))
            EditableCard(
                title = "About",
                content = aboutText,
                onEdit = {
                    aboutText = it
                }
            )
            Spacer(Modifier.height(16.dp))
            SkillsCard(
                listOf("React Native", "Firebase", "JavaScript", "TypeScript", "NodeJS", "Kotlin", "Python")
            )
            Spacer(Modifier.height(16.dp))
            ExtracurricularCard(extracurriculars, onAddClick = { showDialog = true })
            Spacer(Modifier.height(16.dp))
            ExperienceCard()
        }
    }

    if (showDialog) {
        AddExtracurricularDialog(
            onDismiss = { showDialog = false },
            onAdd = {
                extracurriculars.add(it)
                showDialog = false
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    ProfileScreen()
}

