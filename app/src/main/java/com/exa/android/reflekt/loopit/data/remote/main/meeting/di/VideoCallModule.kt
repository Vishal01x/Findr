package com.exa.android.reflekt.loopit.data.remote.main.meeting.di

import android.content.Context
import com.exa.android.reflekt.loopit.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.getstream.video.android.core.StreamVideo
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.model.User
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StreamVideoModule {

    @Provides
    @Singleton
    fun provideStreamVideo(@ApplicationContext context: Context): StreamVideo {
        val apiKey = Constants.MEET_API_KEY
        val userId = "stream"
        val token = StreamVideo.devToken(userId)

        return StreamVideoBuilder(
            context = context,
            apiKey = apiKey,
            token = token,
            user = User(
                id = userId,
                name = "streamUser",
                image = "http://placekitten.com/200/300",
                role = "admin",
                custom = mapOf("email" to userId),
            ),
        ).build()
    }
}
