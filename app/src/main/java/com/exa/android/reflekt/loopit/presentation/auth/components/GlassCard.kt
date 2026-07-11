package com.exa.android.reflekt.loopit.presentation.auth.components

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(LiquidColors.GlassSurface)
            .border(
                width = 1.dp,
                color = LiquidColors.GlassBorder,
                shape = shape
            )
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun GlassCardPreview() {
    androidx.compose.material3.MaterialTheme {
        GlassCard(
            modifier = Modifier.padding(16.dp)
        ) {
            androidx.compose.material3.Text(
                text = "Glass Card Content",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
