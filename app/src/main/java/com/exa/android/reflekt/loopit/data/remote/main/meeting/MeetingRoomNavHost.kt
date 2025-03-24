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

package io.getstream.meeting.room.compose.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import io.getstream.meeting.room.compose.ui.call.CallScreen
import io.getstream.meeting.room.compose.ui.lobby.LobbyScreen
/*
fun NavGraphBuilder.meetingRoomNavGraph(
  navController: NavHostController,
  startDestination: String = AppScreens.Main.destination,
) {
//  NavHost(
//    modifier = modifier
//      .fillMaxSize()
//      .background(VideoTheme.colors.appBackground),
//    navController = navController,
//    startDestination = startDestination
//  )
  navigation(startDestination = startDestination, route = "meeting_room"){
    composable(AppScreens.Main.destination) {
      LobbyScreen(navController = navController)
    }

    composable(AppScreens.Call.destination) {
      CallScreen(navController = navController)
    }
  }
}*/

enum class AppScreens(val destination: String) {
  Main("meeting_main"),
  Call("meeting_call"),
}
