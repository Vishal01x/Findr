package com.exa.android.reflekt.loopit.presentation.main.notification.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model.Notification
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model.NotificationCategory
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(NotificationCategory.ALL)
    val selectedCategory: StateFlow<NotificationCategory> = _selectedCategory

    val notifications = repository.getNotifications()
        .combine(_selectedCategory) { list, category ->
            if (category == NotificationCategory.ALL) list
            else list.filter { it.category == category }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> = repository.getUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            repository.syncNotificationsRealTime()
        }
    }

    fun setCategory(category: NotificationCategory) {
        _selectedCategory.value = category
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch { repository.markAsRead(notificationId) }
    }

    fun markAllAsRead() {
        viewModelScope.launch { repository.markAllAsRead() }
    }

    fun acceptInvitation(notification: Notification) {
        val postId = notification.targetId ?: return
        viewModelScope.launch { repository.acceptInvitation(notification.id, postId) }
    }

    fun rejectInvitation(notification: Notification) {
        viewModelScope.launch { repository.rejectInvitation(notification.id) }
    }
}
