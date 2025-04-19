package com.exa.android.reflekt.loopit.presentation.main.test

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.tooling.preview.Preview
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.presentation.main.Home.component.ImageUsingCoil

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreennn() {
    var expandedAbout by remember { mutableStateOf(false) }
    val featuredItems = listOf("LeetCode", "GeeksforGeeks", "Projects", "Certifications")

    Scaffold(
        containerColor = Color.White,
        contentColor = Color.White
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            item { ProfileHeaderr() }

            item {
                Spacer(Modifier.height(48.dp))
                ProfileInfoSectionn(
                    name = "Vishal Sharma",
                    title = "Android Developer | LNCT Bhopal",
                    stats = mapOf(
                        "Connections" to "1.5K",
                        "Followers" to "2.8K",
                        "Projects" to "15"
                    )
                )
            }

            item { ActionButtonss() }

            item {
                SectionTitlee("About")
                ExpandableAboutTextt(
                    text = "As a third-year undergraduate student at Lakshmi Narain College...",
                    expanded = expandedAbout,
                    onExpandClick = { expandedAbout = !expandedAbout }
                )
            }

            item {
                SectionTitlee("Top Skills")
                SkillsSectionn(skills = listOf(
                    "Kotlin", "C++", "SQL", "Machine Learning",
                    "Android Development", "Firebase", "Problem Solving"
                ))
            }

            item {
                SectionTitlee("Experience")
                ExperienceItemm(
                    position = "Android Developer Intern",
                    company = "Word",
                    duration = "Jun 2024 - Aug 2024",
                    location = "On-site",
                    achievements = listOf(
                        "Optimized authentication flow using Firebase",
                        "Integrated Geo Fire and Google Maps SDK"
                    )
                )
            }

            item {
                SectionTitlee("Education")
                EducationItemm(
                    institution = "Lakshmi Narain College of Technology",
                    degree = "B.Tech Computer Science",
                    duration = "Nov 2022 - Aug 2026"
                )
            }

            item {
                SectionTitlee("Featured")
                FeaturedContentt(items = featuredItems)
            }
        }
    }
}

@Composable
private fun ProfileHeaderr() {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            ImageUsingCoil(context, "", R.drawable.placeholder, modifier = Modifier
                .height(150.dp)
                .fillMaxWidth())

            // Profile Image
            ImageUsingCoil(context, "", R.drawable.placeholder, modifier = Modifier
                .size(140.dp)
                .align(Alignment.BottomStart)
                .offset(x = 15.dp, y = 60.dp)
                .border(3.dp, Color.White, CircleShape)
                .clip(CircleShape))
        }

        // Social Media Icons
        Row(
            modifier = Modifier
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { /* LinkedIn */ }) {
                Icon(
                    imageVector = Icons.Default.Lyrics,
                    contentDescription = "LinkedIn",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = { /* YouTube */ }) {
                Icon(
                    imageVector = Icons.Default.SmartDisplay,
                    contentDescription = "YouTube",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Message Button and Rating
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 70.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { /* Handle message */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Message", style = MaterialTheme.typography.labelLarge)
            }

            // Rating Chip
            Row(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "4.5/5.0",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        // Additional Stats
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "5.7 Million learners",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "25M monthly visits",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
@Composable
private fun ProfileInfoSectionn(
    name: String,
    title: String,
    stats: Map<String, String>
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black.copy(.9f),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            stats.forEach { (key, value) ->
                StatItem(label = key, value = value)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionButtonss() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { /* Handle connect */ },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Connect")
        }

        OutlinedButton(
            onClick = { /* Handle message */ },
            modifier = Modifier.weight(1f),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.dp,
                //color = MaterialTheme.colorScheme.outline
            )
        ) {
            Text("Message")
        }
    }
}

@Composable
private fun SectionTitlee(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier
            .padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
            .fillMaxWidth()
    )
}

@Composable
private fun ExpandableAboutTextt(
    text: String,
    expanded: Boolean,
    onExpandClick: () -> Unit
) {
    val displayText = if (expanded) text else text.take(150) + "..."

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(
            onClick = onExpandClick,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text(if (expanded) "Show less" else "See more")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillsSectionn(skills: List<String>) {
    FlowRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        skills.forEach { skill ->
            AssistChip(
                onClick = { /* Handle skill click */ },
                label = { Text(skill) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier.size(AssistChipDefaults.IconSize)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    leadingIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun ExperienceItemm(
    position: String,
    company: String,
    duration: String,
    location: String,
    achievements: List<String>
) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = position,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = company,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$duration • $location",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                achievements.forEach { achievement ->
                    Text(
                        text = "• $achievement",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EducationItemm(
    institution: String,
    degree: String,
    duration: String
) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "Education",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = institution,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = degree,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = duration,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun FeaturedContentt(items: List<String>) {
    LazyRow(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            Card(
                modifier = Modifier
                    .width(200.dp)
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileScreennn()
    }
}