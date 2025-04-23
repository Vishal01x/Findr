package com.exa.android.reflekt.loopit.presentation.main.Home

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.ChatViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.component.showLoader
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.bottomSheet
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.model.ChatList
import com.google.gson.Gson


@Composable
fun HomeScreen(navController: NavController, viewModel: ChatViewModel) {
    val context = LocalContext.current
    BackHandler(enabled = true) {
        (context as Activity).finish()
    }

    var isQueryClicker by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var originalChatList by remember { mutableStateOf(emptyList<ChatList>()) }

    val filteredList by remember(searchQuery, originalChatList) {
        derivedStateOf {
            if (searchQuery.text.isBlank()) originalChatList
            else {
                val words = searchQuery.text.trim().lowercase().split("\\s+".toRegex())
                originalChatList.filter { chat ->
                    words.all { word -> chat.name.lowercase().contains(word) }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 2.dp)
    ) {
        if (!isQueryClicker) {
            HeaderSection(navController) {
                isQueryClicker = true
            }
        } else {
            QuerySection(
                searchQuery = searchQuery,
                onQueryChange = { searchQuery = it },
                onBackClick = {
                    isQueryClicker = false
                    searchQuery = TextFieldValue("")
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        ChatsSection(
            navController = navController,
            isQueryActive = isQueryClicker,
            chatList = filteredList,
            viewModel = viewModel
        ) { fetchedList ->
            originalChatList = fetchedList
        }
    }
}


@Composable
fun HeaderSection(navController: NavController, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Findr",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = Color.Black,
            modifier = Modifier
                .clickable { onClick() }
                .padding(8.dp)
                .size(24.dp)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuerySection(
    searchQuery: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        BasicTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary, CircleShape)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            decorationBox = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    Box {
                        if (searchQuery.text.isEmpty()) {
                            Text(
                                text = "Search chats...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )
    }
}

@Composable
fun ChatsSection(
    navController: NavController,
    isQueryActive: Boolean,
    chatList: List<ChatList>,
    viewModel: ChatViewModel,
    onSuccess: (List<ChatList>) -> Unit
) {
    val response by viewModel.chatList.collectAsState()

    when (response) {
        is Response.Loading -> {
            showLoader("Chats Loading...")
        }

        is Response.Success -> {
            val data = (response as Response.Success<List<ChatList>>).data
            onSuccess(data)

            if (data.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No chats Yet")
                    Spacer(modifier = Modifier.height(16.dp))
//                            Button(onClick = { navController.navigate(HomeRoute.SearchScreen.route) }) {
//                                Text("New Chat")
//                            }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chatList) { chat ->
                    ChatListItem(
                        chat = chat,
                        zoomImage = {/* navController.navigate("zoomImage/${chat.image}")*/ },
                        openChat = { navController.navigate(HomeRoute.ChatDetail.createRoute(chat.userId)) }
                    )
                }
            }
        }

        is Response.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Failed to load chats. Please try again.", color = Color.Red)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.getChatList() }) {
                    Text("Retry")
                }
            }
        }
    }
}


data class Chat(
    val image: Int,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int
)
