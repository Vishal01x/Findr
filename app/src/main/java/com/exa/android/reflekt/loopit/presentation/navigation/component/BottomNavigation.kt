package com.exa.android.reflekt.loopit.presentation.navigation.component

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.size
import kotlin.math.min
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.layout.*
// Icon imports
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Work
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.ui.geometry.Size
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.ChatViewModel


data class BottomNavItemm(
    val route: String, val icon: ImageVector, val label: String, val onClick: () -> Unit
)

@Composable
fun CustomBottomNavigationBar(
    navController: NavController,
    chatViewModel: ChatViewModel,
    onNewChatClick: () -> Unit

) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val items = listOf(
        BottomNavItem(
            route = MainRoute.Project.route,
            iconSelected = Icons.Filled.Home,
            iconUnselected = Icons.Outlined.Home,
            label = "Home"
        ),
        BottomNavItem(
            route = MainRoute.Map.route,
            iconSelected = Icons.Filled.MyLocation,
            iconUnselected = Icons.Outlined.MyLocation,
            label = "Map"
        ),
        BottomNavItem(
            route = HomeRoute.ChatList.route,
            iconSelected = Icons.AutoMirrored.Filled.Chat,
            iconUnselected = Icons.AutoMirrored.Outlined.Chat,
            label = "Chats"
        ),
        BottomNavItem(
            route = MainRoute.Profile.route,
            iconSelected = Icons.Filled.Person,
            iconUnselected = Icons.Outlined.Person,
            label = "Profile"
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                NavigationItem(
                    item = item,
                    isSelected = currentDestination?.hierarchy?.any {
                        it.route == item.route
                    } == true,
                    onClick = {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    chatViewModel
                )
            }
        }
    }
}

@Composable
private fun NavigationItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    chatViewModel: ChatViewModel
) {
    val unreadCount by chatViewModel.unreadCount
    val showBadge = !isSelected && unreadCount > 0 && item.route == HomeRoute.ChatList.route
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(60.dp)
            .padding(vertical = 6.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // Conditional badge for chat icon
        if (showBadge) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else "$unreadCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = if (isSelected) item.iconSelected else item.iconUnselected,
                    contentDescription = item.label,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Icon(
                imageVector = if (isSelected) item.iconSelected else item.iconUnselected,
                contentDescription = item.label,
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.size(24.dp)
            )
        }


        /*if (item.route == HomeRoute.ChatList.route && unreadCount > 0 && !isSelected) { // for dot use for notification
            val dotcolor = MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .drawBehind {
                        drawCircle(
                            color = dotcolor,
                            radius = 6.dp.toPx(),
                            center = Offset(size.width, 0f)
                        )
                    }
                    .align(Alignment.TopEnd)
            )
        }*/

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = item.label,
            fontSize = 12.sp,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onTertiary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}


data class BottomNavItem(
    val route: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector,
    val label: String
)

@Composable
fun BottomBar(navController: NavController, viewModel: ChatViewModel) {
    val unreadCount by viewModel.unreadCount
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    val items = listOf(
        BottomNavItem(
            route = MainRoute.Project.route,
            iconSelected = Icons.Filled.Home,
            iconUnselected = Icons.Outlined.Home,
            label = "Home"
        ),
        BottomNavItem(
            route = MainRoute.Map.route,
            iconSelected = Icons.Filled.MyLocation,
            iconUnselected = Icons.Outlined.MyLocation,
            label = "Map"
        ),
        BottomNavItem(
            route = HomeRoute.ChatList.route,
            iconSelected = Icons.AutoMirrored.Filled.Chat,
            iconUnselected = Icons.AutoMirrored.Outlined.Chat,
            label = "Chats"
        ),
        BottomNavItem(
            route = MainRoute.Profile.route,
            iconSelected = Icons.Filled.Person,
            iconUnselected = Icons.Outlined.Person,
            label = "Profile"
        )
    )

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            val showBadge = item.route == HomeRoute.ChatList.route && unreadCount > 0 && !isSelected

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    // Conditional badge for chat icon
                    if (showBadge) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(
                                        text = if (unreadCount > 99) "99+" else "$unreadCount",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.iconSelected else item.iconUnselected,
                                contentDescription = item.label
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) item.iconSelected else item.iconUnselected,
                            contentDescription = item.label
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}

data class BottomItem(
    val route: String,
    val iconSelected: ImageVector,
    val label: String
)
