package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.compose.foundation.background
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.profileUser
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortlistedUsersScreen(
    onBackClick: () -> Unit,
    onUserClick: (String) -> Unit = {},
    viewModel: ShortlistedUsersViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val heading = when (viewModel.mode) {
        "event" -> "Registered Participants"
        else -> "Collaborators"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(heading, fontWeight = FontWeight.Bold)
                        if (viewModel.title.isNotBlank()) {
                            Text(
                                viewModel.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = LiquidColors.TextSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LiquidColors.BgBase
                )
            )
        },
        containerColor = LiquidColors.BgBase
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LiquidColors.Primary)
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error ?: "Error loading users", color = LiquidColors.TextSecondary)
                }
            }
            state.users.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No members yet", color = LiquidColors.TextSecondary)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(state.users) { (user, label) ->
                        ShortlistedUserCard(
                            user = user,
                            label = label,
                            onClick = { if (user.uid.isNotBlank()) onUserClick(user.uid) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortlistedUserCard(
    user: profileUser,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickableWithNoIndication(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = LiquidColors.GlassSurface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (user.profilePicture.isNotBlank()) {
                AsyncImage(
                    model = user.profilePicture,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(LiquidColors.Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = LiquidColors.Primary
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.name,
                    fontWeight = FontWeight.SemiBold,
                    color = LiquidColors.TextPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!user.headline.isNullOrBlank()) {
                    Text(
                        user.headline,
                        color = LiquidColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = LiquidColors.Primary.copy(alpha = 0.12f)
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = LiquidColors.Primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
