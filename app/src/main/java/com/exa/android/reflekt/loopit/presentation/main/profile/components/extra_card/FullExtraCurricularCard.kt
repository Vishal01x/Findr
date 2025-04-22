package com.exa.android.reflekt.loopit.presentation.main.profile.components.extra_card

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.presentation.main.Home.component.ImageUsingCoil
import com.exa.android.reflekt.loopit.util.model.Profile.ExtraActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullExtraCardScreen(
    navController: NavController,
    isCurUser : Boolean,
    extraActivity: ExtraActivity,
    onEditClick: (ExtraActivity) -> Unit,
    editExtracurricularViewModel: ExtracurricularViewModel = hiltViewModel()
) {

    val response = remember {  editExtracurricularViewModel.state }

    LaunchedEffect(response) {
        if(response is ExtracurricularState.Success){
            navController.popBackStack()
        }
    }

    val context = LocalContext.current
    Scaffold(
        //scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Extra Activity",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    if(isCurUser) {
                        IconButton(onClick = { onEditClick(extraActivity) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = {
                            editExtracurricularViewModel.deleteExtraActivity(
                                extraActivity
                            )
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                modifier = Modifier.shadow(elevation = 10.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.tertiary,
        content = { padding ->
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(padding),
//                contentAlignment = Alignment.TopCenter
//            ) {
//                // Full screen card centered and padded
//                CodingProfileCard(
//                    extraActivity,
//                    onAddClick = {
//                        onEditClick()
//                    }
//                )
//            }


            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { openUrl(context, extraActivity.link) }
                            .padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
//                        // Header Section
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text("Activity Details", style = MaterialTheme.typography.titleLarge)
//                            IconButton(onClick = onAddClick) {
//                                Icon(Icons.Default.Edit, "Edit")
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(24.dp))

                        // Media Section
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Link Preview", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(16.dp))

                            if (extraActivity.media.isNullOrEmpty()) {
                                Icon(
                                    painter = painterResource(R.drawable.htmx_ic),
                                    contentDescription = "Default icon",
                                    modifier = Modifier.size(120.dp)
                                )
                            } else {
                                ImageUsingCoil(
                                    context = context,
                                    imageUrl = extraActivity.media,
                                    placeholder = R.drawable.htmx_ic,
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    errorImage = R.drawable.htmx_ic
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Details Section
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            InfoItem(
                                label = "Link",
                                value = extraActivity.link,
                                textStyle = MaterialTheme.typography.titleMedium
                            )

                            InfoItem(
                                label = "Name",
                                value = extraActivity.name,
                                textStyle = MaterialTheme.typography.titleMedium
                            )

                            InfoItem(
                                label = "Description",
                                value = extraActivity.description,
                                textStyle = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    textStyle: TextStyle
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = textStyle,
            modifier = Modifier.fillMaxWidth(),
            maxLines = when (label) {
                "Description" -> Int.MAX_VALUE
                else -> 1
            },
            overflow = when (label) {
                "Description" -> TextOverflow.Clip
                else -> TextOverflow.Ellipsis
            }
        )
    }
}
