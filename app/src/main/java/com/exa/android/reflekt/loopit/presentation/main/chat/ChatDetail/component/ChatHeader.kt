package com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.theme.Spacing
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.dialog.BlockUserDialog
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.dialog.DeleteMessageDialog
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.dialog.ReportUserDialog
import com.exa.android.reflekt.loopit.presentation.main.component.ImageUsingCoil
import com.exa.android.reflekt.loopit.core.utils.formatTimestamp
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.Message
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.Status
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.User


@Composable
fun ChatHeader(
    user: User?,
    status: Status?,
    curUser: String,
    selectedMessages: Set<Message>,
    isCurUserBlock: Boolean,
    isOtherUserBlock: Boolean,
    onProfileClick: () -> Unit,
    onBackClick: () -> Unit,
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onUnselectClick: () -> Unit,
    onCopyClick: () -> Unit,
    onEditClick: (Message?) -> Unit,
    onReplyClick: (Message?) -> Unit,
    onForwardClick: () -> Unit,
    onDeleteClick: (Int) -> Unit,
    onClearChatClick: () -> Unit,
    onBlockClick: () -> Unit,
    onReportClick: (reason: String, proofText: String?, proofImageUri: Uri?) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        AnimatedVisibility(
            visible = selectedMessages.isNotEmpty(),
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            HeaderWithOptions(
                curUser = curUser,
                selectedMessages = selectedMessages,
                onUnselectClick = { onUnselectClick() },
                onCopyClick = { onCopyClick() },
                onForwardClick = { onForwardClick() },
                onDeleteClick = { onDeleteClick(it) },
                onReplyClick = { message -> onReplyClick(message) },
                onEditClick = { message ->
                    onEditClick(message)
                }
            )
        }
        AnimatedVisibility(
            visible = selectedMessages.isEmpty(),
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            HeaderWithProfile(
                otherUser = user,
                status = status,
                curUser = curUser,
                isSelected = selectedMessages.isNotEmpty(),
                isCurUserBlock = isCurUserBlock,
                isOtherUserBlock = isOtherUserBlock,
                onBackClick = { onBackClick() },
                onProfileClick = { onProfileClick() },
                onVoiceCallClick = { onVoiceCallClick() },
                onVideoCallClick = { onVideoCallClick() },
                onClearChatClick = { onClearChatClick() },
                onBlockClick = { onBlockClick() },
                onReportClick = onReportClick
            )
        }
    }
}

@Composable
fun HeaderWithOptions(
    curUser: String,
    selectedMessages: Set<Message>,
    onUnselectClick: () -> Unit,
    onCopyClick: () -> Unit,
    onForwardClick: () -> Unit,
    onDeleteClick: (Int) -> Unit,
    onReplyClick: (Message?) -> Unit,
    onEditClick: (Message?) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var showDialog by remember { mutableStateOf(false) }
    val singleMessage = if (selectedMessages.size == 1) selectedMessages.first() else null
    val endIcons = mutableListOf<IconData>()

    val reply = IconData(
        iconType = IconType.PainterIcon(R.drawable.reply_icon),
        contentDescription = "Reply",
        onClick = { onReplyClick(singleMessage) }
    )
    val copy = IconData(
        iconType = IconType.PainterIcon(R.drawable.ic_copy),
        contentDescription = "Copy Selected",
        onClick = { onCopyClick() }
    )
    val delete = IconData(
        iconType = IconType.VectorIcon(Icons.Default.Delete),
        contentDescription = "Delete Selected",
        onClick = { showDialog = true }
    )
    val edit = IconData(
        iconType = IconType.VectorIcon(Icons.Default.Edit),
        contentDescription = "Edit Selected",
        onClick = { onEditClick(singleMessage) }
    )

    getVisibleIcons(selectedMessages, curUser).forEach {
        when (it) {
            IconsName.REPLY -> endIcons.add(reply)
            IconsName.COPY -> endIcons.add(copy)
            IconsName.EDIT -> endIcons.add(edit)
            IconsName.DELETE -> endIcons.add(delete)
            IconsName.FORWARD -> {}
        }
    }

    PlaceIcons(
        selectedMessages = selectedMessages.size,
        startIcons = listOf(
            IconData(
                iconType = IconType.VectorIcon(Icons.AutoMirrored.Filled.ArrowBack),
                contentDescription = "Unselect fields",
                onClick = { onUnselectClick() }
            )
        ),
        endIcons = endIcons
    ) { iconData, index ->
        val rotation = if (iconData.contentDescription == "Forward Selected") 90f else 0f
        ShowIcon(iconData = iconData, rotationAngle = rotation)
    }

    if (showDialog) {
        DeleteMessageDialog(
            checkAllFromCurrentUser(selectedMessages, curUser),
            checkDeleted(selectedMessages),
            onDelete = { deleteOption ->
                if (deleteOption == "Delete for Me") {
                    onDeleteClick(1)
                } else {
                    onDeleteClick(2)
                }
                showDialog = false
            },
            onCancel = {
                showDialog = false
            }
        )
    }
}

@Composable
fun PlaceIcons(
    selectedMessages: Int,
    startIcons: List<IconData>,
    endIcons: List<IconData>,
    iconContent: @Composable (iconData: IconData, index: Int) -> Unit = { iconData, _ ->
        ShowIcon(iconData = iconData)
    }
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            .background(colorScheme.surface)
            .clickable(false) { }
            .padding(vertical = Spacing.extraSmall, horizontal = Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            startIcons.forEachIndexed { index, iconData ->
                iconContent(iconData, index)
            }

            Text(
                text = selectedMessages.toString(),
                color = colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            endIcons.forEachIndexed { index, iconData ->
                iconContent(iconData, index)
            }
        }
    }
}

@Composable
fun ShowIcon(iconData: IconData, rotationAngle: Float = 0f) {
    IconButton(onClick = { iconData.onClick() }) {
        when (val icon = iconData.iconType) {
            is IconType.VectorIcon -> {
                Icon(
                    imageVector = icon.imageVector,
                    contentDescription = iconData.contentDescription,
                    modifier = Modifier
                        .rotate(rotationAngle)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            is IconType.PainterIcon -> {
                Icon(
                    painter = painterResource(id = icon.painter),
                    contentDescription = iconData.contentDescription,
                    modifier = Modifier
                        .rotate(rotationAngle)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun HeaderWithProfile(
    otherUser: User?,
    status: Status? = Status(),
    curUser: String,
    isSelected: Boolean,
    isCurUserBlock: Boolean,
    isOtherUserBlock: Boolean,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onClearChatClick: () -> Unit,
    onBlockClick: () -> Unit,
    onReportClick: (reason: String, proofText: String?, proofImageUri: Uri?) -> Unit
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    val isOnline = status?.isOnline == true
    val isTyping = status?.typingTo == curUser

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.small, vertical = Spacing.extraSmall)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            // Back Button
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = colorScheme.onSurface,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Profile Picture with online ring
            Box(contentAlignment = Alignment.BottomEnd) {
                val borderBrush = if (isOnline) {
                    Brush.linearGradient(listOf(colorScheme.primary, colorScheme.secondary))
                } else {
                    Brush.linearGradient(listOf(colorScheme.outline, colorScheme.outline))
                }
                ImageUsingCoil(
                    context = context,
                    imageUrl = if (isCurUserBlock) "" else otherUser?.profilePicture,
                    placeholder = R.drawable.placeholder,
                    errorImage = R.drawable.placeholder,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(2.dp, borderBrush, CircleShape)
                        .clickable { onProfileClick() }
                )

                // Animated online dot
                if (isOnline && !isCurUserBlock) {
                    val pulseTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseAlpha by pulseTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseAlpha"
                    )
                    Box(
                        modifier = Modifier
                            .offset(x = (-1).dp, y = (-1).dp)
                            .size(12.dp)
                            .graphicsLayer { alpha = pulseAlpha }
                            .clip(CircleShape)
                            .background(colorScheme.secondary)
                            .border(2.dp, colorScheme.surface, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Name and Status
            Column(
                modifier = Modifier
                    .clickable(!isSelected) { onProfileClick() }
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = otherUser?.name ?: "---",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!isCurUserBlock) {
                    val statusContent = when {
                        isTyping -> "typing..."
                        isOnline -> "• Active"
                        status?.lastSeen != null -> {
                            val time = formatTimestamp(status.lastSeen * 1000L)
                            "last active at $time"
                        }
                        else -> "last active"
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isTyping) {
                            TypingDotsIndicator()
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Text(
                            text = statusContent,
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                isTyping -> colorScheme.primary
                                isOnline -> colorScheme.secondary
                                else -> colorScheme.onSurfaceVariant
                            },
                            fontWeight = if (isTyping || isOnline) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }

        val expanded = remember { mutableStateOf(false) }

        // Container for MoreVert Button and Dropdown
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
        ) {
            IconButton(
                onClick = { expanded.value = true },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            var showBlockDialog by remember { mutableStateOf(false) }
            var showReportDialog by remember { mutableStateOf(false) }

            if (showBlockDialog) {
                BlockUserDialog(
                    onDismiss = { showBlockDialog = false },
                    onConfirmBlock = {
                        showBlockDialog = false
                        onBlockClick()
                    }
                )
            }

            if (showReportDialog) {
                ReportUserDialog(
                    onDismiss = { showReportDialog = false },
                    onReportSubmit = { reason, proofText, proofImageUri ->
                        showReportDialog = false
                        onReportClick(reason, proofText, proofImageUri)
                    }
                )
            }

            Box {
                IconButton(onClick = { expanded.value = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false },
                    modifier = Modifier
                        .background(colorScheme.surface)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("Clear Chat") },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, null, tint = colorScheme.error)
                        },
                        onClick = {
                            expanded.value = false
                            onClearChatClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isOtherUserBlock) "Unblock" else "Block") },
                        leadingIcon = {
                            Icon(Icons.Default.Block, null,
                                tint = if (isOtherUserBlock) colorScheme.primary else colorScheme.error)
                        },
                        onClick = {
                            expanded.value = false
                            showBlockDialog = !isOtherUserBlock
                            if (isOtherUserBlock) onBlockClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Report") },
                        leadingIcon = {
                            Icon(Icons.Default.Flag, null, tint = colorScheme.error)
                        },
                        onClick = {
                            expanded.value = false
                            showReportDialog = true
                        }
                    )
                }
            }
        }
    }
}


// Animated 3-dot typing indicator
@Composable
private fun TypingDotsIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typingDots")

    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(3) { index ->
            val offset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, delayMillis = index * 120, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot$index"
            )
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .offset(y = offset.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}


fun getVisibleIcons(selectedMessages: Set<Message>, curUser: String): List<IconsName> {
    if (selectedMessages.isEmpty()) return emptyList()

    val isSingleSelection = selectedMessages.size == 1
    val allFromCurrentUser = checkAllFromCurrentUser(selectedMessages, curUser)
    val anyDeleted = checkDeleted(selectedMessages)

    if (anyDeleted) {
        return listOf(IconsName.DELETE)
    } else if (isSingleSelection) {
        val visibleIcos = listOf(
            IconsName.REPLY,
            IconsName.COPY,
            IconsName.FORWARD,
            IconsName.DELETE,
            IconsName.EDIT
        )
        return if (allFromCurrentUser) visibleIcos else visibleIcos.filter { it != IconsName.EDIT }
    } else {
        return listOf(IconsName.COPY, IconsName.FORWARD, IconsName.DELETE)
    }

}

fun checkAllFromCurrentUser(selectedMessages: Set<Message>, curUser: String): Boolean {
    return selectedMessages.all { it.senderId == curUser }
}

fun checkDeleted(selectedMessages: Set<Message>): Boolean {
    return selectedMessages.any { it.message == "deleted" }
}


sealed class IconType {
    data class VectorIcon(val imageVector: ImageVector) : IconType()
    data class PainterIcon(val painter: Int) : IconType()
}

data class IconData(
    val iconType: IconType,
    val contentDescription: String,
    val onClick: () -> Unit
)

enum class IconsName {
    COPY, DELETE, FORWARD, EDIT, REPLY
}
