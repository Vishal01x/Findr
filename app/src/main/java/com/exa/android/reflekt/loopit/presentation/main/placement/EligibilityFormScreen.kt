package com.exa.android.reflekt.loopit.presentation.main.placement

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.reflekt.loopit.presentation.main.placement.components.GlassCard
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidMeshBackground
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EligibilityFormScreen(
    onBack: () -> Unit = {},
    onSubmit: () -> Unit = {}
) {
    var collegeName by remember { mutableStateOf("") }
    var gradYear by remember { mutableStateOf("") }
    var cgpa by remember { mutableStateOf("") }
    var tenthPercent by remember { mutableStateOf("") }
    var twelfthPercent by remember { mutableStateOf("") }
    var hasActiveBacklogs by remember { mutableStateOf<Boolean?>(null) }
    var hasPreviousBacklogs by remember { mutableStateOf<Boolean?>(null) }

    val scrollState = rememberScrollState()

    Box(Modifier.fillMaxSize().background(LiquidColors.BgBase)) {
        LiquidMeshBackground()
        Scaffold(
            topBar = {
                LiquidTopBar(
                    title = "Eligibility Check",
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = LiquidColors.TextPrimary)
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 90.dp) // Space for bottom button
                ) {
                    // Scrollable Form
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text(
                            "Fill in your academic details to check if you're eligible for this drive.",
                            fontSize = 14.sp, color = LiquidColors.TextSecondary
                        )

                        // Section 1: Education
                        GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("Education", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)

                                GlassTextField(
                                    value = collegeName,
                                    onValueChange = { collegeName = it },
                                    label = "College Name",
                                    icon = Icons.Default.School
                                )

                                GlassTextField(
                                    value = gradYear,
                                    onValueChange = { gradYear = it },
                                    label = "Graduation Year",
                                    icon = Icons.Default.CalendarToday,
                                    keyboardType = KeyboardType.Number
                                )
                            }
                        }

                        // Section 2: Grades
                        GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("Academic Performance", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)

                                GlassTextField(
                                    value = cgpa,
                                    onValueChange = { cgpa = it },
                                    label = "Current CGPA (out of 10)",
                                    icon = Icons.Default.StarRate,
                                    keyboardType = KeyboardType.Decimal
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    GlassTextField(
                                        value = tenthPercent,
                                        onValueChange = { tenthPercent = it },
                                        label = "10th %",
                                        icon = Icons.Default.Grade,
                                        keyboardType = KeyboardType.Decimal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    GlassTextField(
                                        value = twelfthPercent,
                                        onValueChange = { twelfthPercent = it },
                                        label = "12th %",
                                        icon = Icons.Default.Grade,
                                        keyboardType = KeyboardType.Decimal,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Section 3: Backlogs
                        GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                Text("Backlog History", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)

                                BooleanSegmentedControl(
                                    label = "Do you have any active backlogs?",
                                    selectedValue = hasActiveBacklogs,
                                    onSelectionChanged = { hasActiveBacklogs = it }
                                )

                                BooleanSegmentedControl(
                                    label = "Did you have any previous backlogs?",
                                    selectedValue = hasPreviousBacklogs,
                                    onSelectionChanged = { hasPreviousBacklogs = it }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Bottom Action Button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(LiquidColors.BgBase.copy(alpha = 0.85f))
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .navigationBarsPadding()
                ) {
                    val isFormComplete = collegeName.isNotBlank() && gradYear.isNotBlank() &&
                                         cgpa.isNotBlank() && tenthPercent.isNotBlank() &&
                                         twelfthPercent.isNotBlank() &&
                                         hasActiveBacklogs != null && hasPreviousBacklogs != null

                    val primaryColor = LiquidColors.Primary
                    val glassSurface = LiquidColors.GlassSurface

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isFormComplete)
                                    Brush.horizontalGradient(listOf(primaryColor, Color(0xFF0EA5E9)))
                                else
                                    Brush.horizontalGradient(listOf(glassSurface, glassSurface))
                            )
                            .clickable(enabled = isFormComplete) { onSubmit() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Check Eligibility",
                                color = if (isFormComplete) Color.White else LiquidColors.TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (isFormComplete) {
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp), tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Reusable Components ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = LiquidColors.Primary, modifier = Modifier.size(20.dp)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LiquidColors.Primary,
            unfocusedBorderColor = LiquidColors.GlassBorder,
            focusedLabelColor = LiquidColors.Primary,
            unfocusedLabelColor = LiquidColors.TextMuted,
            cursorColor = LiquidColors.Primary,
            focusedContainerColor = LiquidColors.GlassSurface,
            unfocusedContainerColor = LiquidColors.GlassSurface,
            focusedTextColor = LiquidColors.TextPrimary,
            unfocusedTextColor = LiquidColors.TextPrimary
        )
    )
}

@Composable
private fun BooleanSegmentedControl(
    label: String,
    selectedValue: Boolean?,
    onSelectionChanged: (Boolean) -> Unit
) {
    Column {
        Text(label, fontSize = 14.sp, color = LiquidColors.TextSecondary, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LiquidColors.GlassSurface, RoundedCornerShape(12.dp))
                .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SegmentOption(
                text = "Yes",
                isSelected = selectedValue == true,
                onClick = { onSelectionChanged(true) },
                modifier = Modifier.weight(1f)
            )
            SegmentOption(
                text = "No",
                isSelected = selectedValue == false,
                onClick = { onSelectionChanged(false) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SegmentOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(if (isSelected) LiquidColors.Primary else Color.Transparent)
    val textColor by animateColorAsState(if (isSelected) Color.White else LiquidColors.TextPrimary)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = textColor)
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Light Mode")
@Composable
private fun EligibilityFormScreenPreviewLight() = MaterialTheme {
    EligibilityFormScreen()
}

@Preview(showBackground = true, name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EligibilityFormScreenPreviewDark() = MaterialTheme {
    EligibilityFormScreen()
}
