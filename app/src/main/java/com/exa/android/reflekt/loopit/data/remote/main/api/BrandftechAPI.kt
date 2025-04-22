package com.exa.android.reflekt.loopit.data.remote.main.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

// --- Data Classes ---
data class BrandfetchResponse(
    val name: String,
    val domain: String,
    val logos: List<BrandLogo>
)

data class BrandLogo(
    val type: String,
    val theme: String,
    val formats: List<LogoFormat>
)

data class LogoFormat(
    val src: String,
    val background: String,
    val size: String
)

// --- Retrofit Service ---
interface BrandftechAPI {
    @GET("v2/brands/{domain}")
    suspend fun getBrandInfo(
        @Path("domain") domain: String,
        @Header("Authorization") apiKey: String
    ): Response<BrandfetchResponse>
}

