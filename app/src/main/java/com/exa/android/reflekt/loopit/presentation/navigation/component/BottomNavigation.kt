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
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.ui.geometry.Size
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy

/*@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("main_app", R.drawable.chat_ic, "Home"),
        BottomNavItem(MainRoute.Profile.route, R.drawable.assesment_ic, "Profile"),
        BottomNavItem(MainRoute.Setting.route, R.drawable.call_ic, "Settings")
    )

    BottomNavigation {
        val currentDestination = navController.currentBackStackEntryAsState()?.value?.destination
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(painter = painterResource(id = item.icon), contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentDestination?.route == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
*/

@Composable
fun CustomBottomNavigationBarr(
    navController: NavController, onNewChatClick: () -> Unit
) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    var selected = currentDestination?.route
    Log.d(
        "currentBackStackEntry->BottomNav",
        "selected - $selected, back - ${currentDestination?.route.toString()}"
    )
    // Define navigation items
    val items = listOf(BottomNavItemm(route = HomeRoute.ChatList.route,
        icon = Icons.Default.Home,
        label = "Home",
        onClick = {
            if (selected != HomeRoute.ChatList.route) {
                navController.navigate(HomeRoute.ChatList.route) {
                    // Clear back stack up to start destination
//                    popUpTo(navController.graph.findStartDestination().id) {
//                        saveState = true
//                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }),
        BottomNavItemm(route = MainRoute.Profile.route,
            icon = Icons.Default.Person,
            label = "Profile",
            onClick = {
                //  if (selected != MainRoute.Profile.route) {
                Log.d("profile", "2")
                navController.navigate(MainRoute.Profile.route) {
//                // Clear back stack up to start destination
//                popUpTo(navController.graph.findStartDestination().id) {
//                    saveState = true
//                }
                    launchSingleTop = true
                    restoreState = true
                }
                // }
            }),
        BottomNavItemm(
            route = MainRoute.Map.route,
            icon = Icons.Default.LocationOn,
            label = "Map",
            onClick = {
                //  if (selected != MainRoute.Profile.route) {
                Log.d("Map", "2")
                navController.navigate("map_graph") {
                    launchSingleTop = true
                    restoreState = true
                }
                // }
            }
        ),

        BottomNavItemm(
            route = MainRoute.Project.route,
            icon = Icons.Default.List,
            label = "Project",
            onClick = {
                //  if (selected != MainRoute.Profile.route) {
                Log.d("Project", "2")
                navController.navigate("project_graph") {
                    launchSingleTop = true
                    restoreState = true
                }
                // }
            }
        )
    )



    // Render the custom navigation bar
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                items[0].onClick()
                //selected.value = items[0].route
            }) {
            Icon(
                imageVector = items[0].icon,
                contentDescription = items[0].label,
                tint = if (selected == items[0].route) Color.Black else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
        }
        // Central Button (always unselected but functional)
        /*Box(contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(Color.Black, shape = RoundedCornerShape(50))
                .clickable { onNewChatClick() }
                .padding(horizontal = 24.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Chat",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(22.dp)
                )
                Text(
                    text = "New Chat",
                    color = Color.White,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp
                )
            }
        }*/

        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                Log.d("profile", "1")
                items[1].onClick()
                //selected.value = items[1].route
            }) {
            Icon(
                imageVector = items[1].icon,
                contentDescription = items[1].label,
                tint = if (currentDestination?.route == items[1].route) Color.Black else Color.Gray,
                modifier = Modifier
                    .size(32.dp)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                Log.d("Map", "1")
                items[2].onClick()
            }
        ) {
            Icon(
                imageVector = items[2].icon,
                contentDescription = items[2].label,
                tint = if (currentDestination?.route == items[2].route) Color.Black else Color.Gray,
                modifier = Modifier
                    .size(32.dp)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                Log.d("Map", "1")
                items[3].onClick()
            }
        ) {
            Icon(
                imageVector = items[3].icon,
                contentDescription = items[3].label,
                tint = if (currentDestination?.route == items[3].route) Color.Black else Color.Gray,
                modifier = Modifier
                    .size(32.dp)
            )
        }


    }
}


data class BottomNavItemm(
    val route: String, val icon: ImageVector, val label: String, val onClick: () -> Unit
)

@Composable
fun CustomBottomNavigationBar(
    navController: NavController,
    onNewChatClick: () -> Unit
) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val items = listOf(
        BottomNavItem(
            route = HomeRoute.ChatList.route,
            iconSelected = Icons.Filled.Home,
            iconUnselected = Icons.Outlined.Home,
            label = "Home"
        ),
        BottomNavItem(
            route = MainRoute.Profile.route,
            iconSelected = Icons.Filled.Person,
            iconUnselected = Icons.Outlined.Person,
            label = "Profile"
        ),
        BottomNavItem(
            route = MainRoute.Map.route,
            iconSelected = Icons.Filled.MyLocation,
            iconUnselected = Icons.Outlined.MyLocation,
            label = "Map"
        ),
        BottomNavItem(
            route = MainRoute.Project.route,
            iconSelected = Icons.Filled.Work,
            iconUnselected = Icons.Outlined.Work,
            label = "Projects"
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
                    }
                )
            }
        }
    }
}
@Composable
private fun NavigationItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
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
        Icon(
            imageVector = if (isSelected) item.iconSelected else item.iconUnselected,
            contentDescription = item.label,
            tint = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.label,
            fontSize = 10.sp,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}


@Composable
private fun NavigationItemm(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "navItemAnimation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick
            )
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Animated icon scale
            Icon(
                imageVector = if (isSelected) item.iconSelected else item.iconUnselected,
                contentDescription = item.label,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                },
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer {
                        scaleX = 1f + 0.2f * animatedProgress
                        scaleY = 1f + 0.2f * animatedProgress
                    }
            )

            // Pulsing animation for selected item
            if (isSelected) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0xFF4875E1).copy(alpha = 0.1f),
                        radius = size.minDimension / 2 * (1 + animatedProgress * 0.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Animated label
        Text(
            text = item.label,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
            },
            fontSize = if (isSelected) 12.sp else 11.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

data class BottomNavItem(
    val route: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector,
    val label: String
)
