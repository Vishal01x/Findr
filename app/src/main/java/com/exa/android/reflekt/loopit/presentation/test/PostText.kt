package com.exa.android.reflekt.loopit.presentation.test

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.presentation.main.Home.component.ImageUsingCoil
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.model.User
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

// ----------------- Data Models -----------------

data class Postt(
    val id: String,
    val userName: String,
    val timeAgo: String,
    val likes: Int,
    val tags: List<Tag>,
    val title: String,
    val description: String,
    val actionText: String,
    val hashTags: List<String>
)

enum class TagType { INFO, URGENT }

data class Tag(
    val text: String,
    val type: TagType
)

data class UiState(
    val posts: List<Postt> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

// ----------------- Repository -----------------

interface PostRepository {
    suspend fun getPosts(): List<Postt>
}

class PostRepositoryImpl @Inject constructor() : PostRepository {
    override suspend fun getPosts(): List<Postt> {
        return listOf(
            Postt(
                id = "1",
                userName = "Vishal Kumar",
                timeAgo = "2h",
                likes = 456,
                tags = listOf(
                    Tag("Expert", TagType.INFO),
                    Tag("Learning", TagType.INFO),
                    Tag("Urgent", TagType.URGENT)
                ),
                title = "DSA Study Session with Vishal - Join at 7 PM!",
                description = "Vishal is leading an intensive DSA discussion session tonight at 7 PM...",
                actionText = "Join DSA Study Session - Google Meet",
                hashTags = listOf("DSA", "Study Group", "Programming", "Algorithms")
            )
        )
    }
}

// ----------------- ViewModel -----------------

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set

    init {
        loadPosts()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val posts = repository.getPosts()
                uiState = uiState.copy(posts = posts, isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        uiState = uiState.copy(searchQuery = query)
    }
}

// ----------------- Colors -----------------

val DarkBlue = Color(0xFF0F1B2B)
val LightBlue = Color(0xFF1976D2)
val ChipBlue = Color(0xFFE3F2FD)
val TextBlue = Color(0xFF2196F3)
val UrgentRed = Color(0xFFFF5252)
val LightGray = Color(0xFFF5F5F5)
val DarkGray = Color(0xFF616161)

// ----------------- UI Components -----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    showMyProjectsOnly: Boolean,
    curUserDetails: Response<User?>?,
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {

    val context = LocalContext.current

    TopAppBar(
//        title = {
//            Text(
//                text = title,
//                color = MaterialTheme.colorScheme.primary,
//                fontWeight = FontWeight.ExtraBold,
//                fontSize = 26.sp // Make it bigger
//            )
//        },

        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (showMyProjectsOnly) "My Activity" else "Findr",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 26.sp
                    )
                )
                if (showMyProjectsOnly) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(start = 8.dp)
                    )
                }
            }
        },

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        actions = {
            IconCircleBackground(
                icon = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                onClick = onNotificationsClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            if(curUserDetails is Response.Success && !curUserDetails.data?.profilePicture.isNullOrEmpty()){
                ImageUsingCoil(context,curUserDetails.data?.profilePicture, modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(12.dp))
                   // .border(1.dp, Color.Black, CircleShape)
                    .clickable { onProfileClick() })
            }else {
                IconCircleBackground(
                    icon = Icons.Default.Person,
                    contentDescription = "Profile",
                    onClick = onProfileClick
                )
            }
        }
    )
}

@Composable
fun IconCircleBackground(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(.1f)) // Light Grey
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SearchFilter(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    val primary = Color(0xFF4875E1)
    val backgroundColor = Color(0x1A4875E1) // 10% opacity → 0x1A

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(45.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(.1f)),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                val focusManager = LocalFocusManager.current

                CompositionLocalProvider(
                    LocalTextSelectionColors provides TextSelectionColors(
                        handleColor = MaterialTheme.colorScheme.primary,
                        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                ) {
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onTertiary),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (query.isEmpty()) {
                                Text(
                                    text = "Search project,post, discuss",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth(),

                        )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(primary.copy(alpha = 0.1f))
                .clickable { onFilterClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.FilterAlt,
                contentDescription = "Filter",
                tint = primary
            )
        }
    }
}

@Composable
fun RolesSection(
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Roles",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Button(
            onClick = onAddClick,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary),

            ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Post",
                tint = Color.White
            )

            Spacer(Modifier.width(8.dp))

            Text("Post", style = MaterialTheme.typography.bodyLarge)
        }
    }
}


@Composable
fun PostCard(post: Postt) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LightGray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        post.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Schedule,
                            contentDescription = "time posted",
                            tint = DarkGray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("${post.timeAgo}", color = DarkGray, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.weight(1f))

                InfoTag("Learning")
            }

//            Spacer(modifier = Modifier.height(8.dp))
//            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                post.tags.forEach {
//                    when (it.type) {
//                        TagType.URGENT -> UrgentTag(it.text)
//                        TagType.INFO -> InfoTag(it.text)
//                    }
//                }
//            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                post.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onTertiary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                post.description,
                color = MaterialTheme.colorScheme.onTertiary.copy(.8f),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightBlue)
                    .clickable { }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(post.actionText, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                post.hashTags.take(3).forEach { HashTag(it) }
                if (post.hashTags.size > 3) {
                    Text("+${post.hashTags.size - 3} more", color = TextBlue, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun InfoTag(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(ChipBlue)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 12.sp, color = TextBlue, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun UrgentTag(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(UrgentRed.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = "Urgent",
            tint = UrgentRed,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, color = UrgentRed)
    }
}

@Composable
fun HashTag(text: String) {
    Text(
        text = "#$text",
        color = TextBlue,
        fontSize = 12.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(ChipBlue)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
fun BottomNavigation() {
    val items = listOf("Discuss", "Map", "Chat", "College", "Theme")
    val selectedItem = remember { mutableStateOf("Discuss") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        items.forEach { item ->
            Text(
                text = item,
                color = if (item == selectedItem.value) LightBlue else DarkGray,
                fontWeight = if (item == selectedItem.value) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.clickable { selectedItem.value = item }
            )
        }
    }
}
/*
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState = viewModel.uiState

    Scaffold(
        topBar = {
            AppHeader(
                showMyProjectsOnly = false,
                curUserDetails = ,
                onNotificationsClick = TODO(),
                onProfileClick = TODO()
            )
        },
        bottomBar = { BottomNavigation() }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.posts.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No posts available")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .background(LightGray)
                ) {
                    item {
                        SearchFilter(
                            query = TODO(),
                            onQueryChange = viewModel::updateSearchQuery,
                            onFilterClick = TODO()
                        )
                    }
                    item {
                        Text(
                            "Most Recent",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 8.dp)
                        )
                    }
                    items(uiState.posts) { post ->
                        PostCard(post = post)
                    }
                }
            }
        }
    }
}
*/
// ----------------- Hilt Module -----------------

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providePostRepository(): PostRepository = PostRepositoryImpl()
}

//
//@Preview(showBackground = true)
//@Composable
//fun HeaderPreview() {
//    AppTheme {
//        Column {
//            AppHeader(
//                showMyProjectsOnly = false,
//                curUserDetails = curUserDetails,
//                onNotificationsClick = TODO(),
//                onProfileClick = TODO()
//            )
//            SearchFilter("", {}, {})
//            RolesSection { }
//            PostCard(
//                Postt(
//                    id = "1",
//                    userName = "Vishal Kumar",
//                    timeAgo = "2h",
//                    likes = 456,
//                    tags = listOf(
//                        Tag("Expert", TagType.INFO),
//                        Tag("Learning", TagType.INFO),
//                        Tag("Urgent", TagType.URGENT)
//                    ),
//                    title = "DSA Study Session with Vishal - Join at 7 PM!",
//                    description = "Vishal is leading an intensive DSA discussion session tonight at 7 PM. We'll cover dynamic programming, graph algorithms, and solve some...",
//                    actionText = "Join DSA Study Session - Google Meet",
//                    hashTags = listOf("DSA", "Study Group", "Programming", "Algorithms")
//                )
//            )
//        }
//
//    }
//}

//@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
//@Composable
//fun FullScreenPreview() {
//    AppTheme {
//        HomeScreen()
//    }
//}