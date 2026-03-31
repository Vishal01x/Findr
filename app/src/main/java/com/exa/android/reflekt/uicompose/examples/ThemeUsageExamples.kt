package com.exa.android.reflekt.uicompose.examples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.uicompose.theme.*

/**
 * Example demonstrating how to use the FindrTheme design system
 */

// ============================================================================
// Usage Example: Accessing Theme Properties
// ============================================================================

@Composable
fun ThemeUsageExample() {
    // Access color scheme
    val colors = FindrTheme.colorScheme
    val typography = FindrTheme.typography
    val dimens = FindrTheme.dimens
    val shapes = FindrTheme.shapes
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface.primary)
            .padding(dimens.margin.medium),
        verticalArrangement = Arrangement.spacedBy(dimens.margin.medium)
    ) {
        // Headline with primary color
        Text(
            text = "Welcome to Findr",
            style = typography.headlineLarge,
            color = colors.content.primary
        )
        
        // Body text with secondary color
        Text(
            text = "This is an example of using the Findr design system",
            style = typography.bodyMedium,
            color = colors.content.secondary
        )
        
        // Primary button
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimens.card.minHeight),
            shape = shapes.button.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.content.onPrimary
            )
        ) {
            Text(
                text = "Primary Action",
                style = typography.labelLarge
            )
        }
        
        // Card example
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimens.card.mediumHeight),
            shape = shapes.card.medium,
            color = colors.surface.secondary,
            tonalElevation = dimens.elevation.small
        ) {
            Column(
                modifier = Modifier
                    .padding(dimens.card.padding),
                verticalArrangement = Arrangement.spacedBy(dimens.card.spacing)
            ) {
                Text(
                    text = "Card Title",
                    style = typography.titleMedium,
                    color = colors.content.primary
                )
                Text(
                    text = "Card content goes here",
                    style = typography.bodySmall,
                    color = colors.content.tertiary
                )
            }
        }
    }
}

// ============================================================================
// Usage Example: Color Extension Functions
// ============================================================================

@Composable
fun ColorExtensionExample() {
    val colors = FindrTheme.colorScheme
    val dimens = FindrTheme.dimens
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimens.margin.medium),
        verticalArrangement = Arrangement.spacedBy(dimens.margin.small)
    ) {
        // Original color
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(colors.primary)
        ) {
            Text(
                text = "Original Color",
                color = colors.primary.contentColor(),
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // Hover color
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(colors.primary.hoverColor())
        ) {
            Text(
                text = "Hover Color",
                color = colors.primary.hoverColor().contentColor(),
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // Pressed color
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(colors.primary.pressedColor())
        ) {
            Text(
                text = "Pressed Color",
                color = colors.primary.pressedColor().contentColor(),
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // Disabled color
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(colors.primary.disabledColor())
        ) {
            Text(
                text = "Disabled Color",
                color = colors.primary.disabledColor().contentColor(),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

// ============================================================================
// Usage Example: Icons
// ============================================================================

@Composable
fun IconsUsageExample() {
    val colors = FindrTheme.colorScheme
    val dimens = FindrTheme.dimens
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimens.margin.medium),
        horizontalArrangement = Arrangement.spacedBy(dimens.margin.medium)
    ) {
        // Default (Outlined) Icon
        Icon(
            imageVector = FindrIcons.Default.HOME.icon,
            contentDescription = "Home",
            tint = colors.content.primary,
            modifier = Modifier.size(dimens.icon.medium)
        )
        
        // Filled Icon
        Icon(
            imageVector = FindrIcons.Filled.FAVORITE.icon,
            contentDescription = "Favorite",
            tint = colors.critical,
            modifier = Modifier.size(dimens.icon.medium)
        )
        
        // Using getIconById helper
        getIconById("search", filled = false)?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = "Search",
                tint = colors.content.secondary,
                modifier = Modifier.size(dimens.icon.medium)
            )
        }
    }
}

// ============================================================================
// Usage Example: Responsive Design
// ============================================================================

@Composable
fun ResponsiveExample() {
    ResponsiveFindrTheme {
        // Your content automatically adapts to screen size
        ThemeUsageExample()
    }
}

@Composable
fun CustomCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val colors = FindrTheme.colorScheme
    val typography = FindrTheme.typography
    val dimens = FindrTheme.dimens
    val shapes = FindrTheme.shapes
    
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = shapes.card.medium,
        color = colors.surface.primary,
        tonalElevation = dimens.elevation.small,
        shadowElevation = dimens.elevation.small
    ) {
        Column(
            modifier = Modifier
                .padding(dimens.card.padding),
            verticalArrangement = Arrangement.spacedBy(dimens.card.spacing)
        ) {
            Text(
                text = title,
                style = typography.titleMedium,
                color = colors.content.primary
            )
            
            Text(
                text = description,
                style = typography.bodySmall,
                color = colors.content.secondary
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimens.margin.small)
            ) {
                Icon(
                    imageVector = FindrIcons.Default.ARROW_RIGHT.icon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(dimens.icon.small)
                )
            }
        }
    }
}

@Preview(name = "Light Theme", showBackground = true)
@Composable
fun ThemeUsageExampleLightPreview() {
    FindrLightThemePreview {
        ThemeUsageExample()
    }
}

@Preview(name = "Dark Theme", showBackground = true)
@Composable
fun ThemeUsageExampleDarkPreview() {
    FindrDarkThemePreview {
        ThemeUsageExample()
    }
}

@Preview(name = "Color Extensions", showBackground = true)
@Composable
fun ColorExtensionExamplePreview() {
    FindrLightThemePreview {
        ColorExtensionExample()
    }
}

@Preview(name = "Icons Example", showBackground = true)
@Composable
fun IconsUsageExamplePreview() {
    FindrLightThemePreview {
        IconsUsageExample()
    }
}

@Preview(name = "Custom Card", showBackground = true)
@Composable
fun CustomCardPreview() {
    FindrLightThemePreview {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CustomCard(
                title = "Example Card",
                description = "This is a custom card component using the Findr theme",
                onClick = { }
            )
        }
    }
}
