package com.exa.android.reflekt.loopit.presentation.main.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.profile.data.viewmodel.EditProfileViewModel
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.viewmodel.UserViewModel
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.CollegeInfo
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ExperienceInfo
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ExtraActivity
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ProfileData
import com.exa.android.reflekt.loopit.presentation.main.component.showLoader
import com.exa.android.reflekt.loopit.presentation.main.profile.components.*
import com.exa.android.reflekt.loopit.presentation.main.profile.components.education.EducationCard
import com.exa.android.reflekt.loopit.presentation.main.profile.components.education.ExperienceCard
import com.exa.android.reflekt.loopit.presentation.main.profile.components.extra_card.ExtracurricularCard
import com.exa.android.reflekt.loopit.presentation.main.profile.components.header.ImageHeader
import com.exa.android.reflekt.loopit.presentation.main.profile.components.header.ProfileHeader
import com.exa.android.reflekt.loopit.presentation.main.profile.feedback.ProfileFeedback
import com.exa.android.reflekt.loopit.presentation.main.profile.feedback.ProfileViews
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProfileRoute
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.presentation.main.post.screen.PostHomeViewModel
import com.exa.android.reflekt.loopit.presentation.navigation.component.PostRoute
import androidx.compose.ui.graphics.Color
import com.exa.android.reflekt.loopit.presentation.main.placement.components.GlassCard
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.placement.components.liquidTopBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String?,
    navController: NavController,
    onEditHeaderClick: (ProfileData) -> Unit,
    onAddExtraCard: (String?, ExtraActivity?) -> Unit,
    openChat: (String?) -> Unit,
    openImage: (String?) -> Unit,
    onEditEducation: (CollegeInfo?) -> Unit,
    onEditExperience: (ExperienceInfo) -> Unit,
    onViewVerifier : (String?) -> Unit,
    userViewModel: UserViewModel = hiltViewModel(),
    editProfileViewModel: EditProfileViewModel = hiltViewModel()
) {
    val profileData by userViewModel.userProfileData.collectAsState()
    val curUser = editProfileViewModel.curUser
    var showShareSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    LaunchedEffect(Unit) {
        userViewModel.getProfileData(userId)
        editProfileViewModel.getAverageRating(userId)
        editProfileViewModel.getAllVerifiersDetail(userId)
        if(userId != null && userId != curUser) {
            editProfileViewModel.getRatingByCurUser(userId)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Profile Screen",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = LiquidColors.TextPrimary
                            )
                        )
                    }
                },
                colors = liquidTopBarColors(),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    val targetUserId = userId ?: curUser
                    IconButton(onClick = { showShareSheet = true }) {
                        Icon(Icons.Default.Share, "Share", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { navController.navigate(PostRoute.MyPosts.createRoute(targetUserId ?: "")) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Article, "My Posts",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { navController.navigate(ProfileRoute.ProfileSetting.route) }) {
                        Icon(Icons.Default.Settings, "Settings", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        },
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { padding ->
        val pullState = rememberPullToRefreshState()
        var isRefreshing by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                userViewModel.getProfileData(userId ?: curUser)
                scope.launch {
                    delay(2000)
                    isRefreshing = false
                }
            },
            state = pullState,
            modifier = Modifier.fillMaxSize(),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullState,
                    isRefreshing = isRefreshing,
                    color = LiquidColors.Primary,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        ) {
            when (val response = profileData) {
                is Response.Error -> {
                    ProfileContent(
                        profileData = ProfileData(),
                        userId = userId,
                        curUser = curUser,
                        onEditHeaderClick = onEditHeaderClick,
                        onAddExtraCard = onAddExtraCard,
                        openChat = openChat,
                        openImage = openImage,
                        onEditEducation = onEditEducation,
                        onEditExperience = onEditExperience,
                        onViewVerifier = onViewVerifier,
                        padding = padding,
                        editProfileViewModel = editProfileViewModel,
                        onShowAllPosts = { 
                            val targetUserId = userId ?: curUser
                            navController.navigate(PostRoute.MyPosts.createRoute(targetUserId ?: "")) 
                        },
                        onPostClick = { postId -> navController.navigate(PostRoute.PostDetail.createRoute(postId)) }
                    )
                }

                Response.Loading -> {
                    showLoader("Loading Profile...")
                }

                is Response.Success -> {
                    editProfileViewModel.loadUserProfileData(response.data)
                    ProfileContent(
                        profileData = response.data,
                        userId = userId,
                        curUser = curUser,
                        onEditHeaderClick = onEditHeaderClick,
                        onAddExtraCard = onAddExtraCard,
                        openChat = openChat,
                        openImage = openImage,
                        onEditEducation = onEditEducation,
                        onEditExperience = onEditExperience,
                        onViewVerifier = onViewVerifier,
                        padding = padding,
                        editProfileViewModel = editProfileViewModel,
                        onShowAllPosts = { 
                            val targetUserId = userId ?: curUser
                            navController.navigate(PostRoute.MyPosts.createRoute(targetUserId ?: "")) 
                        },
                        onPostClick = { postId -> navController.navigate(PostRoute.PostDetail.createRoute(postId)) }
                    )
                }
            }

            if (showShareSheet) {
                val profile = (profileData as? Response.Success)?.data ?: ProfileData()
                val targetUserId = userId ?: curUser ?: ""

                com.exa.android.reflekt.loopit.presentation.main.component.sheet.AppShareSheet(
                    onCopyLink = {
                        scope.launch {
                            val shortLink = com.exa.android.reflekt.loopit.core.utils.ShareUtils.buildShortProfileFdlUrl(context, targetUserId)
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(shortLink))
                            android.widget.Toast.makeText(context, "Link copied!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    onSystemShare = {
                        scope.launch {
                            val shareData = com.exa.android.reflekt.loopit.core.utils.ShareUtils.prepareProfileShareData(context, profile, targetUserId)
                            com.exa.android.reflekt.loopit.core.utils.ShareUtils.executeShare(context, shareData)
                        }
                    },
                    onDismiss = { showShareSheet = false }
                )
            }
        }
    }
}

@Composable
fun ProfileContent(
    profileData: ProfileData,
    userId: String?,
    curUser : String?,
    onEditHeaderClick: (ProfileData) -> Unit,
    onAddExtraCard: (String?, ExtraActivity?) -> Unit,
    openChat: (String?) -> Unit,
    openImage: (String?) -> Unit,
    onEditEducation: (CollegeInfo?) -> Unit,
    onEditExperience: (ExperienceInfo) -> Unit,
    onViewVerifier : (String?) -> Unit,
    padding: PaddingValues,
    editProfileViewModel: EditProfileViewModel,
    onShowAllPosts: () -> Unit,
    onPostClick: (String) -> Unit,
    postViewModel: PostHomeViewModel = hiltViewModel()
) {
    val scrollState = rememberLazyListState()
    val postState by postViewModel.state.collectAsState()
    val isCurUser = userId == null || userId == curUser

    LaunchedEffect(userId) {
        if (userId != curUser && userId != null) {
            editProfileViewModel.updateProfileView(userId, profileData.profileHeader.profileImageUrl)
        }
    }

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .padding(padding)
            .navigationBarsPadding()
            .fillMaxSize()
    ) {
        item {
            ImageHeader(
                userId == null || userId == curUser,
                profileData.profileHeader,
                onEditClick = {
                    onEditHeaderClick(profileData)
                },
                openImage = openImage
            )
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 8.dp)) {

                ProfileHeader(
                    userId,
                    curUser,
                    profileData.profileHeader,
                    profileData.collegeInfo,
                    openChat = { openChat(userId) },
                    editProfileViewModel
                )

                if (profileData.isProfileDetailsEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    EmptyProfileOverviewCard(
                        isCurUser = isCurUser,
                        onEditIntro = { onEditHeaderClick(profileData) },
                        onAddEducation = { onEditEducation(profileData.collegeInfo) },
                        onAddExperience = { onEditExperience(profileData.experienceInfo) }
                    )
                }

                Spacer(Modifier.height(12.dp))

                AboutCard(
                    userId == null || userId == curUser,
                    title = "About",
                    content = profileData.about.description
                )

                Spacer(Modifier.height(12.dp))

                val skill = profileData.skill
                SkillsCard(userId == null || userId == curUser, skill.split(",").map { it.trim() })

                Spacer(Modifier.height(12.dp))

                ExtracurricularCard(userId, curUser, onAddClick = { onAddExtraCard(userId, it) })

                Spacer(Modifier.height(12.dp))

                ExperienceCard(
                    userId == null || userId == curUser,
                    experienceInfo = profileData.experienceInfo,
                    onEditExperience = { onEditExperience(profileData.experienceInfo) }
                )

                Spacer(Modifier.height(12.dp))

                EducationCard(
                    isCurUser,
                    collegeInfo = profileData.collegeInfo,
                    onEditEducation = { onEditEducation(profileData.collegeInfo) }
                )

                Spacer(Modifier.height(12.dp))

                val targetUserId = userId ?: curUser
                val isCurUserCheck = targetUserId == curUser
                val firstName = profileData.profileHeader.name.split(" ").firstOrNull() ?: "User"
                val postsTitle = if (isCurUserCheck) "My Posts" else "$firstName's Posts"

                MyPostsPreviewSection(
                    title = postsTitle,
                    posts = postState.posts.filter { it.createdBy == targetUserId }.take(3),
                    isLoading = postState.isLoading,
                    onShowAllClick = onShowAllPosts,
                    onPostClick = onPostClick
                )

                Spacer(Modifier.height(12.dp))

                ProfileFeedback(userId, editProfileViewModel) {
                    onViewVerifier(userId)
                }

                if (userId == null || userId == curUser) {
                    ProfileViews(userId,editProfileViewModel)
                }

                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

private fun ProfileData.isProfileDetailsEmpty(): Boolean {
    return about.description.isBlank() &&
            skill.isBlank() &&
            collegeInfo.instituteName.isBlank() &&
            experienceInfo.title.isBlank() &&
            profileHeader.headline.isBlank()
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun EmptyProfileOverviewCard(
    isCurUser: Boolean,
    onEditIntro: () -> Unit,
    onAddEducation: () -> Unit,
    onAddExperience: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(10.dp).size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isCurUser) "Build your profile" else "Profile details coming soon",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isCurUser) {
                            "Add a headline, education, and experience so people know what you are working on."
                        } else {
                            "This person has not added profile details yet. You can still view their posts and activity."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isCurUser) {
                Spacer(Modifier.height(14.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = onEditIntro,
                        label = { Text("Intro") },
                        leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    AssistChip(
                        onClick = onAddEducation,
                        label = { Text("Education") },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    AssistChip(
                        onClick = onAddExperience,
                        label = { Text("Experience") },
                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MyPostsPreviewSection(
    title: String,
    posts: List<Post>,
    isLoading: Boolean,
    onShowAllClick: () -> Unit,
    onPostClick: (String) -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (posts.isNotEmpty()) {
                    TextButton(onClick = onShowAllClick) {
                        Text("Show all", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            when {
                isLoading && posts.isEmpty() -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                posts.isEmpty() -> Text(
                    text = "No posts yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(posts) { post ->
                        ProfilePostPreviewCard(post = post, onClick = { onPostClick(post.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfilePostPreviewCard(
    post: Post,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(190.dp)
            .height(166.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(LiquidColors.GlassSurface)
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(14.dp))
            .clickableWithNoIndication(onClick = onClick)
    ) {
        val coverImage = post.imageUrls.firstOrNull()
        if (coverImage != null) {
            AsyncImage(
                model = coverImage,
                contentDescription = post.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Rounded.Article, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = post.title.ifBlank { post.type },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = post.description.ifBlank { post.type },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${post.likesCount} likes • ${post.commentsCount} comments",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
