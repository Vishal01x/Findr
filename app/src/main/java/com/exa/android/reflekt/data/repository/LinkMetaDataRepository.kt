package com.exa.android.reflekt.data.repository

import com.exa.android.reflekt.data.local.LinkMetadataDao
import com.exa.android.reflekt.domain.LinkMetadata
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URL
import javax.inject.Inject

class LinkMetadataRepository @Inject constructor(
    private val dao: LinkMetadataDao,
    private val dispatcher: CoroutineDispatcher
) {
    private val cache = mutableMapOf<String, LinkMetadata>()

    suspend fun getMetadata(url: String): Result<LinkMetadata> = withContext(dispatcher) {
        // Check in-memory cache
        cache[url]?.let { return@withContext Result.success(it) }

        // Check database cache
        dao.get(url)?.let {
            cache[url] = it
            return@withContext Result.success(it)
        }

        // Fetch from network
        return@withContext try {
            val metadata = fetchFromNetwork(url)
            dao.insert(metadata)
            cache[url] = metadata
            Result.success(metadata)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchFromNetwork(url: String): LinkMetadata {
        val doc = Jsoup.connect(url)
            .timeout(10_000)
            .get()

        return LinkMetadata(
            url = url,
            title = doc.selectFirst("meta[property=og:title]")?.attr("content")
                ?: doc.title(),
            description = doc.selectFirst("meta[property=og:description]")?.attr("content")
                ?: doc.selectFirst("meta[name=description]")?.attr("content"),
            imageUrl = doc.selectFirst("meta[property=og:image]")?.attr("content"),
            domain = URL(url).host?.removePrefix("www.") ?: ""
        )
    }
}