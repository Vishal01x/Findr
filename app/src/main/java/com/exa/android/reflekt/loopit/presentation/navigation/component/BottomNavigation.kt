package com.exa.android.reflekt.loopit.presentation.navigation.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import androidx.navigation.compose.currentBackStackEntryAsState


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
fun CustomBottomNavigationBar(
    navController: NavController, onNewChatClick: () -> Unit
) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    var selected = currentDestination?.route
    Log.d("currentBackStackEntry->BottomNav", "selected - $selected, back - ${currentDestination?.route.toString()}")
    // Define navigation items
    val items = listOf(BottomNavItem(route = HomeRoute.ChatList.route,
        icon = Icons.Default.Home,
        label = "Home",
        onClick = {
            if (selected != HomeRoute.ChatList.route) {
                navController.navigate(HomeRoute.ChatList.route) {
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }), BottomNavItem(route = MainRoute.Profile.route,
        icon = Icons.Default.Person,
        label = "Profile",
        onClick = {
            //  if (selected != MainRoute.Profile.route) {
            Log.d("profile", "2")
            navController.navigate(MainRoute.Profile.route) {
                launchSingleTop = true
                restoreState = true
            }
            // }
        }))

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
        Box(contentAlignment = Alignment.Center,
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
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                Log.d("profile", "1")
                items[1].onClick()
                //selected.value = items[1].route
            }) {
            Icon(imageVector = items[1].icon,
                contentDescription = items[1].label,
                tint = if (currentDestination?.route == items[1].route) Color.Black else Color.Gray,
                modifier = Modifier
                    .size(32.dp)
            )
        }

    }
}


data class BottomNavItem(
    val route: String, val icon: ImageVector, val label: String, val onClick: () -> Unit
)
