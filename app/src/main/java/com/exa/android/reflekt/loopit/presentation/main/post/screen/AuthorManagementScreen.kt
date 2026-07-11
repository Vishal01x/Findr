package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.RoleEnrollment
import com.exa.android.reflekt.loopit.presentation.main.post.components.UserAvatar
import com.exa.android.reflekt.loopit.presentation.main.post.components.AvatarSize

// Adaptive colour palette — matches EventDetailScreen's color system
private object MrColors {
    val Bg: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFF101D22) else Color(0xFFF1F5F9)
    val Surface: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFF16272E) else Color(0xFFFFFFFF)
    val Slate700: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFF334155) else Color(0xFFCBD5E1)
    val TextPrimary: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val TextSl3: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFFCBD5E1) else Color(0xFF475569)
    val TextSl4: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF64748B)
    val Border: Color
        @Composable get() = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
    val BorderSubtle: Color
        @Composable get() = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f)
    val Primary = Color(0xFF13B6EC)
    val Red: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFFFF6B6B) else Color(0xFFDC2626)
    val Green: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFF34D399) else Color(0xFF059669)
}

@Composable
fun AuthorManagementScreen(
    postId: String,
    onBackClick: () -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: AuthorManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(postId) { viewModel.loadEnrollments(postId) }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MrColors.Bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.statusBarsPadding())

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MrColors.TextPrimary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Manage Requests",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MrColors.TextPrimary
                    )
                    Text(
                        "${state.pendingCount} pending applications",
                        style = MaterialTheme.typography.labelSmall,
                        color = MrColors.TextSl4
                    )
                }
                IconButton(onClick = { viewModel.selectTab((state.selectedTab + 1) % 3) }) {
                    Icon(Icons.Rounded.FilterList, "Filter", tint = MrColors.TextSl4)
                }
            }

            HorizontalDivider(color = MrColors.Border)

            val currentList = when (state.selectedTab) {
                0 -> state.pendingEnrollments
                1 -> state.acceptedEnrollments
                else -> state.rejectedEnrollments
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // ── Project context banner ─────────────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MrColors.Surface)
                            .border(1.dp, MrColors.Border, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(MrColors.Primary, Color(0xFF0E7490))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Code,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    "MANAGING PROJECT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MrColors.Primary,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = state.post?.title?.ifBlank { "Unnamed Project" } ?: "Loading…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MrColors.TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // ── Status filter chips ────────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusFilterChip("Pending",  state.selectedTab == 0, state.pendingCount)  { viewModel.selectTab(0) }
                        StatusFilterChip("Accepted", state.selectedTab == 1, state.acceptedCount) { viewModel.selectTab(1) }
                        StatusFilterChip("Rejected", state.selectedTab == 2, state.rejectedCount) { viewModel.selectTab(2) }
                    }
                }

                // ── Loading / Error / Empty ────────────────────────────────────
                when {
                    state.isLoading -> item {
                        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MrColors.Primary)
                        }
                    }
                    state.error != null -> item {
                        Column(
                            Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(state.error ?: "Error", color = MrColors.Red, textAlign = TextAlign.Center)
                            Button(
                                onClick = { viewModel.loadEnrollments(postId) },
                                colors = ButtonDefaults.buttonColors(containerColor = MrColors.Primary)
                            ) { Text("Retry") }
                        }
                    }
                    currentList.isEmpty() -> item {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.Inbox, null, tint = MrColors.TextSl4, modifier = Modifier.size(48.dp))
                            Text(
                                when (state.selectedTab) {
                                    0 -> "No pending requests"
                                    1 -> "No accepted requests"
                                    else -> "No rejected requests"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MrColors.TextSl4
                            )
                        }
                    }
                    else -> {
                        items(currentList) { enrollment ->
                            ApplicantCard(
                                enrollment = enrollment,
                                isProcessing = state.processingIds.contains(enrollment.id),
                                onAccept  = { viewModel.acceptEnrollment(enrollment.id) },
                                onReject  = { viewModel.rejectEnrollment(enrollment.id) },
                                onUserClick = { onUserClick(enrollment.userId) }
                            )
                        }
                        // End of list marker
                        item {
                            Text(
                                "End of Requests",
                                style = MaterialTheme.typography.labelSmall,
                                color = MrColors.TextSl4,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // ── Bottom bar ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MrColors.Bg.copy(alpha = 0.97f))
                .border(width = 1.dp, color = MrColors.Border, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${state.spotsFilled}/${state.spotsTotal} Spots Filled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MrColors.TextSl4
                    )
                    Text(
                        "View Project Details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MrColors.Primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickableWithNoIndication { onBackClick() }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(MrColors.Surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = if (state.spotsTotal > 0) state.spotsFilled.toFloat() / state.spotsTotal else 0f)
                            .background(MrColors.Primary)
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding()
        )
    }
}

// ── Status filter chip ────────────────────────────────────────────────────────
@Composable
private fun StatusFilterChip(label: String, selected: Boolean, count: Int, onClick: () -> Unit) {
    val bg     = if (selected) MrColors.Primary.copy(alpha = 0.15f) else MrColors.Surface
    val border = if (selected) MrColors.Primary.copy(alpha = 0.40f) else MrColors.Border
    val tint   = if (selected) MrColors.Primary else MrColors.TextSl4

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(50))
            .clickableWithNoIndication(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = tint, fontWeight = FontWeight.Medium)
        if (count > 0) {
            Text(
                "$count",
                style = MaterialTheme.typography.labelSmall,
                color = tint.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ── Applicant card ────────────────────────────────────────────────────────────
@Composable
private fun ApplicantCard(
    enrollment: RoleEnrollment,
    isProcessing: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onUserClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MrColors.Surface)
            .border(1.dp, MrColors.Border, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // ── Avatar + Name row ──────────────────────────────────────────────
            Row(
                modifier = Modifier.clickableWithNoIndication(onClick = onUserClick),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, MrColors.Primary.copy(alpha = 0.20f), CircleShape)
                ) {
                    if (!enrollment.userProfilePic.isNullOrBlank()) {
                        AsyncImage(
                            model = enrollment.userProfilePic,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        UserAvatar(
                            imageUrl = null,
                            userName = enrollment.userName,
                            size = AvatarSize.Large,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = enrollment.userName.ifBlank { "Unknown" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MrColors.TextPrimary
                    )
                    Text(
                        text = enrollment.roleName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MrColors.TextSl4
                    )
                }

                // Status badge for non-pending
                if (enrollment.status != EnrollmentStatus.PENDING) {
                    val (badgeColor, badgeLabel) = when (enrollment.status) {
                        EnrollmentStatus.ACCEPTED -> Pair(MrColors.Green, "Accepted")
                        EnrollmentStatus.REJECTED -> Pair(MrColors.Red,   "Rejected")
                        else                      -> Pair(MrColors.TextSl4, "")
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .border(1.dp, badgeColor.copy(alpha = 0.30f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(badgeLabel, style = MaterialTheme.typography.labelSmall, color = badgeColor, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ── Message / Note ─────────────────────────────────────────────────
            if (!enrollment.message.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MrColors.Bg)
                        .border(1.dp, MrColors.BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "\"${enrollment.message}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MrColors.TextSl3,
                        fontStyle = FontStyle.Italic,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // ── Action buttons (only for PENDING) ──────────────────────────────
            if (enrollment.status == EnrollmentStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MrColors.TextSl3),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MrColors.Slate700)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MrColors.TextSl4, strokeWidth = 2.dp)
                        } else {
                            Text("Reject", fontWeight = FontWeight.Medium)
                        }
                    }
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MrColors.Primary)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Accept", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
