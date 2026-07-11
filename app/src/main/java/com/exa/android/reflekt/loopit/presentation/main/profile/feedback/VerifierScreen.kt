package com.exa.android.reflekt.loopit.presentation.main.profile.feedback

import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.feature.main.profile.data.viewmodel.EditProfileViewModel
import com.exa.android.reflekt.loopit.presentation.main.component.ImageUsingCoil
import com.exa.android.reflekt.loopit.presentation.main.component.showLoader
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ProfileHeaderData
import com.exa.android.reflekt.loopit.core.utils.showToast
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidMeshBackground
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.background


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifierScreen(
    isVerifier : Boolean,
    userId: String?,
    onProfileClick: (String) -> Unit,
    onBackClick: () -> Unit,
    editProfileViewModel: EditProfileViewModel = hiltViewModel()
) {
    val usersResponse by editProfileViewModel.userProfiles.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        if(isVerifier)
        editProfileViewModel.getAllVerifiersDetail(userId)
        else editProfileViewModel.getAllViewersDetail(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if(isVerifier)"Verifier Users" else "Profile Viewers",
                        style = MaterialTheme.typography.titleLarge,
                        color = LiquidColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = LiquidColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LiquidColors.GlassSurface
                )
            )
        },
        containerColor = LiquidColors.BgBase,
        contentColor = LiquidColors.TextPrimary
    ) { paddingValues ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {

        val error = if(isVerifier)"Verifiers" else "Viewers"

        when (val response = usersResponse) {
            is Response.Loading -> {
                showLoader(message = if(isVerifier)"Verifier Loading..." else "Viewers Loading...")
            }

            is Response.Error -> {
                showToast(context, "Error in loading $error")
            }

            is Response.Success -> {
                val users = response.data
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    items(users) { user ->
                        UserProfileItem(
                            user = user,
                            onClick = {
                                // Navigate to user profile
                                onProfileClick(user.uid)
                            }
                        )

                        if (users.last() != user) {
                            Divider(
                                modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
fun UserProfileItem(
    user: ProfileHeaderData,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(LiquidColors.GlassSurface)
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(16.dp))
            .clickableWithNoIndication(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Image
        ImageUsingCoil(
            context, user.profileImageUrl, R.drawable.placeholder,
            Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Name
            Text(
                text = user.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
                color = LiquidColors.TextPrimary
            )

            // Headline
            Text(
                text = user.headline,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                fontWeight = FontWeight.Medium,
                overflow = TextOverflow.Ellipsis,
                color = LiquidColors.TextSecondary
            )
        }
    }
}

