package com.exa.android.reflekt.loopit.data.feature.main.link_data.icon.viewmodel

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.feature.main.link_data.icon.CompanyLogoUtils
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

    private val logoCache = mutableMapOf<String, String?>()

    suspend fun fetchBrandInfoSingle(domain: String): BrandfetchResponse? {
        return try {
            val response = api.getBrandInfo(
                domain = domain,
                apiKey = ContextCompat.getString(context, R.string.BRANDFTECH_API_KEY)
            )
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    /** Resolves the best available logo URL for a company name.
     *  Tries Brandfetch (domain-derived), then falls back to Clearbit. */
    suspend fun resolveLogoUrl(companyName: String): String? {
        if (companyName.isBlank()) return null
        if (logoCache.containsKey(companyName)) return logoCache[companyName]

        val domain = CompanyLogoUtils.companyNameToDomain(companyName)

        val brandfetchUrl = try {
            val apiKey = ContextCompat.getString(context, R.string.BRANDFTECH_API_KEY)
            val response = api.getBrandInfo(domain, apiKey)
            if (response.isSuccessful) {
                val logos = response.body()?.logos ?: emptyList()
                logos.asSequence()
                    .sortedBy { if (it.type == "icon") 0 else 1 }
                    .flatMap { it.formats.asSequence() }
                    .firstOrNull { it.src.isNotBlank() }?.src
            } else null
        } catch (e: Exception) { null }

        val resolved = brandfetchUrl ?: CompanyLogoUtils.faviconLogoUrl(domain)
        logoCache[companyName] = resolved
        return resolved
    }
}
