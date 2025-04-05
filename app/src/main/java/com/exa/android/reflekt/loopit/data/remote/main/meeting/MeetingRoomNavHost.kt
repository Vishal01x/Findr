

package com.exa.android.reflekt.loopit.data.remote.main.meeting

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
