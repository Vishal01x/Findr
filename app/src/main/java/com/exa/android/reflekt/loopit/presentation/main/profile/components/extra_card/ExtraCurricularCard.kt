package com.exa.android.reflekt.loopit.presentation.main.profile.components.extra_card

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.remote.main.Repository.BrandfetchViewModel
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.EditProfileViewModel
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.UserViewModel
import com.exa.android.reflekt.loopit.data.remote.main.api.BrandfetchResponse
import com.exa.android.reflekt.loopit.presentation.main.Home.component.ImageUsingCoil
import com.exa.android.reflekt.loopit.presentation.main.profile.components.header.CircularIconCard
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.model.Profile.ExtraActivity


@Composable
fun ExtracurricularCard(
    userId: String?, onAddClick: (ExtraActivity?) -> Unit,
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
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Extracurricular Activities", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiary)
                        if(userId.isNullOrEmpty())
                            CircularIconCard(Icons.Default.Add, { onAddClick(null) })
                    }
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



    @Composable
    fun CodingProfileCard(extraActivity: ExtraActivity, onAddClick: () -> Unit) {
        val context = LocalContext.current
//    val domain = remember(link) { Uri.parse(link).host?.removePrefix("www.") ?: "" }
//    val viewModel: BrandfetchViewModel = hiltViewModel()
//
//    var brandData by remember(domain) { mutableStateOf<BrandfetchResponse?>(null) }
//    var isLoading by remember(domain) { mutableStateOf(false) }
//
//    LaunchedEffect(domain) {
//        if (domain.isNotEmpty()) {
//            isLoading = true
//            try {
//                val result = viewModel.fetchBrandInfoSingle(domain)
//                brandData = result
//            } finally {
//                isLoading = false
//            }
//        }
//    }

        Card(
            modifier = Modifier
                .width(180.dp)
                .wrapContentHeight()
                .clickable { onAddClick() }
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text("Link", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiary, modifier =
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
                            .align(Alignment.CenterHorizontally)
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
                    color = MaterialTheme.colorScheme.onTertiary
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
                    color = MaterialTheme.colorScheme.onTertiary
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
