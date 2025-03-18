// ui/home/HomeScreen.kt
package com.exa.android.reflekt.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.model.ChatUser
import com.exa.android.reflekt.ui.chat.UserAvatar
import com.exa.android.reflekt.ui.theme.*

@Composable
fun HomeScreen(
    users: List<ChatUser>,
    onUserClicked: (ChatUser) -> Unit,
    onNewChatClicked: () -> Unit
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }
    val filteredUsers = remember(searchQuery, users) {
        if (searchQuery.text.isEmpty()) {
            users
        } else {
            users.filter { user ->
                user.name.contains(searchQuery.text, ignoreCase = true) ||
                        user.lastMessage.contains(searchQuery.text, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onNewChatClick = onNewChatClicked
            )
        },
        content = { padding ->
            UserList(
                users = filteredUsers,
                onUserClicked = onUserClicked,
                modifier = Modifier.padding(padding)
            )
        }
    )
}

@Composable
private fun HomeTopBar(
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    onNewChatClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .clip(TextFieldShape),
                    colors = textFieldColors(),
                    placeholder = {
                        Text(
                            "Search users...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onNewChatClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AppColors.CoralAccent)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun UserList(
    users: List<ChatUser>,
    onUserClicked: (ChatUser) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(users) { user ->
            UserListItem(
                user = user,
                onClick = { onUserClicked(user) }
            )
            Divider(
                color = AppColors.DividerColor,
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun UserListItem(
    user: ChatUser,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(user.imageUrl, user.isOnline)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (user.unreadCount > 0) {
                        Badge(
                            containerColor = AppColors.CoralAccent,
                            contentColor = Color.White
                        ) {
                            Text(text = user.unreadCount.toString())
                        }
                    }
                }

                Text(
                    text = user.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Preview
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview() {
    ReflektTheme {
        HomeScreen(
            users = listOf(
                ChatUser(
                    id = "1",
                    name = "Alice Smith",
                    imageUrl = "",
                    lastMessage = "Hey, are you coming tomorrow?",
                    unreadCount = 2,
                    isOnline = true,
                    lastSeen = ""
                ),
                ChatUser(
                    id = "2",
                    name = "Bob Johnson",
                    imageUrl = "",
                    lastMessage = "Thanks for the help!",
                    unreadCount = 0,
                    isOnline = false,
                    lastSeen = ""
                )
            ),
            onUserClicked = {},
            onNewChatClicked = {}
        )
    }
}