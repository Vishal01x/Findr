package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import android.content.Context
import android.net.Uri
import com.exa.android.reflekt.loopit.data.feature.main.media.repository.MediaSharingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap

/**
 * Use case for uploading media (images and files) to Cloudinary
 * Handles parallel uploads for better performance
 */
class UploadMediaUseCase @Inject constructor(
    private val mediaSharingRepository: MediaSharingRepository,
    @ApplicationContext private val context: Context
) {
    /**
     * Uploads multiple images in parallel to Cloudinary
     * @param imageUris List of image URIs to upload
     * @param postId The post ID for organizing uploads (not used with Cloudinary)
     * @return Result with list of download URLs or error
     */
    suspend fun uploadImages(
        imageUris: List<Uri>,
        postId: String
    ): Result<List<String>> = coroutineScope {
        try {
            val uploadJobs = imageUris.map { uri ->
                async {
                    uploadSingleFile(uri)
                }
            }
            
            val results = uploadJobs.awaitAll()
            
            // Check if any upload failed
            val failures = results.filter { it.isFailure }
            if (failures.isNotEmpty()) {
                return@coroutineScope Result.failure(
                    failures.first().exceptionOrNull() ?: Exception("Image upload failed")
                )
            }
            
            // Extract all URLs
            val urls = results.mapNotNull { it.getOrNull() }
            Result.success(urls)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to upload images: ${e.message}"))
        }
    }
    
    /**
     * Uploads multiple files in parallel to Cloudinary
     * @param fileUris List of pairs (URI, filename) to upload
     * @param postId The post ID for organizing uploads (not used with Cloudinary)
     * @return Result with list of download URLs or error
     */
    suspend fun uploadFiles(
        fileUris: List<Pair<Uri, String>>,
        postId: String
    ): Result<List<String>> = coroutineScope {
        try {
            val uploadJobs = fileUris.map { (uri, _) ->
                async {
                    uploadSingleFile(uri)
                }
            }
            
            val results = uploadJobs.awaitAll()
            
            // Check if any upload failed
            val failures = results.filter { it.isFailure }
            if (failures.isNotEmpty()) {
                return@coroutineScope Result.failure(
                    failures.first().exceptionOrNull() ?: Exception("File upload failed")
                )
            }
            
            // Extract all URLs
            val urls = results.mapNotNull { it.getOrNull() }
            Result.success(urls)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to upload files: ${e.message}"))
        }
    }
    
    /**
     * Uploads a single file to Cloudinary
     * @param uri The URI of the file to upload
     * @return Result with download URL or error
     */
    private suspend fun uploadSingleFile(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Create temp file from URI
            val tempFile = createTempFileFromUri(uri)
            
            // Upload to Cloudinary
            val uploadedUrl = mediaSharingRepository.uploadFileToCloudinary(tempFile)
            
            // Clean up temp file
            tempFile.delete()
            
            if (uploadedUrl != null) {
                Result.success(uploadedUrl)
            } else {
                Result.failure(Exception("Upload returned null URL"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to upload file: ${e.message}"))
        }
    }
    
    /**
     * Creates a temporary file from a URI
     * @param uri The URI to convert to a file
     * @return The created temporary file
     */
    private suspend fun createTempFileFromUri(uri: Uri): File = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        var fileName = "upload_file"
        
        // Try to extract the original file name
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex)
            }
        }
        
        // Use file extension if not in original file name
        val fileExtension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(contentResolver.getType(uri)) ?: "tmp"
        
        if (!fileName.contains(".")) {
            fileName += ".$fileExtension"
        }
        
        // Create a new file in cache directory
        val file = File(context.cacheDir, fileName)
        
        // Copy the URI content to the new file
        contentResolver.openInputStream(uri)?.use { inputStream ->
            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }
        }
        
        file
    }
}

