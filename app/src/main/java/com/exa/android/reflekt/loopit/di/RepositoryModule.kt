package com.exa.android.reflekt.loopit.di

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.data.feature.main.post.data.repository.PostRepositoryImpl
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.repository.NotificationRepository
import com.exa.android.reflekt.loopit.data.feature.main.notification.data.repository.NotificationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindProjectRepository(
        impl: PostRepositoryImpl
    ): PostRepository

    @Binds
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository

}