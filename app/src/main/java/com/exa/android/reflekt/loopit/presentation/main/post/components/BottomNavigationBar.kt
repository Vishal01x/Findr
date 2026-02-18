package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.theme.*

/**
 * Bottom navigation bar with 5 items and centered gradient FAB
 */
@Composable
fun BottomNavigationBar(
    selectedItem: Int = 0,
    onItemSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.small, horizontal = Spacing.small),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            BottomNavItem(
                icon = if (selectedItem == 0) Icons.Rounded.Home else Icons.Rounded.Home,
                label = "Home",
                selected = selectedItem == 0,
                onClick = { onItemSelected(0) }
            )
            
            // Chat
            BottomNavItem(
                icon = if (selectedItem == 1) Icons.Rounded.Chat else Icons.Rounded.ChatBubbleOutline,
                label = "Chat",
                selected = selectedItem == 1,
                onClick = { onItemSelected(1) }
            )
            
            // Add Post (Center) - Gradient FAB
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                GradientPurpleStart,
                                GradientPurpleMid,
                                GradientPurpleEnd
                            )
                        )
                    )
                    .shadow(8.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { onItemSelected(2) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Post",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            // Placement
            BottomNavItem(
                icon = if (selectedItem == 3) Icons.Rounded.Work else Icons.Rounded.WorkOutline,
                label = "Jobs",
                selected = selectedItem == 3,
                onClick = { onItemSelected(3) }
            )
            
            // Profile
            BottomNavItem(
                icon = if (selectedItem == 4) Icons.Rounded.Person else Icons.Rounded.PersonOutline,
                label = "Profile",
                selected = selectedItem == 4,
                onClick = { onItemSelected(4) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconSize by animateDpAsState(
        targetValue = if (selected) 26.dp else 24.dp,
        label = "iconSize"
    )
    
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    
    val contentColor by animateColorAsState(
        targetValue = if (selected) selectedColor else unselectedColor,
        label = "iconColor"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.small, vertical = Spacing.extraSmall)
    ) {
        // Active Indicator (Dot)
        AnimatedVisibility(
            visible = selected,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GradientPurpleStart, GradientPurpleEnd)
                        )
                    )
            )
        }
        
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(iconSize)
        )
        
        // Label only visible when selected or make it always visible but subtle
        if (selected) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
