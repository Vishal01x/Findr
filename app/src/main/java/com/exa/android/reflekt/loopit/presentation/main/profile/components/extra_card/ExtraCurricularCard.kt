package com.exa.android.reflekt.loopit.presentation.main.profile.components.extra_card

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.viewmodel.UserViewModel
import com.exa.android.reflekt.loopit.presentation.main.component.ImageUsingCoil
import com.exa.android.reflekt.loopit.presentation.main.profile.components.header.CircularIconCard
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ExtraActivity
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors


@Composable
fun ExtracurricularCard(
    userId: String?, curUser : String?, onAddClick: (ExtraActivity?) -> Unit,
    userViewModel: UserViewModel = hiltViewModel(),
    editExtracurricularViewModel: ExtracurricularViewModel = hiltViewModel()
) {
    val activities by userViewModel.userExtraActivity.collectAsState()

    LaunchedEffect(userId) {
        userViewModel.getExtraActivity(userId)
    }
    when (val response = activities) {
        is Response.Error -> {
            Box(
                Modifier
                    .height(180.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally){
                    Text("Error in Loading Extra Activities /n Tap to Retry")
                }

                Spacer(Modifier.height(4.dp))

                Button(onClick = { userViewModel.getExtraActivity(userId) }) {
                    Text("Retry", color = MaterialTheme.colorScheme.secondary)
                }

            }
        }

        Response.Loading -> {
            Box(
                Modifier
                    .height(180.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        is Response.Success -> {
            Card(
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(LiquidColors.GlassSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(12.dp))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Extracurricular Activities", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        if(userId == null || userId == curUser)
                            CircularIconCard(Icons.Default.Add, { onAddClick(null) })
                    }
                    if (response.data.isEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (userId == null || userId == curUser) "Add achievements, coding profiles, clubs, or activities." else "No activities added yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(response.data) {

                                CodingProfileCard(
                                    it, { onAddClick(it) }
                                )
                            }

                            // to add + card

                        }
                    }
                }
            }
        }
    }
}



@Composable
fun CodingProfileCard(extraActivity: ExtraActivity, onAddClick: () -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .width(180.dp)
            .wrapContentHeight()
            .clickable { onAddClick() }
            .padding(8.dp)
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(LiquidColors.GlassSurface)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Link", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, modifier =
            Modifier.clickable { openUrl(context, extraActivity.link) })

            Spacer(Modifier.height(8.dp))

//            if (isLoading) {
//                CircularProgressIndicator(modifier = Modifier.size(40.dp))
//            } else {
            val logoUrl = extraActivity.media
            if (!logoUrl.isNullOrEmpty()) {
                ImageUsingCoil(
                    context = context,
                    imageUrl = logoUrl,
                    placeholder = R.drawable.htmx_ic,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .align(Alignment.CenterHorizontally),
                    errorImage = R.drawable.htmx_ic
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.htmx_ic),
                    contentDescription = "Default icon",
                    modifier = Modifier.size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .align(Alignment.CenterHorizontally),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            //}

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = extraActivity.domain.ifBlank { ".com" },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = extraActivity.name.ifBlank { "No Title" },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = extraActivity.description,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .animateContentSize(),
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
    }
}

//fun openUrl(context: Context, url: String) {
//    try {
//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
//            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//        }
//
//        // Ensure back press returns to your app
//        TaskStackBuilder.create(context).apply {
//            addNextIntentWithParentStack(intent)
//        }.startActivities()
//
//    } catch (e: Exception) {
//        Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
//    }
//}

