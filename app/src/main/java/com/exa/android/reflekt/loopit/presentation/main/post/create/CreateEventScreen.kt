package com.exa.android.reflekt.loopit.presentation.main.post.create

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventDetails
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.DiscardDialog
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    postId: String? = null,
    onBack: () -> Unit,
    onPostCreated: () -> Unit,
    onNavigateToEditor: (String) -> Unit = {},
    editorResult: String? = null,
    onEditorResultConsumed: () -> Unit = {},
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Local event form state, initialized from ViewModel state to preserve data across navigation
    var isVirtual    by remember { mutableStateOf(state.eventDetails?.isVirtual ?: false) }
    var location     by remember { mutableStateOf(state.eventDetails?.location ?: "") }
    var locationLink by remember { mutableStateOf(state.eventDetails?.locationLink ?: "") }
    var meetingLink  by remember { mutableStateOf(state.eventDetails?.meetingLink ?: "") }
    var capacity     by remember { mutableIntStateOf(state.eventDetails?.capacity ?: 10) }
    var selectedDate by remember {
        mutableStateOf(state.eventDetails?.eventDate?.let { ts ->
            Calendar.getInstance().apply { timeInMillis = ts.toDate().time }
        })
    }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    var dateDisplay  by remember { mutableStateOf(selectedDate?.let { dateFormatter.format(it.time) } ?: "") }
    var timeDisplay  by remember { mutableStateOf(state.eventDetails?.eventTime ?: "") }

    LaunchedEffect(Unit) {
        viewModel.onEvent(CreatePostEvent.OnPostTypeSelected(PostType.EVENT))
        if (postId != null) viewModel.onEvent(CreatePostEvent.OnLoadPost(postId))
    }

    // When post loads in edit mode, sync local event detail state
    LaunchedEffect(state.eventDetails) {
        state.eventDetails?.let { details ->
            isVirtual = details.isVirtual
            location = details.location ?: ""
            locationLink = details.locationLink ?: ""
            meetingLink = details.meetingLink ?: ""
            capacity = details.capacity ?: 10
            details.eventDate?.let { ts ->
                val cal = Calendar.getInstance().apply { timeInMillis = ts.toDate().time }
                selectedDate = cal
                dateDisplay = dateFormatter.format(cal.time)
            }
            details.eventTime?.let { timeDisplay = it }
        }
    }

    // Sync local event state → ViewModel whenever fields change
    LaunchedEffect(isVirtual, dateDisplay, timeDisplay, location, locationLink, meetingLink, capacity) {
        val cal = selectedDate
        val eventDetails = EventDetails(
            eventDate = cal?.let { Timestamp(Date(it.timeInMillis)) },
            eventTime = timeDisplay.ifBlank { null },
            location = if (!isVirtual) location.ifBlank { null } else null,
            locationLink = if (!isVirtual) locationLink.ifBlank { null } else null,
            isVirtual = isVirtual,
            meetingLink = if (isVirtual) meetingLink.ifBlank { null } else null,
            capacity = if (capacity > 0) capacity else null
        )
        viewModel.onEvent(CreatePostEvent.OnSaveEventDetails(eventDetails))
    }



    LaunchedEffect(state.postCreatedSuccessfully) {
        if (state.postCreatedSuccessfully) onPostCreated()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                CreatePostUiEvent.PopBack -> onBack()
                is CreatePostUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    // Consume description returned from MarkdownEditorScreen
    LaunchedEffect(editorResult) {
        editorResult?.let {
            viewModel.onEvent(CreatePostEvent.OnDescriptionChanged(it))
            onEditorResultConsumed()
        }
    }

    val coverImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onEvent(CreatePostEvent.OnImagesSelected(listOf(it))) }
    }

    // Date picker
    val datePicker = remember {
        val cal = Calendar.getInstance()
        DatePickerDialog(context, { _, y, m, d ->
            val picked = Calendar.getInstance().apply { set(y, m, d) }
            selectedDate = picked
            dateDisplay = dateFormatter.format(picked.time)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }
    }

    // Time picker
    val timePicker = remember {
        val cal = Calendar.getInstance()
        TimePickerDialog(context, { _, h, m ->
            val amPm = if (h < 12) "AM" else "PM"
            val h12 = if (h % 12 == 0) 12 else h % 12
            timeDisplay = "%d:%02d %s".format(h12, m, amPm)
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false)
    }

    BackHandler { viewModel.onEvent(CreatePostEvent.OnBackPressed) }

    androidx.compose.material3.Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
    Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(Color.Transparent)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // ── Top bar ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { viewModel.onEvent(CreatePostEvent.OnBackPressed) }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
                }
                Text(
                    text = if (state.isEditMode) "Edit Event" else "Create Event",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.width(70.dp))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

            val scrollState = rememberScrollState()
            val coroutineScope = rememberCoroutineScope()
            var showValidationErrors by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // ── Physical / Virtual toggle ──────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(LiquidColors.GlassSurface)
                        .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Physical" to false, "Virtual" to true).forEach { (label, virtual) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isVirtual == virtual) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickableWithNoIndication { isVirtual = virtual }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = if (isVirtual == virtual) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // ── Event Topic ────────────────────────────────────────────
                EventFormField(label = "Event Topic") {
                    EventInputField(
                        value = state.title,
                        onValueChange = { 
                            viewModel.onEvent(CreatePostEvent.OnTitleChanged(it))
                            if (showValidationErrors) showValidationErrors = false
                        },
                        placeholder = "e.g. Kotlin Meetup",
                        leadingIcon = Icons.Outlined.Event,
                        isError = showValidationErrors && state.title.isBlank()
                    )
                }

                // ── Description ────────────────────────────────────────────
                EventFormField(label = "Description") {
                    DescriptionTapField(
                        text = state.description,
                        placeholder = "What are we doing? Tap to write…",
                        onTap = { onNavigateToEditor(state.description) }
                    )
                }

                // ── Cover Image ────────────────────────────────────────────
                EventFormField(label = "Cover Image") {
                    val coverUri = state.selectedImages.firstOrNull()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.5.dp,
                                color = if (coverUri != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                coverImageLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (coverUri != null) {
                            coil.compose.AsyncImage(
                                model = coverUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            // Change overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Edit, null,
                                    tint = Color.White, modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.AddPhotoAlternate, null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    "Upload Photo",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                // ── Date & Time ────────────────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        EventFormField(label = "Date") {
                            EventInputField(
                                value = dateDisplay,
                                onValueChange = {},
                                placeholder = "Select date",
                                leadingIcon = Icons.Outlined.CalendarToday,
                                readOnly = true,
                                onClick = { datePicker.show() }
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        EventFormField(label = "Time") {
                            EventInputField(
                                value = timeDisplay,
                                onValueChange = {},
                                placeholder = "Select time",
                                leadingIcon = Icons.Outlined.Schedule,
                                readOnly = true,
                                onClick = { timePicker.show() }
                            )
                        }
                    }
                }

                // ── Location / Meeting link ────────────────────────────────
                AnimatedContent(
                    targetState = isVirtual,
                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(130)) },
                    label = "location_field"
                ) { virtual ->
                    if (!virtual) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            EventFormField(label = "Location") {
                                EventInputField(
                                    value = location,
                                    onValueChange = { location = it },
                                    placeholder = "Add location or address",
                                    leadingIcon = Icons.Outlined.Place
                                )
                            }
                            EventFormField(label = "Location Link (optional)") {
                                EventInputField(
                                    value = locationLink,
                                    onValueChange = { locationLink = it },
                                    placeholder = "https://maps.google.com/…",
                                    leadingIcon = Icons.Outlined.Map
                                )
                            }
                        }
                    } else {
                        EventFormField(label = "Meeting Link") {
                            EventInputField(
                                value = meetingLink,
                                onValueChange = { meetingLink = it },
                                placeholder = "https://meet.google.com/...",
                                leadingIcon = Icons.Outlined.Link
                            )
                        }
                    }
                }

                // ── Max Participants ───────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(LiquidColors.GlassSurface)
                        .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Max Participants",
                            color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, fontSize = 14.sp
                        )
                        Text(
                            "How many people can join?",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Transparent)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (capacity > 1) capacity-- },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(
                                Icons.Outlined.Remove, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            "$capacity",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.width(40.dp),
                            textAlign = TextAlign.Center
                        )
                        IconButton(
                            onClick = { capacity++ },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(
                                Icons.Outlined.Add, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            // ── Post button ────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LiquidColors.GlassSurface)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (state.title.isNotBlank()) {
                            viewModel.onEvent(CreatePostEvent.OnCreatePost)
                        } else {
                            showValidationErrors = true
                            coroutineScope.launch {
                                scrollState.animateScrollTo(0)
                            }
                        }
                    },
                    enabled = !state.isUploading,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    )
                ) {
                    if (state.isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(
                            if (state.isEditMode) "Update Event" else "Post Event",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward, null,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }

    if (state.showDiscardDialog) {
        DiscardDialog(
            title = "Discard event?",
            message = "Are you sure you want to discard this event?",
            onConfirm = { viewModel.onEvent(CreatePostEvent.OnConfirmDiscard) },
            onDismiss = { viewModel.onEvent(CreatePostEvent.OnCancelDiscard) }
        )
    }
    } // end Scaffold
}

// ── Helpers ────────────────────────────────────────────────────────────────

@Composable
private fun EventFormField(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        content()
    }
}

@Composable
private fun EventInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    readOnly: Boolean = false,
    isError: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Wrap in Box so the transparent overlay can intercept taps.
    // OutlinedTextField internally consumes pointer events, so attaching
    // .clickable directly to it never fires for read-only picker fields.
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
            leadingIcon = { Icon(leadingIcon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            readOnly = readOnly || onClick != null,
            isError = isError,
            colors = eventFieldColors(),
            shape = RoundedCornerShape(12.dp),
            textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
        )
        // Transparent tap interceptor — sits on top of the text field
        if (onClick != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickableWithNoIndication(onClick = onClick)
            )
        }
    }
}

@Composable
private fun eventFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
)
