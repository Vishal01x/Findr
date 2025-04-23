package com.exa.android.reflekt.loopit.data.remote.main.Repository

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.remote.main.api.BrandftechAPI
import com.exa.android.reflekt.loopit.data.remote.main.api.BrandfetchResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrandfetchViewModel @Inject constructor(
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
