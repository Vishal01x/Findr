@file:OptIn(ExperimentalLayoutApi::class)

package com.exa.android.reflekt.loopit.presentation.main.profile.components.education

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.CollegeInfo
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ExperienceInfo

@Composable
fun ExperienceCard(
    isCurUser: Boolean,
    experienceInfo: ExperienceInfo,
    onEditExperience: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(LiquidColors.GlassSurface)
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isCurUser) {
                    IconButton(onClick = onEditExperience) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Experience",
                            tint = MaterialTheme.colorScheme.onSurface
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
                            color = MaterialTheme.colorScheme.onSurface
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            } else {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "No experience details added yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(LiquidColors.GlassSurface)
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isCurUser) {
                    IconButton(onClick = onEditEducation) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Education",
                            tint = MaterialTheme.colorScheme.onSurface
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
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (collegeInfo.stream.isNotBlank()) {
                            Text(
                                text = collegeInfo.stream,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val academicDetails = listOfNotNull(
                            collegeInfo.year.takeIf { it.isNotBlank() }?.let { "Year: $it" },
                            collegeInfo.semester.takeIf { it.isNotBlank() }?.let { "Semester: $it" },
                            collegeInfo.classWithSection.takeIf { it.isNotBlank() }?.let { "Class: $it" }
                        )

                        if (academicDetails.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                academicDetails.forEach { detail ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                    ) {
                                        Text(
                                            text = detail,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        val duration = if (collegeInfo.endDate.isNullOrBlank()) {
                            "${collegeInfo.startDate} - Present"
                        } else {
                            "${collegeInfo.startDate} - ${collegeInfo.endDate}"
                        }

                        Text(
                            text = duration,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!collegeInfo.grade.isNullOrBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Grade: ${collegeInfo.grade}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }

                    }
                }
            } else {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isCurUser) "Add your college, class year, semester, and section." else "No education details added yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


