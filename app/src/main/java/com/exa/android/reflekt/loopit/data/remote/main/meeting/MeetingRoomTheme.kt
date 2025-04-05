
package com.exa.android.reflekt.loopit.data.remote.main.meeting

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.getstream.video.android.compose.theme.StreamColors
import io.getstream.video.android.compose.theme.VideoTheme

@Composable
fun MeetingRoomTheme(
  content: @Composable () -> Unit,
) {
  VideoTheme(
    colors = if (isSystemInDarkTheme()) {
      StreamColors.defaultDarkColors().copy(appBackground = Color(0xFF2C2C2E))
    } else {
      StreamColors.defaultColors().copy(appBackground = Color(0xFF2C2C2E))
    },
  ) {
    content.invoke()
  }
}
