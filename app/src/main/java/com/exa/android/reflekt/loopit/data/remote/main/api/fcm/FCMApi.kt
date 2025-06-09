package com.exa.android.reflekt.loopit.data.remote.main.api.fcm

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.Headers


//interface FCMApiService {
//    //@Headers("Content-Type: application/json")
//    @POST("v1/projects/social-media-ab0cc/messages:send")
//    fun sendNotification(
//        @Header("Authorization") authToken: String,
//        @Body request: FCMRequest
//    ): Call<FCMResponse>
//}

interface FcmApiService {
    //@Headers("Content-Type: application/json")
    @POST("v1/projects/social-media-ab0cc/messages:send")
    fun sendNotification(
        @Header("Authorization") authToken: String,
        @Body request: FcmRequest
    ): Call<FcmResponse>

    companion object {
        const val BASE_URL = "https://fcm.googleapis.com/"
    }
}