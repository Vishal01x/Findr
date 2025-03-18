package com.exa.android.reflekt.di

import android.content.Context
import androidx.room.Room
import com.exa.android.reflekt.data.local.LinkMetadataDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context : Context): LinkMetadataDatabase {
        return  Room.databaseBuilder(
            context,
            LinkMetadataDatabase::class.java, "metadata-db"
        ).build()
    }

    @Provides
    fun provideMetadataDao(database: LinkMetadataDatabase) = database.metadataDao()

    @Provides
    fun provideCoroutineDispatcher(): CoroutineDispatcher = Dispatchers.IO
}