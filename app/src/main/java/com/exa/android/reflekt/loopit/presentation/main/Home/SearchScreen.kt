package com.exa.android.reflekt.loopit.presentation.main.Home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.ChatViewModel
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.util.Response
import com.google.gson.Gson

@Composable
fun SearchScreen(navController: NavController, viewModel: ChatViewModel) {
    var query by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(true) }
    val searchResult by viewModel.searchResult.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Top Row with Back Button and Search Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(onClick = {
                navController.popBackStack()
                isSearching = false
                query = ""
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Search TextField
            TextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.searchUser(it) // Trigger search in ViewModel
                },
                placeholder = { Text(text = "Search...") },
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Results or Placeholder
        if (isSearching && query.isNotBlank()) {
            when (val response = searchResult) {
                is Response.Loading -> {
                    Text("Searching...", Modifier.padding(16.dp))
                }

                is Response.Success -> {
                    if (response.data == null) {
                        Text("No results found", Modifier.padding(16.dp))
                    } else {
                        val user = response.data
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 8.dp)
                                .clickable {
//                                    val userJson = Gson().toJson(user)
//                                    val encodedUserJson = java.net.URLEncoder.encode(userJson, "UTF-8")
//                                    navController.navigate(HomeRoute.ChatDetail.createRoute(encodedUserJson))
                                      navController.navigate(HomeRoute.ChatDetail.createRoute(user.userId))
                                }
                        ) {
                            Image(
                                painter = painterResource(R.drawable.chat_img3),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.SpaceAround
                            ) {
                                Text(
                                    user.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.DarkGray,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                is Response.Error -> {
                    Text("Error: ${response.message}", Modifier.padding(16.dp), color = Color.Red)
                }
            }
        }
    }
}
