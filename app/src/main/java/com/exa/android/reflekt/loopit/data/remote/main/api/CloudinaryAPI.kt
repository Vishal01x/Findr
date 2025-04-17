package com.exa.android.reflekt.loopit.data.remote.main.api

import com.exa.android.reflekt.loopit.util.Constants.CLOUDINARY_NAME
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

// Cloudinary response data
data class CloudinaryUploadResponse(
    val secure_url: String
)

// Cloudinary Retrofit API
interface CloudinaryApi {
    @Multipart
    @POST("v1_1/${CLOUDINARY_NAME}/raw/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("upload_preset") uploadPreset: RequestBody
    ): CloudinaryUploadResponse
}