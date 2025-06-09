package com.exa.android.reflekt.loopit.di

import com.exa.android.reflekt.loopit.data.remote.authentication.repo.AuthRepository
import com.exa.android.reflekt.loopit.data.remote.authentication.repo.AuthRepositoryImpl
import com.exa.android.reflekt.loopit.data.remote.main.Repository.FirestoreService
import com.exa.android.reflekt.loopit.data.remote.main.Repository.ProfileRepository
import com.exa.android.reflekt.loopit.data.remote.main.api.BrandftechAPI
import com.exa.android.reflekt.loopit.data.remote.main.api.CloudinaryApi
import com.exa.android.reflekt.loopit.util.Constants.BRANDFTECH_BASE_URL
import com.exa.android.reflekt.loopit.util.Constants.CLOUDINARY_BASE_URL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
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
    fun provideFirebaseFirestore() = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideRealTimeFirebase() = FirebaseDatabase.getInstance()

    @Provides
    fun provideFirebaseMessaging() = FirebaseMessaging.getInstance()


    @Provides
    @Singleton
    fun provideProfileRepository(firestore: FirebaseFirestore): ProfileRepository {
        return ProfileRepository(firestore)
    }

    @Provides
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        firestoreService: FirestoreService
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


