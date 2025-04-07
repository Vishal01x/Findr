package com.exa.android.reflekt.loopit.presentation.main.test

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage

sealed class Resource<out T> {
    object Empty : Resource<Nothing>()
    object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}

@Singleton
class MediaRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) {
    suspend fun uploadMedia(uri: Uri, type: String): String? {
        return try {
            val cleanName = uri.lastPathSegment?.replace("[:\\s]".toRegex(), "_") ?: "file"
            val path = "uploads/${System.currentTimeMillis()}_$cleanName"
            val bucket = supabase.storage.from("media-store")
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.readBytes()?.let { bucket.upload(path, it) }
            val mediaUrl = bucket.publicUrl(path)
            firestore.collection("media").add(mapOf("url" to mediaUrl, "type" to type))
            mediaUrl
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAllMedia(): List<String> {
        val mediaList = mutableListOf<String>()
        firestore.collection("media").get().addOnSuccessListener { result ->
            for (document in result) {
                document.getString("url")?.let { mediaList.add(it) }
            }
        }.await()
        return mediaList
    }
}

@HiltViewModel
class MediaViewModel @Inject constructor(private val repository: MediaRepository) : ViewModel() {
    private val _uploadState = MutableStateFlow<Resource<String>>(Resource.Empty)
    val uploadState: StateFlow<Resource<String>> = _uploadState

    private val _mediaList = MutableStateFlow<List<String>>(emptyList())
    val mediaList: StateFlow<List<String>> = _mediaList

    fun uploadMedia(uri: Uri, type: String) {
        viewModelScope.launch {
            _uploadState.value = Resource.Loading
            val result = repository.uploadMedia(uri, type)
            _uploadState.value = if (result != null) Resource.Success(result) else Resource.Error("Upload failed")
        }
    }

    fun fetchMedia() {
        viewModelScope.launch {
            _mediaList.value = repository.getAllMedia()
        }
    }

    fun uploadMediaWithRetry(uri: Uri, type: String, retryCount: Int = 3) {
        viewModelScope.launch {
            repeat(retryCount) { attempt ->
                _uploadState.value = Resource.Loading
                val result = repository.uploadMedia(uri, type)
                if (result != null) {
                    _uploadState.value = Resource.Success(result)
                    return@launch
                }
                if (attempt == retryCount - 1) {
                    _uploadState.value = Resource.Error("Upload failed after retries")
                }
            }
        }
    }
}

@Composable
fun MediaScreen(viewModel: MediaViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val uploadState by viewModel.uploadState.collectAsState()
    val mediaList by viewModel.mediaList.collectAsState()

    val launcher = rememberLauncherForActivityResult(GetContent()) { uri ->
        uri?.let {
            val type = context.contentResolver.getType(it) ?: "image/*"
            viewModel.uploadMedia(uri = it, type = type)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { launcher.launch("*/*") }) {
            Text("Upload Media")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (uploadState) {
            is Resource.Loading -> CircularProgressIndicator()
            is Resource.Success -> Text("✅ Upload Successful!")
            is Resource.Error -> Text("❌ Upload Failed, please retry")
            else -> {}
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(mediaList) { url ->
                MediaItem(url)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchMedia()
    }
}

@Composable
fun MediaItem(url: String) {
    val context = LocalContext.current
    var isDownloaded by remember { mutableStateOf(false) }
    var localFileUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        val fileName = getFileNameFromUrl(url)
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        if (file.exists()) {
            isDownloaded = true
            localFileUri = Uri.fromFile(file)
        }
    }

    val painter = rememberAsyncImagePainter(localFileUri ?: url)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Image(
            painter = painter,
            contentDescription = "Media Preview",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val fileName = getFileNameFromUrl(url)
                downloadMedia(context, url, fileName) {
                    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                    if (file.exists()) {
                        isDownloaded = true
                        localFileUri = Uri.fromFile(file)
                    }
                }
            },
            enabled = !isDownloaded,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(if (isDownloaded) "Downloaded" else "Download")
        }
    }
}

fun getFileNameFromUrl(url: String): String {
    val fileName = Uri.parse(url).lastPathSegment ?: "downloaded_media"
    return fileName.replace("[:\\s]".toRegex(), "_")
}

fun downloadMedia(context: Context, url: String, fileName: String, onDownloaded: () -> Unit) {
    val request = DownloadManager.Request(Uri.parse(url))
        .setTitle("Downloading Media")
        .setDescription("Downloading file...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val downloadId = manager.enqueue(request)

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                onDownloaded()
                context.unregisterReceiver(this)
            }
        }
    }

    ContextCompat.registerReceiver(
        context,
        receiver,
        IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
        RECEIVER_NOT_EXPORTED
    )
}
