package com.exa.android.reflekt.loopit.util.application

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.UserViewModel
import com.exa.android.reflekt.loopit.util.CurChatManager.activeChatId


class MyLifecycleObserver(
    private val viewModel: UserViewModel,
    private val userId: String
) : LifecycleObserver {


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForeground() {
        viewModel.updateOnlineStatus(userId, true)
    }

//    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
//    fun onAppPause() {
//        viewModel.updateOnlineStatus(userId, true)
//    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackground() {
        viewModel.updateOnlineStatus(userId, false)
        viewModel.setTypingStatus(userId, "") // when user while typing click home button then decompose will not be called
        //activeChatId = null // update it in detail chat using observer in that
        // that let the status typing but it should be offline
    }
}