package com.exa.android.reflekt.loopit.data.remote.main.api
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://fcm.googleapis.com/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: FCMApiService by lazy {
        retrofit.create(FCMApiService::class.java)
    }
}
