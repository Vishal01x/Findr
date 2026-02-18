package com.exa.android.reflekt.loopit.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

/**
 * Utility object for sharing posts with deep linking support
 */
object ShareUtils {
    
    /**
     * Share a post with its title, first image (if available), and deep link
     * 
     * @param context Android context
     * @param post The post to share
     */
    suspend fun sharePost(context: Context, post: Post) = withContext(Dispatchers.IO) {
        // Use HTTP scheme for clickable links
        val deepLink = "https://findr.app/post/${post.id}"
        val shareText = buildShareText(post, deepLink)
        
        // If post has images, share with the first image
        if (post.imageUrls.isNotEmpty()) {
            shareWithImage(context, post.imageUrls.first(), shareText)
        } else {
            // Share text only
            shareTextOnly(context, shareText)
        }
    }
    
    /**
     * Build the share text content
     */
    private fun buildShareText(post: Post, deepLink: String): String {
        return buildString {
            if (post.title.isNotBlank()) {
                appendLine(post.title)
                appendLine()
            }
            appendLine("Check out this post on Findr!")
            appendLine(deepLink)
        }
    }
    
    /**
     * Share text only without image
     */
    private fun shareTextOnly(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        
        val chooser = Intent.createChooser(intent, "Share Post")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
    
    /**
     * Share with image from URL
     * Downloads the image temporarily and shares it using FileProvider
     */
    private suspend fun shareWithImage(context: Context, imageUrl: String, text: String) {
        try {
            // Download image to cache directory
            val imageFile = downloadImageToCache(context, imageUrl)
            
            if (imageFile != null && imageFile.exists()) {
                // Get URI using FileProvider
                val imageUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    imageFile
                )
                
                // Create share intent with image
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    putExtra(Intent.EXTRA_TEXT, text)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                val chooser = Intent.createChooser(intent, "Share Post")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                
                withContext(Dispatchers.Main) {
                    context.startActivity(chooser)
                }
            } else {
                // Fallback to text only if image download fails
                withContext(Dispatchers.Main) {
                    shareTextOnly(context, text)
                }
            }
        } catch (e: Exception) {
            // Fallback to text only on error
            withContext(Dispatchers.Main) {
                shareTextOnly(context, text)
            }
        }
    }
    
    /**
     * Download image from URL to cache directory
     */
    private suspend fun downloadImageToCache(context: Context, imageUrl: String): File? {
        return try {
            val url = URL(imageUrl)
            val fileName = "share_${System.currentTimeMillis()}.jpg"
            val file = File(context.cacheDir, fileName)
            
            url.openStream().use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
