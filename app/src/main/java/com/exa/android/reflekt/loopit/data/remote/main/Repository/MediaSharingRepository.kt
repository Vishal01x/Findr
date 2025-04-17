package com.exa.android.reflekt.loopit.data.remote.main.Repository

import android.content.Context
import android.util.Log
import com.exa.android.reflekt.loopit.data.remote.main.api.CloudinaryApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class MediaSharingRepository @Inject constructor(
    private val db: FirestoreService,
    private val cloudinaryApi: CloudinaryApi,
    @ApplicationContext private val context : Context
) {


    suspend fun uploadFileToCloudinary(file: File): String? = withContext(Dispatchers.IO) {
        Log.d("Storage Cloudinary", "Preparing file for upload: ${file.name}")

//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://api.cloudinary.com/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        val api = retrofit.create(CloudinaryApi::class.java)

        val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val uploadPreset = "media_uploads".toRequestBody("text/plain".toMediaTypeOrNull())

        return@withContext try {
            val response = cloudinaryApi.uploadFile(body, uploadPreset)
            Log.d("Storage Cloudinary", "Upload successful: ${response.secure_url}")
            response.secure_url
        } catch (e: Exception) {
            Log.d("Storage Cloudinary", "Upload failed")
            Log.e("Storage Cloudinary", "Upload failed", e)
            null
        }
    }
}
