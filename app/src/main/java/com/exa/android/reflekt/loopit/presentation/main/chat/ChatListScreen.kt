package com.exa.android.reflekt.loopit.presentation.main.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.core.permission.RequestNotificationPermissionIfNeeded
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.viewmodel.ChatViewModel
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.ChatList
import com.exa.android.reflekt.loopit.presentation.main.component.showLoader
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.MapInfo
import com.exa.android.reflekt.loopit.presentation.navigation.component.PhotoRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.PostRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProfileRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.bottomSheet
import com.exa.android.reflekt.loopit.presentation.main.placement.components.*
import com.exa.android.reflekt.loopit.data.feature.main.suggestion.SuggestionsViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: ChatViewModel) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val suggestionsViewModel: SuggestionsViewModel = hiltViewModel()
    val suggestions by suggestionsViewModel.suggestions.collectAsState()

    val response by viewModel.chatList.collectAsState()
    val currentUserId by viewModel.curUserId.collectAsState()
    val allChats = (response as? Response.Success<List<ChatList>>)?.data ?: emptyList()

    val displayList by remember(searchQuery, allChats, isSearchActive, selectedTab, currentUserId) {
        derivedStateOf {
            val base = when {
                searchQuery.text.isNotBlank() && isSearchActive -> {
                    val words = searchQuery.text.trim().lowercase().split("\\s+".toRegex())
                    allChats.filter { chat -> words.all { word -> chat.name.lowercase().contains(word) } }
                }
                selectedTab == 1 -> // "Requests" tab
                    allChats.filter { chat ->
                        chat.requestStatus == "pending" &&
                        chat.requestSenderId != currentUserId &&
                        chat.requestSenderId.isNotEmpty()
                    }
                else -> allChats
            }
            base
        }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    BackHandler {
        if (isSearchActive) {
            searchQuery = TextFieldValue("")
            isSearchActive = false
        } else {
            navController.navigate("post_graph") {
                popUpTo(PostRoute.PostList.route) { inclusive = true }
            }
        }
    }

    RequestNotificationPermissionIfNeeded(true)

    val pullState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.getChatList(forceRefresh = true)
            scope.launch {
                delay(2000)
                isRefreshing = false
            }
        },
        state = pullState,
        modifier = Modifier.fillMaxSize().background(Color.Transparent).statusBarsPadding(),
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullState,
                isRefreshing = isRefreshing,
                color = LiquidColors.Primary,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    ) {
        when (response) {
            is Response.Loading -> {
                Box(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    ChatShimmer()
                }
            }
            is Response.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ErrorState(onRetry = { viewModel.getChatList(forceRefresh = true) })
                }
            }
            is Response.Success -> {
                if (allChats.isEmpty() && !isSearchActive) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyChatState(navController = navController)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 160.dp)
                    ) {
                        item(key = "header") {
                            if (isSearchActive) {
                                QuerySection(
                                    searchQuery = searchQuery,
                                    focusRequester = focusRequester,
                                    onQueryChange = { searchQuery = it },
                                    onBackClick = {
                                        searchQuery = TextFieldValue("")
                                        isSearchActive = false
                                    }
                                )
                            } else {
                                MessagesHeader(
                                    onSearchClick = { isSearchActive = true },
                                    onNewChatClick = { navController.navigate(PostRoute.UserSearch.createRoute("new_chat", "New Chat")) }
                                )
                            }
                        }
                        if (!isSearchActive && allChats.isNotEmpty()) {
                            item(key = "suggestions") {
                                SuggestedUsersSection(
                                    suggestions = suggestions,
                                    onUserClick = { userId ->
                                        navController.navigate(ProfileRoute.UserProfile.createRoute(userId))
                                    }
                                )
                            }
                            item(key = "tabs") {
                                val pendingCount = allChats.count { chat ->
                                    chat.requestStatus == "pending" &&
                                    chat.requestSenderId != currentUserId &&
                                    chat.requestSenderId.isNotEmpty()
                                }
                                CategoryTabs(
                                    selectedTab = selectedTab,
                                    onTabSelect = { selectedTab = it },
                                    requestCount = pendingCount
                                )
                            }
                        }
                        if (selectedTab == 1 && displayList.isEmpty()) {
                            item(key = "empty_requests") {
                                EmptyRequestState(modifier = Modifier.fillParentMaxHeight(0.7f))
                            }
                        } else {
                            items(displayList, key = { it.userId }) { chat ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                    ChatListItem(
                                        chat = chat,
                                        currentUserId = currentUserId,
                                        openImage = {
                                            navController.navigate(PhotoRoute.ViewPhotoUsingUrl.createRoute(it))
                                        },
                                        openChat = {
                                            navController.navigate(HomeRoute.ChatDetail.createRoute(chat.userId))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyRequestState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Rounded.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = LiquidColors.TextSecondary.copy(alpha = 0.5f)
            )
            Text(
                "No pending requests",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = LiquidColors.TextPrimary
            )
            Text(
                "When someone wants to start a new chat with you, their request will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = LiquidColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MessagesHeader(onSearchClick: () -> Unit, onNewChatClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "Messages",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = LiquidColors.TextPrimary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LiquidColors.GlassSurface)
                    .border(1.dp, LiquidColors.GlassBorder, CircleShape)
                    .clickableWithNoIndication { onSearchClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = LiquidColors.TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LiquidColors.Primary)
                    .clickableWithNoIndication { onNewChatClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "New Chat",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}


@Composable
private fun CategoryTabs(selectedTab: Int, onTabSelect: (Int) -> Unit, requestCount: Int = 0) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(LiquidColors.GlassSurface)
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Inbox", "Requests").forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == index) LiquidColors.GlassSurface else Color.Transparent)
                        .clickableWithNoIndication { onTabSelect(index) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedTab == index) LiquidColors.Primary else LiquidColors.TextSecondary
                        )
                        if (index == 1 && requestCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$requestCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuerySection(
    searchQuery: TextFieldValue,
    focusRequester: FocusRequester,
    onQueryChange: (TextFieldValue) -> Unit,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(LiquidColors.GlassSurface)
                .border(1.dp, LiquidColors.GlassBorder, CircleShape)
                .clickableWithNoIndication { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = LiquidColors.TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        BasicTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(LiquidColors.GlassSurface)
                .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .focusRequester(focusRequester),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = LiquidColors.TextPrimary),
            cursorBrush = SolidColor(LiquidColors.Primary),
            decorationBox = { innerTextField ->
                Box {
                    if (searchQuery.text.isEmpty()) {
                        Text("Search messages...", style = MaterialTheme.typography.bodyMedium, color = LiquidColors.TextMuted)
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun EmptyChatState(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            imageVector = Icons.Filled.Chat,
            contentDescription = null,
            colorFilter = ColorFilter.tint(LiquidColors.Primary.copy(alpha = 0.6f)),
            modifier = Modifier.size(96.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "No Chats Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = LiquidColors.TextPrimary
        )
        Spacer(Modifier.height(16.dp))
        val primary = LiquidColors.Primary
        val annotatedString = buildAnnotatedString {
            append("Find new contacts by visiting the ")
            withStyle(SpanStyle(color = primary, fontWeight = FontWeight.Bold)) { append("map screen") }
            append(". Connect with people around you!")
        }
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodyLarge,
            color = LiquidColors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickableWithNoIndication { navController.navigate(MapInfo.MapScreen.route) }
        )
        Spacer(Modifier.height(32.dp))
        FilledTonalButton(
            onClick = { navController.navigate(MapInfo.MapScreen.route) },
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = LiquidColors.Primary,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Outlined.Map, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("View Map Screen")
        }
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Failed to load chats. Please try again.",
            color = Color(0xFFEF4444),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary, contentColor = Color.White)
        ) { Text("Retry") }
    }
}

@Composable
fun ShowbottomSheet(modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(bottomSheet) }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Enter Text") },
            confirmButton = { Button(onClick = { showDialog = false }) { Text("OK") } },
            dismissButton = { Button(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}

data class Story(val image: Int, val name: String)
data class Chat(val image: Int, val name: String, val lastMessage: String, val time: String, val unreadCount: Int)

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun ChatListScreenPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MessagesHeader(onSearchClick = {}, onNewChatClick = {})
            
            CategoryTabs(selectedTab = 0, onTabSelect = {}, requestCount = 3)
            
            EmptyRequestState()
            
            ErrorState(onRetry = {})
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun EmptyChatStatePreview() {
    MaterialTheme {
        EmptyChatState(navController = rememberNavController())
    }
}
