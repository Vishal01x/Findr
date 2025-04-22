package com.exa.android.reflekt.loopit.di

import android.app.Application
import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.exa.android.reflekt.loopit.data.remote.main.Repository.LocationRepository
import com.exa.android.reflekt.loopit.data.remote.main.Repository.MediaSharingRepository
import com.exa.android.reflekt.loopit.data.remote.main.worker.MediaUploadWorker
import com.exa.android.reflekt.loopit.data.remote.main.worker.PreferenceHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)  // Ensure this is installed in the Singleton component
object WorkerFactoryModule {

    @Provides
    @Singleton
    fun provideWorkerFactory(
        mediaSharingRepository: MediaSharingRepository
    ): WorkerFactory {
        return object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,



                workerParameters: WorkerParameters
            ): ListenableWorker? {
                return when (workerClassName) {
                    MediaUploadWorker::class.java.name -> {
                        MediaUploadWorker(appContext, workerParameters, mediaSharingRepository)
                    }
                    else -> null
                }
            }
        }
    }
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun providePreferenceHelper(@ApplicationContext context: Context): PreferenceHelper {
        return PreferenceHelper(context)
    }

    @Provides
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }


}
