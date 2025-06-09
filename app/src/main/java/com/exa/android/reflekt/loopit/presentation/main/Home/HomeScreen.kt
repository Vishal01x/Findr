package com.exa.android.reflekt.loopit.presentation.main.Home

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.ChatViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.Listing.screen.ProjectDetailScreen
import com.exa.android.reflekt.loopit.presentation.main.Home.component.RequestNotificationPermissionIfNeeded
import com.exa.android.reflekt.loopit.presentation.main.Home.component.showLoader
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.MapInfo
import com.exa.android.reflekt.loopit.presentation.navigation.component.PhotoRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProjectRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.bottomSheet
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.model.ChatList
import com.google.gson.Gson

@Composable
fun HomeScreen(navController: NavController, viewModel: ChatViewModel) {
    val context = LocalContext.current
    var isQueryClicker by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var originalChatList by remember { mutableStateOf(emptyList<ChatList>()) }

    BackHandler(enabled = true) {
        if(isQueryClicker){
            searchQuery = TextFieldValue("")
            isQueryClicker = false
        }
        //else (context as Activity).finish()
        else navController.navigate("project_graph"){
            popUpTo(ProjectRoute.ProjectList.route){
                inclusive = true
            }
        }
    }
    RequestNotificationPermissionIfNeeded(true)

    val filteredList by remember(searchQuery, originalChatList,isQueryClicker) {
        derivedStateOf {
            if (searchQuery.text.isBlank() || !isQueryClicker) originalChatList
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
            .imePadding()
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
                    searchQuery = TextFieldValue("")
                    isQueryClicker = false
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
/*
@Composable
fun ChatsSection(
    navController: NavController,
    isQueryActive : Boolean,
    updateChatList: List<ChatList>,
    viewModel: ChatViewModel,
    onSuccess: (List<ChatList>) -> Unit
) {
    val chatList by viewModel.chatList.collectAsState()
    Log.d("chatsSection", chatList.toString())
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp), // Avoid spacing issues
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Add a title to the chat list
//        item {
//            ChatTitle()
//        }

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
                onSuccess(response.data)
                // Display the chat list
                if (response.data.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No chats Yet",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navController.navigate(HomeRoute.SearchScreen.route) }) {
                                Text(text = "New Chat")
                            }
                        }
                    }
                }
                val data = if(isQueryActive)updateChatList else response.data
                items(response.data) { chat ->
                    ChatListItem(
                        chat = chat,
                        zoomImage = { imageId ->
                            navController.navigate("zoomImage/$imageId")
                        },
                        openChat = { user ->
//                            val userJson = Gson().toJson(user)
//                            val encodedUserJson = java.net.URLEncoder.encode(userJson, "UTF-8")
//                            navController.navigate(HomeRoute.ChatDetail.createRoute(encodedUserJson))

                            navController.navigate(HomeRoute.ChatDetail.createRoute(user.userId))
                        }
                    )
                }
            }

            is Response.Error -> {

                Log.d("ChatList", response.message)
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


 */


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
                    EmptyChatState(navController = navController)
                }
            }else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatList) { chat ->
                        ChatListItem(
                            chat = chat,
                            openImage = {navController.navigate(PhotoRoute.ViewPhotoUsingUrl.createRoute(it))/* navController.navigate("zoomImage/${chat.image}")*/ },
                            openChat = {
                                navController.navigate(
                                    HomeRoute.ChatDetail.createRoute(
                                        chat.userId
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }

        is Response.Error -> {
            ErrorState(onRetry = { viewModel.getChatList() })




        }
    }
}

@Composable
private fun EmptyChatState(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon
        Image(
            imageVector = Icons.Filled.Chat,
            contentDescription = "Empty Chats",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)),
            modifier = Modifier.size(96.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Chats Yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description with clickable text
        val annotatedString = buildAnnotatedString {
            append("Find new contacts by visiting the ")

            withStyle(style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )) {
                append("map screen")
            }

            append(". Connect with people around you!")
        }
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                navController.navigate(MapInfo.MapScreen.route)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Map Screen Button
        FilledTonalButton(
            onClick = { navController.navigate(MapInfo.MapScreen.route) },
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Map,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Map Screen")
        }
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Failed to load chats. Please try again.",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Retry")
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

