package com.exa.android.reflekt.loopit.di

import com.exa.android.reflekt.loopit.data.feature.authentication.repo.AuthRepository
import com.exa.android.reflekt.loopit.data.feature.authentication.repo.AuthRepositoryImpl
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.repository.ChatRepository
import com.exa.android.reflekt.loopit.data.feature.main.profile.data.repository.ProfileRepository
import com.exa.android.reflekt.loopit.data.feature.main.link_data.icon.api.BrandftechAPI
import com.exa.android.reflekt.loopit.data.feature.main.media.api.CloudinaryApi
import com.exa.android.reflekt.loopit.util.Constants.BRANDFTECH_BASE_URL
import com.exa.android.reflekt.loopit.util.Constants.CLOUDINARY_BASE_URL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val db = FirebaseFirestore.getInstance()
        // Enable offline persistence with 100 MB cache.
        // Placement data will be served from disk on repeat visits,
        // eliminating unnecessary network round-trips.
        val cacheSettings = PersistentCacheSettings.newBuilder()
            .setSizeBytes(100L * 1024L * 1024L) // 100 MB
            .build()
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(cacheSettings)
            .build()
        return db
    }

    @Provides
    @Singleton
    fun provideRealTimeFirebase() = FirebaseDatabase.getInstance()

    @Provides
    fun provideFirebaseMessaging() = FirebaseMessaging.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage() = FirebaseStorage.getInstance()


    @Provides
    @Singleton
    fun provideProfileRepository(firestore: FirebaseFirestore): ProfileRepository {
        return ProfileRepository(firestore)
    }

    @Provides
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        firestoreService: ChatRepository
    ): AuthRepository = AuthRepositoryImpl(auth, firestore, firestoreService)

    @Provides
    @Singleton
    fun providesCloudinaryApi(): CloudinaryApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(CLOUDINARY_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(CloudinaryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBrandfetchApi(): BrandftechAPI {
        return Retrofit.Builder()
            .baseUrl(BRANDFTECH_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BrandftechAPI::class.java)
    }
}


