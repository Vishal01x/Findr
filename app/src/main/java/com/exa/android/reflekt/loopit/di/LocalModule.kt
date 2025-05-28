package com.exa.android.reflekt.loopit.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.exa.android.reflekt.loopit.data.local.LinkMetadataDatabase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
class LocalModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context : Context): LinkMetadataDatabase {
        return  Room.databaseBuilder(
            context,
            LinkMetadataDatabase::class.java, "metadata-db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideMetadataDao(database: LinkMetadataDatabase) = database.metadataDao()

    @Provides
    fun provideCoroutineDispatcher(): CoroutineDispatcher = Dispatchers.IO


    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context : Context) : FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    private  val DATASTORE_NAME = "user_preferences.preferences_pb"

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = {
                context.dataStoreFile(DATASTORE_NAME)
            }
        )
    }
}