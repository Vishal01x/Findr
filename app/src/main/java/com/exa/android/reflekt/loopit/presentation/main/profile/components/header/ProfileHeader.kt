package com.exa.android.reflekt.loopit.presentation.main.profile.components.header

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.feature.main.profile.data.viewmodel.EditProfileViewModel
import com.exa.android.reflekt.loopit.presentation.main.component.ImageUsingCoil
import com.exa.android.reflekt.loopit.presentation.main.post.components.UserAvatar
import com.exa.android.reflekt.loopit.presentation.main.post.components.AvatarSize
import com.exa.android.reflekt.loopit.presentation.main.profile.components.extra_card.openUrl
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.CollegeInfo
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ProfileHeaderData
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors

@Composable
fun ProfileHeader(
    userId: String?,
    curUser: String?,
    profileHeaderData: ProfileHeaderData,
    collegeInfo: CollegeInfo,
    openChat: () -> Unit,
    editProfileViewModel: EditProfileViewModel
) {
    val rating by editProfileViewModel.rating.collectAsState()

    val profileViews by editProfileViewModel.profileViews.observeAsState(0)

    // Call the ViewModel to fetch the profile views
    LaunchedEffect(userId) {
        editProfileViewModel.getProfileView(userId)
    }

    ProfileContent(userId == null || userId == curUser,
        profileHeaderData, collegeInfo, profileViews, rating) {
        openChat()
    }
}


@Composable
fun ImageHeader(
    isCurUser: Boolean,
    userProfileHeader: ProfileHeaderData,
    onEditClick: () -> Unit,
    openImage: (String) -> Unit
) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxWidth()) {
        // Banner Image

        if (userProfileHeader.bannerImageUrl.isNotEmpty()) {
            coil.compose.SubcomposeAsyncImage(
                model = coil.request.ImageRequest.Builder(LocalContext.current)
                    .data(userProfileHeader.bannerImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
                    .clickableWithNoIndication { openImage(userProfileHeader.bannerImageUrl) },
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                loading = { ProfileBannerPlaceholder() },
                error = { ProfileBannerPlaceholder() }
            )
        } else {
            ProfileBannerPlaceholder()
        }

        UserAvatar(
            imageUrl = userProfileHeader.profileImageUrl.takeIf { it.isNotBlank() },
            userName = userProfileHeader.name,
            size = AvatarSize.Profile,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
                .offset(x = 4.dp, y = 60.dp)
                .border(2.dp, Color.White, CircleShape)
                .clickableWithNoIndication(enabled = userProfileHeader.profileImageUrl.isNotEmpty()) {
                    openImage(userProfileHeader.profileImageUrl)
                }
        )
        if (isCurUser) {
            CircularIconCard(
                Icons.Default.Edit,
                {
                    onEditClick()
                },
                Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomEnd)
                    .offset(y = 60.dp)
            )
        }


    }
}

@Composable
private fun ProfileContent(
    isCurUser: Boolean,
    userProfileHeader: ProfileHeaderData,
    collegeInfo: CollegeInfo,
    profileViews: Int,
    rating: Response<Pair<Int, Float>>,
    openChat: () -> Unit
) {
    val context = LocalContext.current
    val educationSummary = remember(collegeInfo) { collegeInfo.toHeaderEducationSummary() }

    // Handle different rating states
    val ratingValue = when (rating) {
        is Response.Success -> rating.data // Get the rating value
        is Response.Error -> Pair(0, 0f) // Default to 0 if there's an error
        else -> Pair(0, 0f) // Default to 0 if it's loading
    }


    Column() {
        // Name and Handle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = userProfileHeader.name ?: "Name",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(top = 55.dp)
            )
            if (rating is Response.Success) {
                RatingChip(ratingValue, Modifier.padding(top = 65.dp))
            }

        }
        if (educationSummary.isNotBlank()) {
            EducationSummaryPill(
                text = educationSummary,
                modifier = Modifier.padding(top = 4.dp, start = 2.dp, end = 8.dp)
            )
        }
        // Bio
        Text(
            text = userProfileHeader.headline.ifBlank {
                if (isCurUser) "Write a catchy headline about your role/goal " +
                        "e.g. Software Developer @ xyz | CP rating | Proficient in xyz technology" else ""
            }
                ?: "Co-founder, Apna College | Ex-Microsoft | Google SPS'20",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = MaterialTheme.typography.bodyLarge.fontSize * 1.1 // smaller than default
            ),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp, start = 2.dp)
        )

        // Social Icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //if (!userProfileHeader.socialLinks.youtube.isNullOrEmpty()) {
            CircularIconCardPainter(iconPainter = R.drawable.github, onClick = {
                userProfileHeader.socialLinks.youtube?.let { openUrl(context, it) }
            })
            // }
            //if (!userProfileHeader.socialLinks.linkedin.isNullOrEmpty()) {
            CircularIconCardPainter(
                iconPainter = R.drawable.linedin,
                onClick = {
                    userProfileHeader.socialLinks.linkedin?.let { openUrl(context, it) }
                }
            )
            //}
            //if (!userProfileHeader.socialLinks.email.isNullOrEmpty()) {
            CircularIconCard(icon = Icons.Default.Email, onClick = {
                userProfileHeader.socialLinks.email?.let {
                    if (it.isNotEmpty())
                        openUrl(context, "mailto:$it")
                }
            })
            //}


            if (!isCurUser) {

                Spacer(Modifier.weight(1f))

                // Chat Button
                FilledTonalButton(
                    onClick = { openChat() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Chat"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Chat")
                }
            } else if(profileViews > 0) {
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .padding(end = 2.dp),
                    verticalAlignment = Alignment.CenterVertically // ensures vertical alignment of the icon and text
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveRedEye,
                        contentDescription = "seen",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.width(4.dp)) // space between icon and number

                    Text(
                        text = profileViews.toString(),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Website and Join Date
//        if (!userProfileHeader.socialLinks.portfolio.isNullOrEmpty()) {
        Row(
            modifier = Modifier
                .padding(top = 6.dp, start = 4.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Portfolio.in",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF4285F4),
                    fontSize = 18.sp,
                    fontStyle = FontStyle.Italic
                ),
                modifier = Modifier.clickableWithNoIndication(enabled = !userProfileHeader.socialLinks.portfolio.isNullOrEmpty()) {
                    openUrl(context, userProfileHeader.socialLinks.portfolio!!)
                }
            )
//            Text(
//                text = "Joined February 2020",
//                style = MaterialTheme.typography.bodyMedium.copy(
//                    color = Color.Gray
//                ),
//                modifier = Modifier.padding(start = 16.dp)
//            )
        }
    }
}

@Composable
private fun EducationSummaryPill(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.widthIn(max = 320.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun CollegeInfo.toHeaderEducationSummary(): String {
    val hasEducationDetails = listOf(
        instituteName,
        endDate,
        stream,
        branch,
        classWithSection
    ).any { it.isNotBlank() }

    if (!hasEducationDetails) return ""

    val collegeShortName = instituteName.toCollegeInitials()
    val classYear = endDate.toClassYear()
    val classSection = classWithSection.ifBlank {
        branch.ifBlank { stream }
    }.toClassSectionLabel()

    return listOfNotNull(
        collegeShortName.takeIf { it.isNotBlank() },
        classYear.takeIf { it.isNotBlank() }?.let { "Class of '$it" },
        classSection.takeIf { it.isNotBlank() }
    ).joinToString(" | ")
}

private fun String.toCollegeInitials(): String {
    val ignoredWords = setOf("and", "of", "the", "for", "in", "at")
    return trim()
        .split(Regex("\\s+"))
        .filter { word -> word.isNotBlank() && word.lowercase() !in ignoredWords }
        .mapNotNull { word -> word.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar() }
        .joinToString("")
        .ifBlank { trim().take(5).uppercase() }
}

private fun String.toClassYear(): String {
    val fourDigitYear = Regex("(20\\d{2}|19\\d{2})").find(this)?.value
    if (fourDigitYear != null) return fourDigitYear.takeLast(2)

    val twoDigitYear = Regex("'?(\\d{2})\\b").find(this)?.groupValues?.getOrNull(1)
    return twoDigitYear.orEmpty()
}

private fun String.toClassSectionLabel(): String {
    return trim()
        .replace(Regex("\\s*-\\s*"), " - ")
        .replace(Regex("\\s+"), " ")
}

@Composable
fun CircularIconCardPainter(
    iconPainter: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier.size(40.dp)
    ) {
        IconButton(
            onClick = {
                onClick()
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Image(painter = painterResource(iconPainter), contentDescription = "")
        }
    }
}


@Composable
fun CircularIconCard(
    icon: ImageVector,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier.size(40.dp)
    ) {
        IconButton(
            onClick = {

                if (onClick != null) {
                    onClick()
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun RatingChip(
    rating: Pair<Int, Float>,  // rating = Pair(ratings list, average rating)
    modifier: Modifier = Modifier
) {
    val fullStars = rating.second.toInt()
    val hasHalfStar = (rating.second - fullStars) > 0.0
    val emptyStars = 5 - fullStars - if (hasHalfStar) 1 else 0
    val numUsersRated = rating.first  // The number of users who rated

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // Display full stars
        repeat(fullStars) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFFD700), // Gold
                modifier = Modifier.size(20.dp)
            )
        }

        // Display half star if applicable
        if (hasHalfStar) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.StarHalf,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(20.dp)
            )
        }

        if (numUsersRated > 0) {
            Text(
                text = "($numUsersRated)",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
fun ProfileBannerPlaceholder() {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val surface = if (isDark) Color(0xFF16262C) else Color(0xFFFFFFFF)
    val bgBase = if (isDark) Color(0xFF101D22) else Color(0xFFF1F5F9)
    val dotColor = if (isDark) Color(0x2613B6EC) else Color(0x3513B6EC)
    val primary = LiquidColors.Primary
    val icon = Icons.Rounded.Wallpaper

    Box(modifier = Modifier
        .height(150.dp)
        .fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(surface, bgBase)))
        )
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val spacing = 18.dp.toPx()
            val radius = 1.dp.toPx()
            val cols = (size.width / spacing).toInt() + 2
            val rows = (size.height / spacing).toInt() + 2
            repeat(cols) { col ->
                repeat(rows) { row ->
                    drawCircle(
                        color = dotColor,
                        radius = radius,
                        center = androidx.compose.ui.geometry.Offset(col * spacing, row * spacing)
                    )
                }
            }
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = primary.copy(alpha = 0.07f),
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 14.dp, y = 14.dp)
                .rotate(-15f)
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(56.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(primary, androidx.compose.ui.graphics.Color(0xFF67E8F9))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
