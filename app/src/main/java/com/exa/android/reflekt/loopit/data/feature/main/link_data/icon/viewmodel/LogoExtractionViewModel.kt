package com.exa.android.reflekt.loopit.data.feature.main.link_data.icon.viewmodel

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.feature.main.link_data.icon.api.BrandftechAPI
import com.exa.android.reflekt.loopit.data.feature.main.link_data.icon.api.BrandfetchResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class BrandftechViewModel @Inject constructor(
    private val api: BrandftechAPI,
    @ApplicationContext private val context: Context
) : ViewModel() {

    suspend fun fetchBrandInfoSingle(domain: String): BrandfetchResponse? {
        return try {
            val response = api.getBrandInfo(
                domain = domain,
                apiKey = ContextCompat.getString(context, R.string.BRANDFTECH_API_KEY)
            )
            if (response.isSuccessful) {
                response.body()
            } else {
                // Log.e("Brandfetch", "Error response: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            //Log.e("Brandfetch", "Exception: ${e.localizedMessage}", e)
            null
        }
    }
}
