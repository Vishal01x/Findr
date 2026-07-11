package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Role

/**
 * Display a role chip with enrollment information
 */
@Composable
fun RoleChip(
    role: Role,
    enrolledCount: Int,
    isEnrolled: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isFull = enrolledCount >= role.count
    val backgroundColor = when {
        isEnrolled -> MaterialTheme.colorScheme.primaryContainer
        isFull -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    val borderColor = when {
        isEnrolled -> MaterialTheme.colorScheme.primary
        isFull -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }
    
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickableWithNoIndication(enabled = !isFull || isEnrolled) { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Role icon
            Icon(
                imageVector = if (isEnrolled) Icons.Rounded.Check else Icons.Rounded.Person,
                contentDescription = null,
                tint = if (isEnrolled) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = role.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isEnrolled) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isEnrolled) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp
                    )
                    
                    // Enrollment count badge
                    Text(
                        text = "$enrolledCount/${role.count}",
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isFull -> MaterialTheme.colorScheme.error
                            enrolledCount > 0 -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                
                // Technologies
                if (role.technologies.isNotEmpty()) {
                    Text(
                        text = role.technologies.joinToString(", "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * Compact role chip for grid display
 */
@Composable
fun CompactRoleChip(
    role: Role,
    enrolledCount: Int,
    modifier: Modifier = Modifier
) {
    val isFull = enrolledCount >= role.count
    
    Surface(
        modifier = modifier,
        color = if (isFull) 
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        else 
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                tint = if (isFull) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
            
            Text(
                text = "${role.name} • $enrolledCount/${role.count}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isFull) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        }
    }
}
