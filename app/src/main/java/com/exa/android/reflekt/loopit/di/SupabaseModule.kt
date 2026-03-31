package com.exa.android.reflekt.loopit.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://quyvvssisgmrwejnoipc.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF1eXZ2c3Npc2dtcndlam5vaXBjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njc5NjY2ODMsImV4cCI6MjA4MzU0MjY4M30.KuFZmj7F4jmVt2E3GwDXEiVysl6uXRFf5uaXOtQCNpc"
        ) {
            install(Postgrest)
        }
    }
}
