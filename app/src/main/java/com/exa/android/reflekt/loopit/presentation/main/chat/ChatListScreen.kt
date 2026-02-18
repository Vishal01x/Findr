package com.exa.android.reflekt.loopit.presentation.main.chat

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.viewmodel.ChatViewModel
import com.exa.android.reflekt.loopit.core.permission.RequestNotificationPermissionIfNeeded
import com.exa.android.reflekt.loopit.presentation.main.component.showLoader
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.MapInfo
import com.exa.android.reflekt.loopit.presentation.navigation.component.PhotoRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.PostRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.bottomSheet
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.ChatList

@Composable
fun HomeScreen(navController: NavController, viewModel: ChatViewModel) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var isQueryClicker by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var originalChatList by remember { mutableStateOf(emptyList<ChatList>()) }


    LaunchedEffect(isQueryClicker) {
        if(isQueryClicker){
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    BackHandler(enabled = true) {
        if (isQueryClicker) {
            searchQuery = TextFieldValue("")
            isQueryClicker = false
        }
        //else (context as Activity).finish()
        else navController.navigate("post_graph") {
            popUpTo(PostRoute.PostList.route) {
                inclusive = true
            }
        }
    }
    RequestNotificationPermissionIfNeeded(true)

    val filteredList by remember(searchQuery, originalChatList, isQueryClicker) {
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
        ) {
            if (!isQueryClicker) {
                HeaderSection(navController) {
                    isQueryClicker = true
                }
            } else {
                QuerySection(
                    searchQuery = searchQuery,
                    focusRequester = focusRequester,
                    onQueryChange = { searchQuery = it },
                    onBackClick = {
                        searchQuery = TextFieldValue("")
                        isQueryClicker = false
                    }
                )
            }

            //Spacer(modifier = Modifier.height(12.dp))

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
}


@Composable
fun HeaderSection(navController: NavController, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Findr",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuerySection(
    searchQuery: TextFieldValue,
    focusRequester: FocusRequester,
    onQueryChange: (TextFieldValue) -> Unit,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        BasicTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .focusRequester(focusRequester),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onBackClick() }
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    Box {
                        if (searchQuery.text.isEmpty()) {
                            Text(
                                text = "Search chats...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
        modifier = Modifier.padding(start = 16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Story",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Add Story",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyChatState(navController = navController)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    //contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatList) { chat ->
                        ChatListItem(
                            chat = chat,
                            openImage = {
                                navController.navigate(
                                    PhotoRoute.ViewPhotoUsingUrl.createRoute(
                                        it
                                    )
                                )
                            },
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
            .fillMaxWidth()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon
        Image(
            imageVector = Icons.Filled.Chat,
            contentDescription = "Empty Chats",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
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

            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append("map screen")
            }

            append(". Connect with people around you!")
        }
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
