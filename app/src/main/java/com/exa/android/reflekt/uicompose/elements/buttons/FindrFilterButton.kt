package com.exa.android.reflekt.uicompose.elements.buttons

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.uicompose.enums.ButtonSize
import com.exa.android.reflekt.uicompose.enums.ButtonType
import com.exa.android.reflekt.uicompose.enums.FindrButtonDefaults
import com.exa.android.reflekt.uicompose.theme.FindrTheme

@Composable
fun FindrFilterButton(
    text: String? = null,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: ButtonSize = ButtonSize.SMALL
) {
    val colors = FindrTheme.colorScheme
    
    // Determine colors based on selection state
    val buttonColors = if (isSelected) {
        FindrButtonDefaults.Primary
    } else {
        FindrButtonDefaults.Secondary // Or Neutral depending on design
    }
    
    val type = if (isSelected) ButtonType.PRIMARY else ButtonType.SECONDARY

    FindrButton(
        params = FindrButtonParams(
            text = text ?: "Filter",
            onClick = onClick,
            modifier = modifier,
            type = type,
            size = size,
            colors = buttonColors,
            leadingIcon = if (isSelected) Icons.Default.Close else Icons.Default.FilterList
        )
    )
}
