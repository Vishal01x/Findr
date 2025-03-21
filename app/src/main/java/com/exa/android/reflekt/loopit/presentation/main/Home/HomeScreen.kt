package com.exa.android.reflekt.loopit.presentation.main.Home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.mvvm.ViewModel.ChatViewModel
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.bottomSheet
import com.exa.android.reflekt.loopit.util.Response

@Composable
fun HomeScreen(navController: NavController, viewModel: ChatViewModel) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 2.dp)
    ) {
        HeaderSection(navController)
        Spacer(modifier = Modifier.height(12.dp))
        StoriesSection()
        Spacer(modifier = Modifier.height(16.dp))
        ChatsSection(navController, viewModel)
    }
}

@Composable
fun HeaderSection(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Mengobrol",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = Color.Black,
            modifier = Modifier
                .clickable { navController.navigate(HomeRoute.SearchScreen.route) }
                .padding(8.dp)
                .size(24.dp)
        )
    }
}

@Composable
fun StoriesSection() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp), // No padding at the end
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Add "Add Story" item
        item {
            AddStoryItem()
        }

        // Add stories
        items(storyList) { story ->
            StoryItem(image = story.image, name = story.name)
        }
    }
}

@Composable
fun AddStoryItem() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(start = 12.dp) // Padding for the first item
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(54.dp)
                .border(
                    width = 1.dp,
                    color = Color.Gray.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Story",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Add Story",
            style = MaterialTheme.typography.titleSmall,
            color = Color.Black
        )
    }
}

@Composable
fun ChatsSection(navController: NavController, viewModel: ChatViewModel) {
    val chatList by viewModel.chatList.collectAsState()
    Log.d("chatsSection", chatList.toString())
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp), // Avoid spacing issues
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Add a title to the chat list
        item {
            ChatTitle()
        }

        // Handle the state of the chat list
        when (val response = chatList) {
            is Response.Loading -> {
                // Show a loading indicator
                item {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }

            is Response.Success -> {
                // Display the chat list
                if (response.data.isEmpty()) {
                    item {
                        Column(
                            modifier =  Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "No chats Yet", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navController.navigate(HomeRoute.SearchScreen.route)}) {
                                Text(text = "New Chat")
                            }
                        }
                    }
                }
                items(response.data) { chat ->
                    ChatListItem(
                        chat = chat,
                        zoomImage = { imageId ->
                            navController.navigate("zoomImage/$imageId")
                        },
                        openChat = { userId ->
                            navController.navigate(HomeRoute.ChatDetail.createRoute(userId))
                        }
                    )
                }
            }

            is Response.Error -> {
                // Show a friendly error message and optional retry button
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Failed to load chats. Please try again.",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.getChatList() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ChatTitle(modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Chats",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp
        )

        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "search chat",
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun ShowbottomSheet(modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(bottomSheet) }
    var textState by remember { mutableStateOf("") }

    // Show the dialog box if showDialog is true
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { bottomSheet = false },  // Dismiss the dialog on outside click
            title = { Text("Enter Text") },
            text = {
                Column {
                    Text("Please enter something below:")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = textState,
                        onValueChange = { textState = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Input") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Handle the OK button action
                        bottomSheet = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        // Handle the Cancel button action
                        bottomSheet = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}


// Sample data classes
data class Story(val image: Int, val name: String)


data class Chat(
    val image: Int,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int
)

// Sample data
val storyList = listOf(
    Story(R.drawable.chat_img3, "Terry"),
    Story(R.drawable.chat_img3, "Craig"),
    Story(R.drawable.chat_img3, "Roger"),
    Story(R.drawable.chat_img3, "Nolan"),
    Story(R.drawable.chat_img3, "Terry"),
    Story(R.drawable.chat_img3, "Craig"),
    Story(R.drawable.chat_img3, "Roger"),
    Story(R.drawable.chat_img3, "Nolan"),
    Story(R.drawable.chat_img3, "Terry"),
    Story(R.drawable.chat_img3, "Craig"),
    Story(R.drawable.chat_img3, "Roger"),
    Story(R.drawable.chat_img3, "Nolan")

)

val chatList = listOf(
    Chat(
        R.drawable.chat_img3,
        "Angel Curtis",
        "Please help me find a good monitor...",
        "02:11",
        2
    ),
    Chat(R.drawable.chat_img3, "Zaire Dorwart", "Gacor pisan kang", "02:11", 2),
    Chat(R.drawable.chat_img3, "Kelas Malam", "Bima: No one can come today?", "02:11", 2),
    Chat(R.drawable.chat_img3, "Jocelyn Gouse", "You're now an admin", "02:11", 0),
    Chat(R.drawable.chat_img3, "Jaylon Dias", "Buy back 10k gallons...", "02:11", 0),
    Chat(R.drawable.chat_img3, "Chance Rhiel Madsen", "Thank you mate!", "02:11", 2),
    Chat(
        R.drawable.chat_img3,
        "Angel Curtis",
        "Please help me find a good monitor...",
        "02:11",
        2
    ),
    Chat(R.drawable.chat_img3, "Zaire Dorwart", "Gacor pisan kang", "02:11", 2),
    Chat(R.drawable.chat_img3, "Kelas Malam", "Bima: No one can come today?", "02:11", 2),
    Chat(R.drawable.chat_img3, "Jocelyn Gouse", "You're now an admin", "02:11", 0),
    Chat(R.drawable.chat_img3, "Jaylon Dias", "Buy back 10k gallons...", "02:11", 0),
    Chat(R.drawable.chat_img3, "Chance Rhiel Madsen", "Thank you mate!", "02:11", 2),
    Chat(
        R.drawable.chat_img3,
        "Angel Curtis",
        "Please help me find a good monitor...",
        "02:11",
        2
    ),
    Chat(R.drawable.chat_img3, "Zaire Dorwart", "Gacor pisan kang", "02:11", 2),
    Chat(R.drawable.chat_img3, "Kelas Malam", "Bima: No one can come today?", "02:11", 2),
    Chat(R.drawable.chat_img3, "Jocelyn Gouse", "You're now an admin", "02:11", 0),
    Chat(R.drawable.chat_img3, "Jaylon Dias", "Buy back 10k gallons...", "02:11", 0),
    Chat(R.drawable.chat_img3, "Chance Rhiel Madsen", "Thank you mate!", "02:11", 2)
)
