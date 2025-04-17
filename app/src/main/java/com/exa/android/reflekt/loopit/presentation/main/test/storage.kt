//package com.exa.android.reflekt.loopit.presentation.main.test
//
//import android.net.Uri
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.lifecycle.ViewModel
//import coil.compose.AsyncImage
//import coil.request.ImageRequest
//import com.exa.android.reflekt.R
//import com.google.firebase.Timestamp
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.Query
//import kotlinx.coroutines.flow.flow
//import kotlinx.coroutines.tasks.await
//
//suspend fun uploadFile(fileUri: Uri): String {
//    val fileBytes = context.contentResolver.openInputStream(fileUri)?.use { it.readBytes() }
//        ?: throw Exception("Could not read file")
//
//    val mimeType = context.contentResolver.getType(fileUri)
//        ?: "application/octet-stream"
//
//    supabase.storage["bucket-name"].upload(
//        path = "folder/${UUID.randomUUID()}", // Unique path
//        data = fileBytes,
//        upsert = true,
//        contentType = mimeType
//    )
//
//    return supabase.storage["bucket-name].publicUrl("folder/${fileName}")
//}
//
//// Data class
//data class MediaDocument(
//    val url: String,
//    val timestamp: Timestamp = Timestamp.now()
//)
//
//// Save function
//suspend fun saveMediaUrl(url: String) {
//    val db = FirebaseFirestore.getInstance()
//    val mediaDoc = MediaDocument(url)
//
//    db.collection("media")
//        .add(mediaDoc)
//        .await()
//}
//
//
//fun getMediaUrls(viewModel: ViewModel) = flow {
//    val db = FirebaseFirestore.getInstance()
//
//    db.collection("media")
//        .orderBy("timestamp", Query.Direction.DESCENDING)
//        .addSnapshotListener { snapshot, error ->
//            if (error != null) return@addSnapshotListener
//            val urls = snapshot?.toObjects(MediaDocument::class.java) ?: emptyList()
//            emit(urls)
//        }
//}
//
//
//@Composable
//fun MediaItem(url: String) {
//    AsyncImage(
//        model = ImageRequest.Builder(LocalContext.current)
//            .data(url)
//            .crossfade(true)
//            .build(),
//        contentDescription = null,
//        modifier = Modifier.fillMaxWidth(),
//        contentScale = ContentScale.Crop,
//        placeholder = painterResource(R.drawable.chat_img3),
//        error = painterResource(R.drawable.ic_launcher_foreground)
//    )
//}
//
//// In ViewModel
//fun uploadAndStore(fileUri: Uri) = viewModelScope.launch {
//    try {
//        val url = uploadFile(fileUri)
//        saveMediaUrl(url)
//    } catch (e: Exception) {
//        // Handle error
//    }
//}
//
//// In Composable
//val mediaUrls by viewModel.mediaUrls.collectAsState(initial = emptyList())
//
//LazyColumn {
//    items(mediaUrls) { media ->
//        MediaItem(media.url)
//    }
//}

package com.exa.android.reflekt.loopit.presentation.main.test

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.exa.android.reflekt.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class MediaDocument(
    val url: String,
    val timestamp: Timestamp = Timestamp.now()
)

object SupabaseClient {
    private const val URL = "YOUR_SUPABASE_URL"
    private const val KEY = "YOUR_SUPABASE_KEY"
    private const val BUCKET = "bucket-name"

    val client = createSupabaseClient(URL, KEY) { install(Storage) }
    val storage = client.storage[BUCKET]
}

class MediaViewModell : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _mediaUrls = mutableStateOf<List<MediaDocument>>(emptyList())
    val mediaUrls: State<List<MediaDocument>> = _mediaUrls

    init {
        viewModelScope.launch {
            fetchMediaUrls().collect { _mediaUrls.value = it }
        }
    }

    fun uploadAndStore(context: Context, fileUri: Uri) = viewModelScope.launch {
        try {
            val url = uploadFile(context, fileUri)
            saveMediaUrl(url)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun uploadFile(context: Context, fileUri: Uri): String {
        val fileBytes = context.contentResolver.openInputStream(fileUri)?.use { it.readBytes() }
            ?: throw Exception("Could not read file")

        val mimeType = context.contentResolver.getType(fileUri) ?: "application/octet-stream"
        val fileName = "${UUID.randomUUID()}"
        val path = "folder/$fileName"

        SupabaseClient.storage.upload(
            path = path,
            data = fileBytes,
            upsert = true,
            contentType = mimeType
        )



        return SupabaseClient.storage.publicUrl(path)
    }

    private suspend fun saveMediaUrl(url: String) {
        db.collection("media").add(MediaDocument(url)).await()
    }

    private fun fetchMediaUrls(): Flow<List<MediaDocument>> = callbackFlow {
        val listener = db.collection("media")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                     // optionally close the flow on error
                    return@addSnapshotListener
                }

                trySend(snapshot?.toObjects(MediaDocument::class.java) ?: emptyList())
            }

        // Remove listener when the flow collector is cancelled
        awaitClose { listener.remove() }
    }

}

@Composable
fun MediaScreen() {
    val viewModel = viewModel<MediaViewModell>()
    val mediaUrls = viewModel.mediaUrls

    LazyColumn {
        items(mediaUrls.value) { media ->
            MediaItemm(media.url)
        }
    }
}

@Composable
fun MediaItemm(url: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = Modifier.fillMaxWidth(),
        contentScale = ContentScale.Crop,
        placeholder = painterResource(R.drawable.chat_img3),
        error = painterResource(R.drawable.ic_launcher_foreground)
    )
}