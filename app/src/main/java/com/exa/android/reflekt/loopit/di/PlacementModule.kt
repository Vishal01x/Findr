package com.exa.android.reflekt.loopit.di

import com.exa.android.reflekt.loopit.data.feature.main.placement.data.datasource.PlacementDataSource
import com.exa.android.reflekt.loopit.data.feature.main.placement.data.repository.PlacementRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlacementModule {

    @Provides
    @Singleton
    fun providePlacementDataSource(
        firestore: FirebaseFirestore,
        auth: com.google.firebase.auth.FirebaseAuth
    ): PlacementDataSource {
        return PlacementDataSource(firestore, auth)
    }

    @Provides
    @Singleton
    fun providePlacementRepository(
        dataSource: PlacementDataSource
    ): PlacementRepository {
        return PlacementRepository(dataSource)
    }
}
