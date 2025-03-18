/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-video-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.meeting.room.compose.ui.component

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
