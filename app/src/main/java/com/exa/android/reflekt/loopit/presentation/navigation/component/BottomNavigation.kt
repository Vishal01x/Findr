package com.exa.android.reflekt.loopit.presentation.navigation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavDestination.Companion.hierarchy
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.viewmodel.ChatViewModel
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors


data class BottomNavItemm(
    val route: String, val icon: ImageVector, val label: String, val onClick: () -> Unit
)

private const val IDX_HOME      = 0
private const val IDX_PLACEMENT = 1
private const val IDX_FAB       = 2
private const val IDX_CHAT      = 3
private const val IDX_MAP       = 4

/**
 * Pill-shaped floating bottom navigation bar.
 * Layout: Home | Placement | [+] | Chat | Search
 *
 * Replicates the reference image's shape / placement / effects.
 * Adapts to both light and dark mode via LiquidColors.
 */
@Composable
fun CustomBottomNavigationBar(
    navController: NavController,
    chatViewModel: ChatViewModel,
    onNewChatClick: () -> Unit
) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val unreadCount by chatViewModel.unreadCount

    fun isSelected(route: String) =
        currentDestination?.hierarchy?.any { it.route == route } == true

    val homeSelected      = isSelected(MainRoute.Project.route)
    val placementSelected = isSelected(MainRoute.Placement.route)
    val chatSelected      = isSelected(HomeRoute.ChatList.route)
    val mapSelected       = isSelected(MainRoute.Map.route)

    fun navigate(route: String) = navController.navigate(route) {
        popUpTo(navController.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState    = true
    }

    val primary   = LiquidColors.Primary          // Theme primary color
    val accent    = Color(0xFF8B5CF6)             // purple accent
    val isDark    = LiquidColors.isDark

    // ── FAB animations (pulse + glow — identical mechanics to reference) ────
    val infinite = rememberInfiniteTransition(label = "fab_anim")
    val fabPulse by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(950, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "fab_pulse"
    )
    val fabGlow by infinite.animateFloat(
        initialValue = 0.30f, targetValue = 0.72f,
        animationSpec = infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "fab_glow"
    )

    // ── Outer container — keeps FAB centred above the bar ──────────────────
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp), // floating gap from edges & bottom
        contentAlignment = Alignment.BottomCenter
    ) {

        // ── Pill bar ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                // Pill shadow — subtle colored glow matching the brand
                .shadow(
                    elevation    = 28.dp,
                    shape        = RoundedCornerShape(50.dp),  // fully pill-shaped
                    ambientColor = primary.copy(alpha = 0.14f),
                    spotColor    = primary.copy(alpha = 0.22f)
                )
                .clip(RoundedCornerShape(50.dp))
                // Dark: very dark translucent card; Light: near-white card
                .background(
                    if (isDark) Color(0xFF111C22) else Color(0xFFFFFFFF)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Home
                NavTabItem(
                    label          = "Home",
                    selected       = homeSelected,
                    primary        = primary,
                    isDark         = isDark,
                    selectedIcon   = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home,
                    onClick        = { navigate(MainRoute.Project.route) }
                )

                // Placement
                NavTabItem(
                    label          = "Placement",
                    selected       = placementSelected,
                    primary        = primary,
                    isDark         = isDark,
                    selectedIcon   = Icons.Filled.Work,
                    unselectedIcon = Icons.Outlined.Work,
                    onClick        = { navigate(MainRoute.Placement.route) }
                )

                // Centre spacer — FAB sits here but floats above
                Spacer(modifier = Modifier.width(58.dp))

                // Chat
                val pendingRequests by chatViewModel.pendingRequestCount
                NavTabItem(
                    label          = "Chat",
                    selected       = chatSelected,
                    primary        = primary,
                    isDark         = isDark,
                    selectedIcon   = Icons.Filled.Forum,
                    unselectedIcon = Icons.Outlined.Forum,
                    badgeCount     = if (!chatSelected && (unreadCount > 0 || pendingRequests > 0))
                                         (unreadCount + pendingRequests) else 0,
                    onClick        = { navigate(HomeRoute.ChatList.route) }
                )

                // Search
                NavTabItem(
                    label          = "Search",
                    selected       = mapSelected,
                    primary        = primary,
                    isDark         = isDark,
                    selectedIcon   = Icons.Filled.Search,
                    unselectedIcon = Icons.Outlined.Search,
                    onClick        = { navigate(MainRoute.Map.route) }
                )
            }
        }

        // ── Centre FAB — floats vertically centred on top of the bar ──────
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 2.dp),   // slightly above bar midline — same as reference
            contentAlignment = Alignment.Center
        ) {
            // Outer glow halo (animated alpha)
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = fabGlow * 0.20f))
            )
            // Inner glow halo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = fabGlow * 0.10f))
            )
            // FAB body
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .scale(fabPulse)
                    .shadow(
                        elevation    = 24.dp,
                        shape        = CircleShape,
                        ambientColor = primary.copy(alpha = 0.55f),
                        spotColor    = primary.copy(alpha = 0.70f)
                    )
                    .clip(CircleShape)
                    .background(primary) // + icon color as per theme
                    .clickableWithNoIndication {
                        navController.navigate(PostRoute.CreatePostSelection.route) {
                            launchSingleTop = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Bold white "+" cross
                Canvas(modifier = Modifier.size(22.dp)) {
                    val s  = size.minDimension
                    val sw = 2.8.dp.toPx()
                    drawLine(Color.White, Offset(0f, s / 2f), Offset(s, s / 2f), sw, StrokeCap.Round)
                    drawLine(Color.White, Offset(s / 2f, 0f), Offset(s / 2f, s), sw, StrokeCap.Round)
                }
            }
        }
    }
}

// ── Tab item ─────────────────────────────────────────────────────────────────

@Composable
private fun NavTabItem(
    label          : String,
    selected       : Boolean,
    primary        : Color,
    isDark         : Boolean,
    selectedIcon   : ImageVector,
    unselectedIcon : ImageVector,
    badgeCount     : Int = 0,
    onClick        : () -> Unit
) {
    val color by animateColorAsState(
        targetValue   = if (selected) primary
                        else if (isDark) Color(0xFF6B7280) else Color(0xFF94A3B8),
        animationSpec = tween(280),
        label         = "tab_color_$label"
    )
    val bgAlpha by animateFloatAsState(
        targetValue   = if (selected) 1f else 0f,
        animationSpec = tween(280),
        label         = "tab_bg_$label"
    )
    val iconScale by animateFloatAsState(
        targetValue   = if (selected) 1.18f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "icon_scale_$label"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clickableWithNoIndication(onClick = onClick)
    ) {
        // Soft Circular glow spot behind selected icon
        if (bgAlpha > 0f) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(primary.copy(alpha = 0.35f * bgAlpha), Color.Transparent),
                            radius = 60f
                        )
                    )
            )
        }

        // Icon + badge container
        Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
            val iconContent = @Composable {
                Icon(
                    imageVector        = if (selected) selectedIcon else unselectedIcon,
                    contentDescription = label,
                    tint               = color,
                    modifier           = Modifier
                        .size(26.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                )
            }
            
            if (badgeCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(containerColor = primary, contentColor = Color.White) {
                            Text(
                                text = if (badgeCount > 99) "99+" else "$badgeCount",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                ) {
                    iconContent()
                }
            } else {
                iconContent()
            }
        }
    }
}

// ── Legacy BottomBar (kept for reference / fallback) ─────────────────────────

data class BottomNavItem(
    val route         : String,
    val iconSelected  : ImageVector,
    val iconUnselected: ImageVector,
    val label         : String
)

data class BottomItem(
    val route       : String,
    val iconSelected: ImageVector,
    val label       : String
)

@Composable
fun BottomBar(navController: NavController, viewModel: ChatViewModel) {
    val unreadCount by viewModel.unreadCount
    val currentDest = navController.currentBackStackEntryAsState().value?.destination

    val items = listOf(
        BottomNavItem(MainRoute.Project.route,   Icons.Filled.Home,                  Icons.Outlined.Home,                  "Home"),
        BottomNavItem(MainRoute.Placement.route, Icons.Filled.Work,                  Icons.Outlined.Work,                  "Placement"),
        BottomNavItem(HomeRoute.ChatList.route,  Icons.Filled.Forum,                 Icons.Outlined.Forum,                 "Chats"),
        BottomNavItem(MainRoute.Map.route,       Icons.Filled.Search,                Icons.Outlined.Search,                "Search")
    )

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
        items.forEach { item ->
            val isSelected = currentDest?.hierarchy?.any { it.route == item.route } == true
            val showBadge  = item.route == HomeRoute.ChatList.route && unreadCount > 0 && !isSelected
            NavigationBarItem(
                selected = isSelected,
                onClick  = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon = {
                    if (showBadge) {
                        BadgedBox(badge = {
                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                Text(
                                    if (unreadCount > 99) "99+" else "$unreadCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }) {
                            Icon(if (isSelected) item.iconSelected else item.iconUnselected, item.label)
                        }
                    } else {
                        Icon(if (isSelected) item.iconSelected else item.iconUnselected, item.label)
                    }
                },
                label = {
                    Text(
                        item.label,
                        fontSize   = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}
