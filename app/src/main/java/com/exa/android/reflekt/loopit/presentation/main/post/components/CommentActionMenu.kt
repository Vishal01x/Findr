package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors

@Composable
fun CommentActionMenu(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val containerColor = LiquidColors.Surface
    val borderColor = LiquidColors.Border
    val textPrimary = LiquidColors.TextPrimary
    val errorColor = MaterialTheme.colorScheme.error
    val iconTint = LiquidColors.UtilityLabel

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "More options",
                tint = iconTint
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = containerColor,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 8.dp,
            border = BorderStroke(0.5.dp, borderColor)
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    expanded = false
                    onEditClick()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null
                    )
                },
                colors = MenuDefaults.itemColors(
                    textColor = textPrimary,
                    leadingIconColor = textPrimary
                )
            )

            HorizontalDivider(
                color = borderColor,
                thickness = 0.5.dp
            )

            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    expanded = false
                    onDeleteClick()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null
                    )
                },
                colors = MenuDefaults.itemColors(
                    textColor = errorColor,
                    leadingIconColor = errorColor
                )
            )
        }
    }
}
