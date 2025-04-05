

package com.exa.android.reflekt.loopit.data.remote.main.meeting.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.video.android.compose.theme.VideoTheme

@Composable
fun StreamButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        enabled = enabled,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = VideoTheme.colors.primaryAccent, // Background color
            contentColor = VideoTheme.colors.primaryAccent,   // Text/icon color
            disabledContainerColor = Color(0xFF979797),       // Disabled background
            disabledContentColor = Color(0xFF979797)          // Disabled text/icon
        ),
    ) {
        Text(
            text = text,
            color = Color.White,
        )
    }
}

@Preview
@Composable
private fun StreamButtonPreview() {
    VideoTheme {
        StreamButton(text = "Sign In with Email", onClick = {})
    }
}
