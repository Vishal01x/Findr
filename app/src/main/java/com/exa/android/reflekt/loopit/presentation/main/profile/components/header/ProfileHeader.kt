package com.exa.android.reflekt.loopit.presentation.main.profile.components.header

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.CallToAction
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.image.openImageIntent
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.EditProfileViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.component.ImageUsingCoil
import com.exa.android.reflekt.loopit.presentation.main.profile.components.extra_card.openUrl
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.model.Profile.ProfileHeaderData

@Composable
fun ProfileHeader(
    userId: String?,
    curUser: String?,
    profileHeaderData: ProfileHeaderData,
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
        profileHeaderData, profileViews, rating) {
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

        ImageUsingCoil(
            context, userProfileHeader.bannerImageUrl, R.drawable.placeholder,
            Modifier
                .height(150.dp)
                .fillMaxWidth()
                .clickable(userProfileHeader.bannerImageUrl.isNotEmpty()) {
                    openImage(userProfileHeader.bannerImageUrl)
//                    openImageIntent(
//                        context,
//                        userProfileHeader.bannerImageUrl
//                    )
                }
        )

        ImageUsingCoil(
            context,
            userProfileHeader.profileImageUrl,
            R.drawable.placeholder,
            Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
                .size(120.dp)
                .offset(x = 4.dp, y = 60.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .clickable(userProfileHeader.profileImageUrl.isNotEmpty()) {
//                    openImageIntent(
//                        context,
//                        userProfileHeader.profileImageUrl
//                    )
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
    profileViews: Int,
    rating: Response<Pair<Int, Float>>,
    openChat: () -> Unit
) {
    val context = LocalContext.current

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
                    color = Color.Black
                ),
                modifier = Modifier.padding(top = 55.dp)
            )
            if (rating is Response.Success) {
                RatingChip(ratingValue, Modifier.padding(top = 65.dp))
            }

        }
        // Bio
        Text(
            text = userProfileHeader.headline.ifBlank {
                if (isCurUser) "Write a catchy headline about your role/goal " +
                        "e.g. Software Developer @ xyz | CP rating | Proficient in xyz technology" else ""
            }
                ?: "Co-founder, Apna College | Ex-Microsoft | Google SPS'20",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.Black,
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
                        imageVector = Icons.Default.Chat,
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
                modifier = Modifier.clickable(!userProfileHeader.socialLinks.portfolio.isNullOrEmpty()) {
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
fun CircularIconCardPainter(
    iconPainter: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiary),
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
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiary),
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
                tint = MaterialTheme.colorScheme.onTertiary
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

    Log.d("ProfileScreen", "$fullStars $hasHalfStar, $rating")

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
                imageVector = Icons.Filled.StarHalf,
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
fun Profile() {
    /*Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Banner with Profile Photo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            // Banner Image
            Image(
                painter = painterResource(R.drawable.chat_img3), // Replace with your banner
                contentDescription = "Profile banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            // Profile Photo
            Image(
                painter = painterResource(R.drawable.chat_img3), // Replace with your image
                contentDescription = "Profile photo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .align(Alignment.BottomCenter)
                    .offset(y = 30.dp) // Half overlaps the banner
            )
        }

        Spacer(modifier = Modifier.height(40.dp)) // Space for profile photo overlap

        // Name and Rating
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "John Doe",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Star Rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "5.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Headline/Bio
        Text(
            text = "Android Developer | Building awesome apps with Jetpack Compose",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Social Links and Chat Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            IconButton(onClick = { /* LinkedIn action */ }) {
                Icon(
                    imageVector = Icons.Filled.Person, // Replace with LinkedIn icon
                    contentDescription = "LinkedIn",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = { /* YouTube action */ }) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow, // Replace with YouTube icon
                    contentDescription = "YouTube",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = { /* Email action */ }) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = "Email",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { /* Chat action */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Chat,
                    contentDescription = "Chat",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chat")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Experienced Android developer with 5+ years of experience...", // Add your bio
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
        }
    }*/
}
