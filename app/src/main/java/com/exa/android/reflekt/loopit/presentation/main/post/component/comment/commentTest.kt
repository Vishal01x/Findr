package com.exa.android.reflekt.loopit.presentation.main.post.component.comment
//@file:OptIn(ExperimentalMaterial3Api::class)
// CommentsBottomSheet.kt
// Requires:
// implementation "androidx.compose.material:material:<compose-version>"
// implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:<version>"
// Hilt/other DI not required for this snippet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


// A COMMENT BOTTOM SHEET VIEW IN TESTING


/*
// --- Simple Comment model (replace with your real model) ---
data class Comment(
    val id: String,
    val senderName: String,
    val text: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

// --- Controller to show/hide the sheet from callers ---
class CommentsSheetController internal constructor(
    internal val showImpl: suspend (List<Comment>, String?) -> Unit,
    internal val hideImpl: suspend () -> Unit
) {
    suspend fun show(comments: List<Comment>, title: String? = null) = showImpl(comments, title)
    suspend fun hide() = hideImpl()
}

/** Remember and provide a CommentsSheetController for controlling the sheet externally. */
@Composable
fun rememberCommentsSheetController(): Pair<CommentsSheetController, @Composable (content: @Composable CommentsSheetScope.() -> Unit) -> Unit> {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmValueChange = { true }
    )

    val contentState = remember { mutableStateOf<(@Composable CommentsSheetScope.() -> Unit)?>(null) }
    val sheetTitleState = remember { mutableStateOf<String?>(null) }
    val sheetDataState = remember { mutableStateOf<List<Comment>>(emptyList()) }

    val controller = remember {
        CommentsSheetController(
            showImpl = { comments, title ->
                sheetDataState.value = comments
                sheetTitleState.value = title
                coroutineScope.launch { sheetState.show() }
            },
            hideImpl = {
                coroutineScope.launch { sheetState.hide() }
            }
        )
    }

    val host: @Composable (content: @Composable CommentsSheetScope.() -> Unit) -> Unit = { providedContent ->
        LaunchedEffect(providedContent) {
            contentState.value = providedContent
        }

        ModalBottomSheetLayout(
            sheetState = sheetState,
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            scrimColor = Color.Black.copy(alpha = 0.4f),
            sheetContent = {
                val scopeImpl = object : CommentsSheetScope {
                    override val controllerRef: CommentsSheetController = controller
                    override val coroutineScope: CoroutineScope = coroutineScope
                    override fun dismiss() {
                        coroutineScope.launch { controller.hide() }
                    }
                }

                // Using BoxWithConstraints to determine max height dynamically
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 72.dp)
                ) {
                    val maxHeightDp = maxHeight * 0.85f // clamp to 85% of available space

                    // ✅ AnimatedVisibility moved OUTSIDE Column to avoid scope conflict
                    AnimatedVisibility(
                        visible = sheetState.currentValue != ModalBottomSheetValue.Hidden,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 0.dp, max = maxHeightDp)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            // drag handle
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(vertical = 6.dp)
                                    .size(width = 48.dp, height = 4.dp)
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(Color.Gray.copy(alpha = 0.4f))
                            )

                            // title
                            sheetTitleState.value?.let { title ->
                                Text(
                                    text = title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            // main content
                            Box(modifier = Modifier.weight(1f, fill = false)) {
                                contentState.value?.invoke(scopeImpl)
                                    ?: DefaultCommentsContent(sheetDataState.value)
                            }
                        }
                    }
                }
            }
        ) {
            // overlay placeholder (transparent)
            Box(modifier = Modifier.fillMaxSize())
        }
    }

    return controller to host
}


// A small scope provided to sheet content for dismiss actions
interface CommentsSheetScope {
    val controllerRef: CommentsSheetController
    val coroutineScope: CoroutineScope
    fun dismiss()
}

/** Default content if caller doesn't provide any */
@Composable
private fun DefaultCommentsContent(comments: List<Comment>) {
    if (comments.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No comments yet", style = MaterialTheme.typography.body1)
        }
        return
    }

    // We clamp list to fill available space when many items exist
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        itemsIndexed(comments) { index, comment ->
            CommentRow(comment = comment)
            if (index < comments.lastIndex) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun CommentRow(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // avatar circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colors.onSurface.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = comment.senderName.take(1).uppercase(), style = MaterialTheme.typography.subtitle2)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(comment.senderName, style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.width(8.dp))
                // show relative time short — replace with your util if needed
                Text(text = " · ${timeAgoShort(comment.timestampMillis)}", style = MaterialTheme.typography.caption)
            }

            Spacer(Modifier.height(4.dp))
            Text(text = comment.text, style = MaterialTheme.typography.body2)
        }
    }
}

// small helper (very simple)
// In production replace with your time-ago util that respects locale
private fun timeAgoShort(ts: Long): String {
    val sec = (System.currentTimeMillis() - ts) / 1000
    return when {
        sec < 60 -> "${sec}s"
        sec < 3600 -> "${sec / 60}m"
        sec < 86400 -> "${sec / 3600}h"
        else -> "${sec / 86400}d"
    }
}

// ----------------- Example usage in a screen -----------------

/**
 * Example composable showing how to use the host + controller.
 * Place CommentsSheetHost() near top-level of your screen composable tree so the sheet overlays content.
 */
@Composable
fun PostWithCommentsExample() {
    // get controller + host
    val (controller, host) = rememberCommentsSheetController()
    val coroutineScope = rememberCoroutineScope()

    // Place the host in the UI (should be high in the hierarchy so scrim covers full screen)
    host {
        // Caller content inside sheet: get scope from CommentsSheetScope (this lambda receives it)
        // We'll provide a small comments list UI with input
        Column(modifier = Modifier.fillMaxSize()) {
            // Note: This is called only inside sheet logic (host). For this example, we keep it simple.
        }
    }

    // Underlying screen
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Post content here", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            // simulate opening comments with sample list
            val sample = List(18) {
                Comment(id = "c$it", senderName = "User $it", text = "This is comment #$it")
            }
            coroutineScope.launch {
                controller.show(sample, title = "Comments")
            }
        }) {
            Text("Open Comments")
        }
    }
}


*/


/*




import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/*---------------------------------------------------------
    Data Model (Mock)
 ---------------------------------------------------------*/
data class Comment(
    val id: String,
    val userName: String,
    val userId: String,
    val text: String,
    val timestamp: Long
)

/*---------------------------------------------------------
    Mock data generator
 ---------------------------------------------------------*/
fun mockComments(): List<Comment> = List(10) {
    Comment(
        id = it.toString(),
        userName = "User $it",
        userId = "user_$it",
        text = "This is a mock comment number $it. Just testing UI layout!",
        timestamp = System.currentTimeMillis() - it * 60_000L
    )
}

/*---------------------------------------------------------
    Single Comment Item
 ---------------------------------------------------------*/
@Composable
fun CommentItemUI(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                comment.userName.first().uppercase(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.userName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = comment.timestamp.asTimeAgo(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/*---------------------------------------------------------
    Input Field (Send Comment)
 ---------------------------------------------------------*/
@Composable
fun CommentInputBar(
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Add a comment…") },
            singleLine = false,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 40.dp, max = 120.dp),
            shape = RoundedCornerShape(24.dp)
        )
        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSend(text)
                    text = ""
                }
            },
            enabled = text.isNotBlank()
        ) {
            Icon(Icons.Default.Send, contentDescription = "Send")
        }
    }
}

/*---------------------------------------------------------
    Bottom Sheet Content
 ---------------------------------------------------------*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsSheetUI(
    comments: List<Comment>,
    onSendComment: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val maxVisibleHeight = when {
        comments.size <= 2 -> 0.35f
        comments.size <= 5 -> 0.55f
        else -> 0.85f
    }

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        modifier = Modifier.fillMaxHeight(maxVisibleHeight)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Comments (${comments.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Divider(thickness = 0.5.dp)

            // Comments list
            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(vertical = 8.dp)
            ) {
                items(comments) { comment ->
                    CommentItemUI(comment)
                    Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
            }

            // Input bar
            CommentInputBar(onSend = onSendComment)
        }
    }
}

/*---------------------------------------------------------
    Host Example with FAB to Open Sheet
 ---------------------------------------------------------*/
@Composable
fun CommentsSheetDemo() {
    val scope = rememberCoroutineScope()
    val sheetVisible = remember { mutableStateOf(false) }
    val comments = remember { mutableStateListOf<Comment>().apply { addAll(mockComments()) } }

    Box(Modifier.fillMaxSize()) {
        // FAB to open comments
        FloatingActionButton(
            onClick = { sheetVisible.value = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        ) {
            Text("💬")
        }

        AnimatedVisibility(sheetVisible.value) {
            CommentsSheetUI(
                comments = comments,
                onSendComment = { new ->
                    // ✅ In future connect to Firestore -> repository.addComment(postId, new)
                    comments.add(
                        0,
                        Comment(
                            id = UUID.randomUUID().toString(),
                            userName = "You",
                            userId = "currentUser",
                            text = new,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                },
                onClose = { sheetVisible.value = false }
            )
        }
    }
}

/*---------------------------------------------------------
    Helpers
 ---------------------------------------------------------*/
fun Long.asTimeAgo(): String {
    val diff = System.currentTimeMillis() - this
    val mins = diff / 60000
    return when {
        mins < 1 -> "Just now"
        mins < 60 -> "$mins min ago"
        mins < 1440 -> "${mins / 60} hr ago"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(this))
    }
}

/*---------------------------------------------------------
    Preview
 ---------------------------------------------------------*/
@Preview(showBackground = true)
@Composable
fun PreviewCommentsSheet() {
    MaterialTheme {
        CommentsSheetDemo()
    }
}
*/