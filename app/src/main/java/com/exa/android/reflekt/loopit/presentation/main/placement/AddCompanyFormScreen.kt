package com.exa.android.reflekt.loopit.presentation.main.placement

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exa.android.reflekt.loopit.presentation.main.placement.components.GlassCard
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidMeshBackground
import com.exa.android.reflekt.loopit.presentation.main.placement.model.*
import com.exa.android.reflekt.loopit.presentation.main.placement.vm.AddCompanyFormEvent
import com.exa.android.reflekt.loopit.presentation.main.placement.vm.AddCompanyFormState
import com.exa.android.reflekt.loopit.presentation.main.placement.vm.AddCompanyFormViewModel

// ─── Step labels ─────────────────────────────────────────────────────────────

private val stepLabels = listOf(
    "Basic Info",
    "Eligibility",
    "Package",
    "Process",
    "Resources",
    "Culture & Contact"
)

private val stepIcons = listOf(
    Icons.Default.Business,
    Icons.Default.School,
    Icons.Default.CurrencyRupee,
    Icons.Default.Timeline,
    Icons.Default.MenuBook,
    Icons.Default.Groups
)

// ─── Root Screen ──────────────────────────────────────────────────────────────

@Composable
fun AddCompanyFormScreen(
    onBack: () -> Unit = {},
    onSuccess: () -> Unit = {},
    viewModel: AddCompanyFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Side-effect toast handling
    var toastMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { msg ->
            toastMessage = msg
        }
    }

    // Auto-dismiss toast
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            kotlinx.coroutines.delay(3000)
            toastMessage = null
        }
    }

    // Navigate on success
    LaunchedEffect(state.submitSuccess) {
        if (state.submitSuccess) onSuccess()
    }

    Box(modifier = Modifier.fillMaxSize().background(LiquidColors.BgBase)) {

        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar
            FormTopBar(
                currentStep = state.currentStep,
                onBack = { if (state.currentStep == 0) onBack() else viewModel.onEvent(AddCompanyFormEvent.PrevStep) }
            )

            // ── Step Progress Indicator
            StepProgressBar(current = state.currentStep, total = state.totalSteps)

            // ── Step Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AnimatedContent(
                    targetState = state.currentStep,
                    transitionSpec = {
                        if (targetState > initialState)
                            slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                        else
                            slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    },
                    label = "step_content"
                ) { step ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        when (step) {
                            0 -> Step0BasicInfo(state, viewModel::onEvent)
                            1 -> Step1Eligibility(state, viewModel::onEvent)
                            2 -> Step2Package(state, viewModel::onEvent)
                            3 -> Step3Process(state, viewModel::onEvent)
                            4 -> Step4Resources(state, viewModel::onEvent)
                            5 -> Step5Culture(state, viewModel::onEvent)
                        }
                        Spacer(Modifier.height(100.dp)) // bottom button clearance
                    }
                }
            }
        }

        // ── Bottom Navigation
        FormBottomBar(
            state = state,
            modifier = Modifier.align(Alignment.BottomCenter),
            onEvent = viewModel::onEvent
        )

        // ── Toast
        toastMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 80.dp, start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (msg.startsWith("✅")) Color(0xFF16A34A) else Color(0xFF7C3AED))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(msg, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // ── Error dialog
        state.submitError?.let { err ->
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(AddCompanyFormEvent.DismissError) },
                title = { Text("Submission Failed") },
                text  = { Text(err) },
                confirmButton = {
                    TextButton(onClick = { viewModel.onEvent(AddCompanyFormEvent.DismissError) }) { Text("OK") }
                },
                containerColor = LiquidColors.GlassSurface
            )
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@Composable
private fun FormTopBar(currentStep: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(LiquidColors.GlassSurface)
                .border(1.dp, LiquidColors.GlassBorder, CircleShape)
                .clickableWithNoIndication(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ArrowBack, "Back", tint = LiquidColors.TextPrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Add Company", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
            Text(stepLabels[currentStep], fontSize = 13.sp, color = LiquidColors.Primary)
        }
        Icon(stepIcons[currentStep], null, tint = LiquidColors.Primary, modifier = Modifier.size(24.dp))
    }
}

// ─── Step Progress Bar ────────────────────────────────────────────────────────

@Composable
private fun StepProgressBar(current: Int, total: Int) {
    val progress by animateFloatAsState(
        targetValue = (current + 1f) / total.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "step_progress"
    )
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Step ${current + 1} of $total", fontSize = 12.sp, color = LiquidColors.TextSecondary)
            Text("${((current + 1f) / total * 100).toInt()}%", fontSize = 12.sp, color = LiquidColors.Primary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50.dp))
                .background(LiquidColors.GlassSurface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progress)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Brush.horizontalGradient(listOf(LiquidColors.Primary, Color(0xFF7C3AED))))
            )
        }
    }
}

// ─── Bottom Bar ───────────────────────────────────────────────────────────────

@Composable
private fun FormBottomBar(
    state: AddCompanyFormState,
    modifier: Modifier = Modifier,
    onEvent: (AddCompanyFormEvent) -> Unit
) {
    val isLast = state.currentStep == state.totalSteps - 1

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(LiquidColors.BgBase.copy(alpha = 0.9f))
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.currentStep > 0) {
                OutlinedButton(
                    onClick = { onEvent(AddCompanyFormEvent.PrevStep) },
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, LiquidColors.GlassBorder)
                ) {
                    Icon(Icons.Default.ArrowBack, null, tint = LiquidColors.TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Back", color = LiquidColors.TextSecondary)
                }
            }
            Button(
                onClick = {
                    if (isLast) onEvent(AddCompanyFormEvent.Submit)
                    else onEvent(AddCompanyFormEvent.NextStep)
                },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary),
                enabled = !state.isSubmitting
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        if (isLast) "Submit Company" else "Next",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        if (isLast) Icons.Default.CheckCircle else Icons.Default.ArrowForward,
                        null, tint = Color.White, modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ─── Step 0: Basic Info ────────────────────────────────────────────────────────

@Composable
private fun Step0BasicInfo(state: AddCompanyFormState, onEvent: (AddCompanyFormEvent) -> Unit) {
    StepSection(title = "Company Basics", icon = Icons.Default.Business) {
        FormField("Company Name *", Icons.Default.Business, state.name, KeyboardType.Text) {
            onEvent(AddCompanyFormEvent.NameChanged(it))
        }
        FormField("Tagline (e.g. 'Fintech • San Francisco')", Icons.Default.Tag, state.tagline, KeyboardType.Text) {
            onEvent(AddCompanyFormEvent.TaglineChanged(it))
        }
        FormField("Website URL", Icons.Default.Link, state.website, KeyboardType.Uri) {
            onEvent(AddCompanyFormEvent.WebsiteChanged(it))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FormField("Location (e.g. Bangalore)", Icons.Default.LocationOn, state.location, KeyboardType.Text, Modifier.weight(1f)) {
                onEvent(AddCompanyFormEvent.LocationChanged(it))
            }
            FormField("Employees (e.g. 10k+)", Icons.Default.People, state.employeeCount, KeyboardType.Text, Modifier.weight(1f)) {
                onEvent(AddCompanyFormEvent.EmployeeCountChanged(it))
            }
        }
        FormField("Job Role (e.g. SDE Intern, SDE-1)", Icons.Default.Work, state.jobRole, KeyboardType.Text) {
            onEvent(AddCompanyFormEvent.JobRoleChanged(it))
        }
    }

    StepSection(title = "About the Company", icon = Icons.Default.Info) {
        FormTextArea("Company Description / Overview", state.description) {
            onEvent(AddCompanyFormEvent.DescriptionChanged(it))
        }
    }

    StepSection(title = "Hiring Type", icon = Icons.Default.Badge) {
        SegmentedControl(
            options = RoleType.entries.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
            selectedIndex = RoleType.entries.indexOf(state.roleType),
            onSelect = { onEvent(AddCompanyFormEvent.RoleTypeChanged(RoleType.entries[it])) }
        )
    }

    StepSection(title = "Drive Status", icon = Icons.Default.Bolt) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Mark as Live Drive", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = LiquidColors.TextPrimary)
                Text("Students will see a live indicator", fontSize = 12.sp, color = LiquidColors.TextSecondary)
            }
            Switch(
                checked = state.isLiveProcess,
                onCheckedChange = { onEvent(AddCompanyFormEvent.IsLiveChanged(it)) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = LiquidColors.Primary)
            )
        }
    }
}

// ─── Step 1: Eligibility ───────────────────────────────────────────────────────

private val allBranches = listOf("CSE", "IT", "ECE", "EEE", "ME", "CE", "MCA", "MBA", "All")

@Composable
private fun Step1Eligibility(state: AddCompanyFormState, onEvent: (AddCompanyFormEvent) -> Unit) {
    StepSection(title = "Academic Cutoffs", icon = Icons.Default.StarRate) {
        FormField("Minimum CGPA (e.g. 7.5)", Icons.Default.StarRate, state.minCgpa, KeyboardType.Decimal) {
            onEvent(AddCompanyFormEvent.MinCgpaChanged(it))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FormField("Min 10th %", Icons.Default.Grade, state.tenth, KeyboardType.Decimal, Modifier.weight(1f)) {
                onEvent(AddCompanyFormEvent.TenthChanged(it))
            }
            FormField("Min 12th %", Icons.Default.Grade, state.twelfth, KeyboardType.Decimal, Modifier.weight(1f)) {
                onEvent(AddCompanyFormEvent.TwelfthChanged(it))
            }
        }
        FormField("Allowed Backlogs (0 = none)", Icons.Default.Warning, state.allowedBacklogs, KeyboardType.Number) {
            onEvent(AddCompanyFormEvent.BacklogsChanged(it))
        }
    }

    StepSection(title = "Allowed Branches", icon = Icons.Default.AccountTree) {
        Text("Select all eligible branches:", fontSize = 13.sp, color = LiquidColors.TextSecondary)
        Spacer(Modifier.height(4.dp))
        BranchChips(selected = state.allowedBranches, branches = allBranches) { branch ->
            onEvent(AddCompanyFormEvent.BranchToggled(branch))
        }
    }
}

// ─── Step 2: Package & Difficulty ─────────────────────────────────────────────

@Composable
private fun Step2Package(state: AddCompanyFormState, onEvent: (AddCompanyFormEvent) -> Unit) {
    StepSection(title = "Compensation", icon = Icons.Default.CurrencyRupee) {
        if (state.roleType != RoleType.INTERN) {
            FormField("Package CTC (e.g. 32 LPA)", Icons.Default.CurrencyRupee, state.packageLpa, KeyboardType.Text) {
                onEvent(AddCompanyFormEvent.PackageChanged(it))
            }
        }
        if (state.roleType != RoleType.FULLTIME) {
            FormField("Stipend (e.g. ₹40,000/mo)", Icons.Default.Payments, state.stipendPerMonth, KeyboardType.Text) {
                onEvent(AddCompanyFormEvent.StipendChanged(it))
            }
        }
        FormField("Approx. Hires Per Season", Icons.Default.Group, state.hiresCount, KeyboardType.Number) {
            onEvent(AddCompanyFormEvent.HiresCountChanged(it))
        }
    }

    StepSection(title = "Interview Difficulty", icon = Icons.Default.TrendingUp) {
        Text("How tough is the recruitment process?", fontSize = 13.sp, color = LiquidColors.TextSecondary)
        Spacer(Modifier.height(8.dp))
        SegmentedControl(
            options = listOf("🟢 Easy", "🟡 Medium", "🔴 Hard"),
            selectedIndex = DifficultyLevel.entries.indexOf(state.level),
            onSelect = { onEvent(AddCompanyFormEvent.LevelChanged(DifficultyLevel.entries[it])) }
        )
    }
}

// ─── Step 3: Recruitment Process ──────────────────────────────────────────────

@Composable
private fun Step3Process(state: AddCompanyFormState, onEvent: (AddCompanyFormEvent) -> Unit) {
    var showAddDialog by remember { mutableStateOf(false) }

    StepSection(title = "Interview Rounds", icon = Icons.Default.Timeline) {
        if (state.recruitmentStages.isEmpty()) {
            EmptyHint("No rounds added yet. Tap + to add a round.")
        } else {
            state.recruitmentStages.forEachIndexed { idx, stage ->
                StageItem(stage = stage, index = idx + 1, onRemove = { onEvent(AddCompanyFormEvent.StageRemoved(idx)) })
                if (idx < state.recruitmentStages.lastIndex) HorizontalDivider(color = LiquidColors.GlassBorder)
            }
        }
        Spacer(Modifier.height(4.dp))
        OutlinedButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, LiquidColors.Primary.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.Add, null, tint = LiquidColors.Primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add Round", color = LiquidColors.Primary)
        }
    }

    if (showAddDialog) {
        AddStageDialog(
            nextOrder = state.recruitmentStages.size + 1,
            onAdd = { stage ->
                onEvent(AddCompanyFormEvent.StageAdded(stage))
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun StageItem(stage: RecruitmentStage, index: Int, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape)
                .background(LiquidColors.Primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("$index", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LiquidColors.Primary)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(stage.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LiquidColors.TextPrimary)
            if (stage.platform.isNotBlank())
                Text(stage.platform, fontSize = 12.sp, color = LiquidColors.TextSecondary)
            if (stage.description.isNotBlank())
                Text(stage.description, fontSize = 12.sp, color = LiquidColors.TextMuted, modifier = Modifier.padding(top = 2.dp))
            if (stage.durationMins > 0)
                Text("${stage.durationMins} mins", fontSize = 11.sp, color = LiquidColors.TextMuted)
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, null, tint = LiquidColors.TextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun AddStageDialog(nextOrder: Int, onAdd: (RecruitmentStage) -> Unit, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LiquidColors.GlassSurface,
        title = { Text("Add Round $nextOrder", color = LiquidColors.TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DialogTextField("Round Title *", Icons.Default.Title, title, KeyboardType.Text) { title = it }
                DialogTextField("Platform (e.g. HackerRank)", Icons.Default.Computer, platform, KeyboardType.Text) { platform = it }
                DialogTextField("Duration (mins)", Icons.Default.Timer, duration, KeyboardType.Number) { duration = it }
                DialogTextField("Description", Icons.Default.Description, description, KeyboardType.Text) { description = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(RecruitmentStage(
                            order = nextOrder,
                            title = title,
                            platform = platform,
                            durationMins = duration.toIntOrNull() ?: 0,
                            description = description,
                            status = StageStatus.UPCOMING
                        ))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary)
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = LiquidColors.TextSecondary) }
        }
    )
}

// ─── Step 4: Resources & PDFs ─────────────────────────────────────────────────

@Composable
private fun Step4Resources(state: AddCompanyFormState, onEvent: (AddCompanyFormEvent) -> Unit) {
    var showLinkDialog by remember { mutableStateOf(false) }

    StepSection(title = "Preparation Guide", icon = Icons.Default.MenuBook) {
        FormTextArea("Write preparation tips, key topics, advice...", state.prepGuide) {
            onEvent(AddCompanyFormEvent.PrepGuideChanged(it))
        }
    }

    StepSection(title = "Reference Links", icon = Icons.Default.Link) {
        if (state.referenceLinks.isEmpty()) {
            EmptyHint("No links added yet.")
        } else {
            state.referenceLinks.forEachIndexed { idx, link ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Link, null, tint = LiquidColors.Primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(link.label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = LiquidColors.TextPrimary)
                        Text(link.url, fontSize = 11.sp, color = LiquidColors.TextSecondary, maxLines = 1)
                    }
                    IconButton(onClick = { onEvent(AddCompanyFormEvent.RefLinkRemoved(idx)) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, null, tint = LiquidColors.TextSecondary, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
        OutlinedButton(
            onClick = { showLinkDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, LiquidColors.Primary.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.Add, null, tint = LiquidColors.Primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add Link", color = LiquidColors.Primary)
        }
    }

    StepSection(title = "PDF Attachments", icon = Icons.Default.PictureAsPdf) {
        if (state.pdfAttachments.isEmpty()) {
            EmptyHint("No PDFs uploaded yet.")
        } else {
            state.pdfAttachments.forEach { pdf ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PictureAsPdf, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(pdf.name, fontSize = 13.sp, color = LiquidColors.TextPrimary)
                        Text("${pdf.sizeKb} KB", fontSize = 11.sp, color = LiquidColors.TextSecondary)
                    }
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                }
            }
        }
        if (state.isPdfUploading) {
            LinearProgressIndicator(
                progress = { state.pdfUploadProgress / 100f },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50.dp)),
                color = LiquidColors.Primary
            )
        }
        // Note: PDF picking via system file picker is wired from the host activity/composable.
        // The UI hint is shown here; the actual Uri is passed via AddCompanyFormEvent.PdfSelected.
        Text(
            "📎 PDF upload: use the 'Pick PDF' button in your host Activity/NavHost to select a file and call onEvent(PdfSelected(...)).",
            fontSize = 11.sp,
            color = LiquidColors.TextMuted,
            modifier = Modifier.padding(top = 4.dp)
        )
    }

    if (showLinkDialog) {
        AddLinkDialog(
            onAdd = { link ->
                onEvent(AddCompanyFormEvent.RefLinkAdded(link))
                showLinkDialog = false
            },
            onDismiss = { showLinkDialog = false }
        )
    }
}

@Composable
private fun AddLinkDialog(onAdd: (ReferenceLink) -> Unit, onDismiss: () -> Unit) {
    var label by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LiquidColors.GlassSurface,
        title = { Text("Add Reference Link", color = LiquidColors.TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DialogTextField("Label (e.g. Striver Sheet)", Icons.Default.Label, label, KeyboardType.Text) { label = it }
                DialogTextField("URL", Icons.Default.Link, url, KeyboardType.Uri) { url = it }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (label.isNotBlank() && url.isNotBlank()) onAdd(ReferenceLink(label, url)) },
                colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = LiquidColors.TextSecondary) } }
    )
}

// ─── Step 5: Culture & Contact ────────────────────────────────────────────────

@Composable
private fun Step5Culture(state: AddCompanyFormState, onEvent: (AddCompanyFormEvent) -> Unit) {
    var showContactDialog by remember { mutableStateOf(false) }

    StepSection(title = "Work Culture", icon = Icons.Default.Diversity3) {
        FormTextArea("Describe work-life balance, culture, remote policy...", state.workCulture) {
            onEvent(AddCompanyFormEvent.WorkCultureChanged(it))
        }
    }

    StepSection(title = "Hiring Dates", icon = Icons.Default.CalendarToday) {
        if (state.roleType != RoleType.FULLTIME) {
            FormField("Intern Hiring Opens / Deadline", Icons.Default.Event, state.internDeadline, KeyboardType.Text) {
                onEvent(AddCompanyFormEvent.InternDeadlineChanged(it))
            }
        }
        if (state.roleType != RoleType.INTERN) {
            FormField("Full-Time Hiring Opens / Deadline", Icons.Default.Event, state.fulltimeDeadline, KeyboardType.Text) {
                onEvent(AddCompanyFormEvent.FulltimeDeadlineChanged(it))
            }
        }
    }

    StepSection(title = "Placement Contacts", icon = Icons.Default.ContactPhone) {
        if (state.placementContacts.isEmpty()) {
            EmptyHint("No contacts added. Add placement officers here.")
        } else {
            state.placementContacts.forEachIndexed { idx, contact ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(LiquidColors.Primary.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            contact.name.take(1).uppercase(),
                            fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LiquidColors.Primary
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(contact.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LiquidColors.TextPrimary)
                        Text(contact.role, fontSize = 12.sp, color = LiquidColors.TextSecondary)
                        if (contact.emailUrl.isNotBlank())
                            Text(contact.emailUrl, fontSize = 11.sp, color = LiquidColors.TextMuted)
                    }
                    IconButton(onClick = { onEvent(AddCompanyFormEvent.ContactRemoved(idx)) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, null, tint = LiquidColors.TextSecondary, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
        OutlinedButton(
            onClick = { showContactDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, LiquidColors.Primary.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.PersonAdd, null, tint = LiquidColors.Primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add Contact", color = LiquidColors.Primary)
        }
    }

    if (showContactDialog) {
        AddContactDialog(
            onAdd = { contact ->
                onEvent(AddCompanyFormEvent.ContactAdded(contact))
                showContactDialog = false
            },
            onDismiss = { showContactDialog = false }
        )
    }
}

@Composable
private fun AddContactDialog(onAdd: (PlacementContact) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var linkedin by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LiquidColors.GlassSurface,
        title = { Text("Add Contact", color = LiquidColors.TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DialogTextField("Full Name *", Icons.Default.Person, name, KeyboardType.Text) { name = it }
                DialogTextField("Role (e.g. University Recruiter)", Icons.Default.Work, role, KeyboardType.Text) { role = it }
                DialogTextField("LinkedIn URL", Icons.Default.Link, linkedin, KeyboardType.Uri) { linkedin = it }
                DialogTextField("Email", Icons.Default.Email, email, KeyboardType.Email) { email = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank())
                        onAdd(PlacementContact(name = name, role = role, linkedinUrl = linkedin, emailUrl = email))
                },
                colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = LiquidColors.TextSecondary) } }
    )
}

// ─── Reusable Components ──────────────────────────────────────────────────────

@Composable
private fun StepSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = LiquidColors.Primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
            }
            HorizontalDivider(color = LiquidColors.GlassBorder)
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormField(
    label: String,
    icon: ImageVector,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = LiquidColors.TextSecondary, fontSize = 12.sp) },
        leadingIcon = { Icon(icon, null, tint = LiquidColors.Primary, modifier = Modifier.size(18.dp)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LiquidColors.Primary,
            unfocusedBorderColor = LiquidColors.GlassBorder,
            focusedContainerColor = LiquidColors.GlassSurface.copy(alpha = 0.5f),
            unfocusedContainerColor = LiquidColors.GlassSurface.copy(alpha = 0.3f),
            focusedTextColor = LiquidColors.TextPrimary,
            unfocusedTextColor = LiquidColors.TextPrimary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormTextArea(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = LiquidColors.TextSecondary, fontSize = 12.sp) },
        minLines = 3,
        maxLines = 6,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LiquidColors.Primary,
            unfocusedBorderColor = LiquidColors.GlassBorder,
            focusedContainerColor = LiquidColors.GlassSurface.copy(alpha = 0.5f),
            unfocusedContainerColor = LiquidColors.GlassSurface.copy(alpha = 0.3f),
            focusedTextColor = LiquidColors.TextPrimary,
            unfocusedTextColor = LiquidColors.TextPrimary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogTextField(
    label: String,
    icon: ImageVector,
    value: String,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp, color = LiquidColors.TextSecondary) },
        leadingIcon = { Icon(icon, null, tint = LiquidColors.Primary, modifier = Modifier.size(16.dp)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LiquidColors.Primary,
            unfocusedBorderColor = LiquidColors.GlassBorder,
            focusedTextColor = LiquidColors.TextPrimary,
            unfocusedTextColor = LiquidColors.TextPrimary
        )
    )
}

@Composable
private fun SegmentedControl(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LiquidColors.GlassSurface, RoundedCornerShape(12.dp))
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { i, opt ->
            val isSelected = i == selectedIndex
            val bgColor by animateColorAsState(
                if (isSelected) LiquidColors.Primary else Color.Transparent,
                label = "seg_$i"
            )
            val textColor by animateColorAsState(
                if (isSelected) Color.White else LiquidColors.TextSecondary,
                label = "seg_text_$i"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .clickable { onSelect(i) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(opt, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = textColor)
            }
        }
    }
}

@Composable
private fun BranchChips(selected: List<String>, branches: List<String>, onToggle: (String) -> Unit) {
    val rows = branches.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { branch ->
                    val isSelected = branch in selected
                    val bgColor by animateColorAsState(
                        if (isSelected) LiquidColors.Primary.copy(alpha = 0.2f) else LiquidColors.GlassSurface,
                        label = "branch_$branch"
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(bgColor)
                            .border(
                                1.dp,
                                if (isSelected) LiquidColors.Primary else LiquidColors.GlassBorder,
                                RoundedCornerShape(50.dp)
                            )
                            .clickable { onToggle(branch) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            branch,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) LiquidColors.Primary else LiquidColors.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Info, null, tint = LiquidColors.TextMuted, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 12.sp, color = LiquidColors.TextMuted)
    }
}
