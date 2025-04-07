package com.exa.android.reflekt.loopit.presentation.main.ankit


//// Extension property to create DataStore instance
//private val Context.dataStore by preferencesDataStore(name = "app_preferences")
//
//@Singleton
//class AppPreferences @Inject constructor(
//    @ApplicationContext context: Context
//) {
//    private val dataStore = context.dataStore
//
//    companion object {
//        val USER_KEY = stringPreferencesKey("user_data")
//    }
//
//    suspend fun saveUser(user: String) {
//        dataStore.edit { preferences ->
//            preferences[USER_KEY] = user
//        }
//    }
//
//    suspend fun getUser(): String? {
//        return dataStore.data.map { preferences ->
//            preferences[USER_KEY]
//        }.firstOrNull()
//    }
//}

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
}


