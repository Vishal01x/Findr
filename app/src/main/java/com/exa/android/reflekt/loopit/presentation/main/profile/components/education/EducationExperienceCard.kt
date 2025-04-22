package com.exa.android.reflekt.loopit.presentation.main.profile.components.education

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.util.model.Profile.CollegeInfo
import com.exa.android.reflekt.loopit.util.model.Profile.ExperienceInfo

@Composable
fun ExperienceCard(
    isCurUser: Boolean,
    experienceInfo: ExperienceInfo,
    onEditExperience: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Experience",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onTertiary
                )
                if (isCurUser) {
                    IconButton(onClick = onEditExperience) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Experience",
                            tint = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }

            if (isCurUser || !isCurUser && experienceInfo.title.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Row {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = "Work Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = experienceInfo.title.ifBlank { "Unspecified" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                        Text(
                            text = experienceInfo.companyName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val durationText = if (experienceInfo.currentlyWorking) {
                            "${experienceInfo.startDate} - Present"
                        } else {
                            "${experienceInfo.startDate} - ${experienceInfo.endDate}"
                        }

                        Text(
                            text = "$durationText • ${experienceInfo.location}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        if (experienceInfo.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = experienceInfo.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

        }
    }
}


@Composable
fun EducationCard(
    isCurUser: Boolean,
    collegeInfo: CollegeInfo,
    onEditEducation: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Education",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onTertiary
                )
                if (isCurUser) {
                    IconButton(onClick = onEditEducation) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Education",
                            tint = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }

            if (collegeInfo.instituteName.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))

                Row {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "School Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = collegeInfo.instituteName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiary
                        )

                        if (collegeInfo.stream.isNotBlank()) {
                            Text(
                                text = collegeInfo.stream,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val duration = if (collegeInfo.endDate.isNullOrBlank()) {
                            "${collegeInfo.startDate} - Present"
                        } else {
                            "${collegeInfo.startDate} - ${collegeInfo.endDate}"
                        }

                        Text(
                            text = duration,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        if (!collegeInfo.grade.isNullOrBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Grade: ${collegeInfo.grade}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Optional description (uncomment if needed)
                        // if (!collegeInfo.description.isNullOrBlank()) {
                        //     Spacer(modifier = Modifier.height(6.dp))
                        //     Text(
                        //         text = collegeInfo.description,
                        //         style = MaterialTheme.typography.bodyMedium,
                        //         color = MaterialTheme.colorScheme.onSurfaceVariant
                        //     )
                        // }
                    }
                }
            }
        }
    }
}


