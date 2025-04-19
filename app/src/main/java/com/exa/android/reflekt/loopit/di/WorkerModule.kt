package com.exa.android.reflekt.loopit.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.exa.android.reflekt.loopit.data.remote.main.Repository.MediaSharingRepository
import com.exa.android.reflekt.loopit.data.remote.main.worker.MediaUploadWorker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
}
