package com.exa.android.reflekt.loopit.presentation.main.profile.feedback

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.EditProfileViewModel
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.model.Profile.ProfileHeaderData
import com.exa.android.reflekt.loopit.util.showToast
import androidx.compose.runtime.derivedStateOf


@Composable
fun ProfileFeedback(userId: String?, editProfileViewModel: EditProfileViewModel, onViewVerifier : () -> Unit) {

    val curUser = remember { editProfileViewModel.curUser }
    val verifierDetailResponse by  editProfileViewModel.userProfiles.collectAsState()
    var verifierDetail by remember { mutableStateOf(emptyList<ProfileHeaderData>()) }
    val rating = remember { editProfileViewModel.curUserRating }

    val context = LocalContext.current

    when (val res = verifierDetailResponse) {
        is Response.Error -> {}
        Response.Loading -> {}
        is Response.Success -> {
            verifierDetail = res.data
        }
    }

    val isVerified by remember(verifierDetail, curUser) {
        derivedStateOf { verifierDetail.any { it.uid == curUser } }
    }

    Column() {
        // Trusted By Section (2 lines max)
        if (verifierDetail.isNotEmpty()) {
            TrustedBySection(
                verifiedBy = verifierDetail.map { it.name },
                onClick = { onViewVerifier() }
            )
        }

        if (userId != null && userId != curUser) {
            Spacer(modifier = Modifier.height(12.dp))

            VerifyAndRating(
                isVerified = isVerified, rating = rating.value,
                onVerifyClick = {
                    editProfileViewModel.verifyProfile(userId)
                    showToast(context, "You have successfully verified this user.")
                },
                onRate = { rating ->
                    editProfileViewModel.updateRating(rating, userId) }
            )
        }
    }
}

// TrustedBySection.kt
@Composable
fun TrustedBySection(
    verifiedBy: List<String>,
    onClick: () -> Unit
) {
    val maxVisibleNames = 2
    val (displayNames, remainingCount) = remember(verifiedBy) {
        val visible = verifiedBy.take(maxVisibleNames)
        val remaining = (verifiedBy.size - visible.size).coerceAtLeast(0)
        Pair(visible, remaining)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            //contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildAnnotatedString {
                        if (verifiedBy.isEmpty()) {
                            append("No verifications yet")
                        } else {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiary
                                )
                            ) {
                                append("Trusted by ")
                            }


                            // Display visible names
                            displayNames.forEachIndexed { index, name ->
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontStyle = FontStyle.Italic
                                    )
                                ) {
                                    append(name)
                                }
                                if (index < displayNames.lastIndex) append(", ")
                            }

                            // Show remaining count if needed
                            if (remainingCount > 0) {
                                append(" & ")
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    append("$remainingCount more")
                                }
                            }
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (verifiedBy.isNotEmpty()) {
                    Text(
                        text = "Tap to see all verifications",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(.7f),
                        modifier = Modifier.padding(top = 4.dp))
                }
            }

            if (verifiedBy.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View all",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp))
            }
        }
    }
}


@Composable
fun VerifyAndRating(
    isVerified: Boolean,
    rating: Int,
    onVerifyClick: () -> Unit,
    onRate: (Int) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiary)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Verify Button
            if (!isVerified) {
                Button(
                    onClick = { onVerifyClick() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verify",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verify Profile", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }


            // Rating Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Show rating dialog */ }
            ) {
                Text(
                    text = "Rate this profile:",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onTertiary
                )
                var currentRating by remember(rating) { mutableIntStateOf(rating) }
                StarRatingBar(rating = rating) { // 0 means not rated yet
                    onRate(it)
                    currentRating = it
                }
            }
        }
    }
}

// StarRatingBar.kt
@Composable
fun StarRatingBar(rating: Int, onRate: (Int) -> Unit) {
    Row {
        for (i in 1..5) {
            Icon(
                imageVector = (if (i <= rating) Icons.Default.Star else Icons.Default.StarOutline),
                contentDescription = "Rating",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onRate(i) }
            )
        }
    }
}
