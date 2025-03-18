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
import com.exa.android.reflekt.loopit.mvvm.ViewModel.ChatViewModel
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.util.Response

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
                                .clickable { navController.navigate(HomeRoute.ChatDetail.createRoute(user.userId)) }
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


//@Composable
//fun WhatsAppSearchScreen(
//    navController: NavController,
//    viewModel: ChatViewModel = hiltViewModel(), // ViewModel to handle search logic
//    onChatSelected: (Chat) -> Unit // Callback when a chat is selected
//) {
//    var isSearching by remember { mutableStateOf(false) } // Track whether the search bar is visible
//    var query by remember { mutableStateOf("") } // Current search query
//
//    // Observe search results from ViewModel
//    val searchResult by viewModel.searchResult.collectAsState()
//    val chatList by viewModel.chatList.collectAsState(emptyList()) // Full chat list
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                        OutlinedTextField(
//                            value = query,
//                            onValueChange = {
//                                query = it
//                                viewModel.searchUser(it) // Trigger search
//                            },
//                            placeholder = { Text("Search…") },
//                            singleLine = true,
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                },
//                navigationIcon = {
//                    if (isSearching) {
//                        IconButton(onClick = { navController.popBackStack(); isSearching = false; query = "" }) {
//                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                        }
//                    }
//                }//,
////                actions = {
////                    if (!isSearching) {
////                        IconButton(onClick = { isSearching = true }) {
////                            Icon(Icons.Default.Search, contentDescription = "Search")
////                        }
////                    }
////                }
//            )
//        }
//    ) { paddingValues ->
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            // Dynamically show filtered chats or all chats
//            val chatsToDisplay = if (isSearching) searchResult else chatList
//
//            items(chatsToDisplay) { chat ->
//                ChatListItem(chat = chat, onClick = { onChatSelected(chat) })
//            }
//        }
//    }
//}
//
//@Composable
//fun ChatListItem(chat: Chat, onClick: () -> Unit) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onClick() }
//            .padding(16.dp)
//    ) {
//        // Placeholder for profile image
//        Box(
//            modifier = Modifier
//                .size(48.dp)
//                .clip(CircleShape)
//                .background(Color.Gray)
//        )
//
//        Spacer(modifier = Modifier.width(16.dp))
//
//        Column {
//            Text(text = chat.users.joinToString(", "), style = MaterialTheme.typography.bodyLarge)
//            Text(
//                text = chat.lastMessage,
//                style = MaterialTheme.typography.bodyMedium,
//                color = Color.Gray,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
//        }
//    }
//}