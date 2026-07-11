package com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MessageRequest
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MessageRequest.Companion.MAX_REQUEST_MESSAGES
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MessageRequest.Companion.STATUS_ACCEPTED
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MessageRequest.Companion.STATUS_NONE
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MessageRequest.Companion.STATUS_PENDING
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MessageRequest.Companion.STATUS_REJECTED
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors

private val Amber = Color(0xFFF59E0B)
private val AmberLight = Color(0xFFFEF3C7)
private val AmberDark = Color(0xFF78350F)
private val RedLight = Color(0xFFFEE2E2)
private val RedDark = Color(0xFF991B1B)
private val GreenLight = Color(0xFFD1FAE5)
private val GreenDark = Color(0xFF065F46)

// ── What the SENDER sees ─────────────────────────────────────────────────────

/**
 * Shown at the bottom of chat (above the input) while the sender still has
 * messages remaining before the request is auto-sent.
 */
@Composable
fun SenderRemainingHint(messagesUsed: Int, modifier: Modifier = Modifier) {
    val remaining = MAX_REQUEST_MESSAGES - messagesUsed
    if (remaining <= 0 || remaining >= MAX_REQUEST_MESSAGES) return

    val isDark = LiquidColors.isDark
    val bg = if (isDark) Color(0xFF1E2D20) else Color(0xFFF0FDF4)
    val border = if (isDark) Color(0xFF16A34A).copy(alpha = 0.4f) else Color(0xFF16A34A).copy(alpha = 0.25f)
    val textColor = if (isDark) Color(0xFF86EFAC) else Color(0xFF15803D)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.Info,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "$remaining message${if (remaining == 1) "" else "s"} remaining before a request is sent",
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Replaces the input bar when the sender has used all 3 messages and is
 * waiting for the receiver to respond.
 */
@Composable
fun SenderPendingBar(receiverName: String, modifier: Modifier = Modifier) {
    val isDark = LiquidColors.isDark
    val bg = if (isDark) Color(0xFF1A1A10) else AmberLight
    val border = if (isDark) Amber.copy(alpha = 0.4f) else Amber.copy(alpha = 0.35f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            Icons.Outlined.Schedule,
            contentDescription = null,
            tint = Amber,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = "Message request sent",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Amber else AmberDark
        )
        Text(
            text = "Waiting for $receiverName to accept. You can't send more messages until they respond.",
            style = MaterialTheme.typography.bodySmall,
            color = if (isDark) Amber.copy(alpha = 0.7f) else AmberDark.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

/**
 * Replaces the input bar when the sender's request was rejected.
 */
@Composable
fun SenderRejectedBar(receiverName: String, modifier: Modifier = Modifier) {
    val isDark = LiquidColors.isDark
    val bg = if (isDark) Color(0xFF1A0E0E) else RedLight
    val border = if (isDark) Color(0xFFEF4444).copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.3f)
    val textColor = if (isDark) Color(0xFFFCA5A5) else RedDark

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            Icons.Outlined.Block,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = "Request declined",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "$receiverName has declined your message request. You cannot send them messages.",
            style = MaterialTheme.typography.bodySmall,
            color = textColor.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

// ── What the RECEIVER sees ────────────────────────────────────────────────────

/**
 * Card shown at the TOP of the receiver's chat before they respond to the request.
 * Only the first 3 messages are visible; this card prompts Accept or Decline.
 */
@Composable
fun ReceiverRequestCard(
    senderName: String,
    senderProfileUrl: String?,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LiquidColors.isDark
    val primary = LiquidColors.Primary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF0D1F2D) else Color(0xFFEFF9FF)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, primary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon + title row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.ModeComment,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Message Request",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = primary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "$senderName wants to connect",
                        style = MaterialTheme.typography.bodySmall,
                        color = LiquidColors.TextSecondary
                    )
                }
            }

            HorizontalDivider(color = primary.copy(alpha = 0.15f))

            Text(
                text = "You can see their message${if (MAX_REQUEST_MESSAGES == 1) "" else "s"} above. Accept to continue the conversation or decline to remove this request.",
                style = MaterialTheme.typography.bodySmall,
                color = LiquidColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                ) {
                    Icon(Icons.Rounded.Close, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Decline", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Accept", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}

/**
 * Shown in the receiver's input area after they have declined the request.
 * Provides an "Allow Messages" button to re-enable.
 */
@Composable
fun ReceiverDeclinedBar(
    senderName: String,
    onAllowMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LiquidColors.isDark
    val bg = if (isDark) Color(0xFF16151A) else Color(0xFFF5F3FF)
    val border = if (isDark) Color(0xFF8B5CF6).copy(alpha = 0.35f) else Color(0xFF8B5CF6).copy(alpha = 0.25f)
    val textColor = if (isDark) Color(0xFFC4B5FD) else Color(0xFF5B21B6)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "You declined this request",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "$senderName cannot message you. Tap below to allow messages.",
            style = MaterialTheme.typography.bodySmall,
            color = textColor.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        OutlinedButton(
            onClick = onAllowMessages,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7C3AED)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.6f))
        ) {
            Icon(Icons.Outlined.Lock, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Allow Messages", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}

// ── Chat input bottom bar state selector ─────────────────────────────────────

enum class RequestInputState {
    NORMAL,           // no restriction
    SENDER_HINT,      // sender: 1-2 msgs used, show hint above input
    SENDER_PENDING,   // sender: 3 msgs sent, waiting approval
    SENDER_REJECTED,  // sender: request was declined
    RECEIVER_PENDING, // receiver: needs to accept/decline
    RECEIVER_DECLINED // receiver: declined, shows re-enable option
}

fun resolveRequestInputState(
    request: MessageRequest,
    currentUserId: String
): RequestInputState {
    val isSender = request.senderId == currentUserId
    return when (request.status) {
        STATUS_ACCEPTED -> RequestInputState.NORMAL
        STATUS_NONE -> {
            if (isSender && request.senderMessageCount in 1 until MAX_REQUEST_MESSAGES) {
                RequestInputState.SENDER_HINT
            } else {
                RequestInputState.NORMAL
            }
        }
        STATUS_PENDING -> {
            if (isSender) RequestInputState.SENDER_PENDING
            else RequestInputState.RECEIVER_PENDING
        }
        STATUS_REJECTED -> {
            if (isSender) RequestInputState.SENDER_REJECTED
            else RequestInputState.RECEIVER_DECLINED
        }
        else -> RequestInputState.NORMAL
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun SenderRemainingHintPreview() {
    MaterialTheme {
        SenderRemainingHint(messagesUsed = 1)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun SenderPendingBarPreview() {
    MaterialTheme {
        SenderPendingBar(receiverName = "Alex")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun SenderRejectedBarPreview() {
    MaterialTheme {
        SenderRejectedBar(receiverName = "Alex")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun ReceiverRequestCardPreview() {
    MaterialTheme {
        ReceiverRequestCard(
            senderName = "Jamie",
            senderProfileUrl = null,
            onAccept = {},
            onDecline = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun ReceiverDeclinedBarPreview() {
    MaterialTheme {
        ReceiverDeclinedBar(
            senderName = "Jamie",
            onAllowMessages = {}
        )
    }
}
