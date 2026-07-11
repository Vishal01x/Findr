package com.exa.android.reflekt.loopit.presentation.main.post.create

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.DiscardDialog
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.post.create.DescriptionTapField

/**
 * Poll creation screen: title, description, and 2-5 poll options.
 * Voting happens entirely inline on the feed card (PostCard) — there is no poll detail screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePollScreen(
    postId: String? = null,
    onBack: () -> Unit,
    onPostCreated: () -> Unit,
    onNavigateToEditor: (String) -> Unit = {},
    editorResult: String? = null,
    onEditorResultConsumed: () -> Unit = {},
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showValidationErrors by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.onEvent(CreatePostEvent.OnPostTypeSelected(PostType.POLL))
        if (postId != null) viewModel.onEvent(CreatePostEvent.OnLoadPost(postId))
    }

    LaunchedEffect(state.postCreatedSuccessfully) {
        if (state.postCreatedSuccessfully) onPostCreated()
    }

    LaunchedEffect(editorResult) {
        editorResult?.let {
            viewModel.onEvent(CreatePostEvent.OnDescriptionChanged(it))
            onEditorResultConsumed()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                CreatePostUiEvent.PopBack -> onBack()
                is CreatePostUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    BackHandler { viewModel.onEvent(CreatePostEvent.OnBackPressed) }

    val validOptionCount = state.pollOptions.count { it.isNotBlank() }

    androidx.compose.material3.Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        containerColor = LiquidColors.BgBase,
        contentColor = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
    Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(LiquidColors.BgBase)) {
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
                IconButton(onClick = { viewModel.onEvent(CreatePostEvent.OnBackPressed) }) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LiquidColors.GlassSurface)
                            .border(1.dp, LiquidColors.GlassBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Close, null, tint = LiquidColors.TextPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                Text(
                    if (state.isEditMode) "Edit Poll" else "Create a Poll",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = LiquidColors.TextPrimary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = {
                        if (state.title.isNotBlank() && validOptionCount >= 2) {
                            viewModel.onEvent(CreatePostEvent.OnCreatePost)
                        } else {
                            showValidationErrors = true
                        }
                    },
                    enabled = !state.isUploading,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    if (state.isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(if (state.isEditMode) "Update Poll" else "Post", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            HorizontalDivider(color = LiquidColors.GlassBorder, thickness = 0.5.dp)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // ── Post as (identity picker) ───────────────────────────
                IdentityPicker(
                    isAnonymous = state.isAnonymous,
                    onSelect = { anon ->
                        if (anon != state.isAnonymous)
                            viewModel.onEvent(CreatePostEvent.OnToggleAnonymous)
                    }
                )

                // ── Title ────────────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Question",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = {
                            viewModel.onEvent(CreatePostEvent.OnTitleChanged(it))
                            if (showValidationErrors) showValidationErrors = false
                        },
                        placeholder = { Text("What do you want to ask?", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Outlined.Poll, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = showValidationErrors && state.title.isBlank(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                    )
                }

                // ── Description (optional context) ─────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Description (optional)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    DescriptionTapField(
                        text = state.description,
                        placeholder = "Add more context…",
                        onTap = { onNavigateToEditor(state.description) }
                    )
                }

                // ── Poll options (2-5) ──────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Options",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "${state.pollOptions.size}/5",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    state.pollOptions.forEachIndexed { index, optionText ->
                        PollOptionField(
                            index = index,
                            value = optionText,
                            onValueChange = { viewModel.onEvent(CreatePostEvent.OnPollOptionChanged(index, it)) },
                            onRemove = { viewModel.onEvent(CreatePostEvent.OnRemovePollOption(index)) },
                            canRemove = state.pollOptions.size > 2,
                            isError = showValidationErrors && validOptionCount < 2 && optionText.isBlank()
                        )
                    }

                    if (state.pollOptions.size < 5) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                .clickableWithNoIndication { viewModel.onEvent(CreatePostEvent.OnAddPollOption) }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.AddCircleOutline, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Add option",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    if (showValidationErrors && validOptionCount < 2) {
                        Text(
                            "Add at least 2 options",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (state.showDiscardDialog) {
        DiscardDialog(
            title = "Discard poll?",
            message = "Are you sure you want to discard this poll?",
            onConfirm = { viewModel.onEvent(CreatePostEvent.OnConfirmDiscard) },
            onDismiss = { viewModel.onEvent(CreatePostEvent.OnCancelDiscard) }
        )
    }
    } // end Scaffold
}

@Composable
private fun PollOptionField(
    index: Int,
    value: String,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean,
    isError: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= 80) onValueChange(it) },
        placeholder = { Text("Option ${index + 1}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = isError,
        trailingIcon = {
            if (canRemove) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Outlined.Close, contentDescription = "Remove option",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(12.dp),
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
    )
}
