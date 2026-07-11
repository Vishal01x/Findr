package com.exa.android.reflekt.loopit.presentation.auth.components

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors

@Composable
fun AnimatedAuthBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LiquidColors.BgBase)
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun AnimatedAuthBackgroundPreview() {
    androidx.compose.material3.MaterialTheme {
        AnimatedAuthBackground {
            androidx.compose.material3.Text("Preview Content")
        }
    }
}
