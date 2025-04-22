package com.exa.android.reflekt.loopit.presentation.main.Home.component

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.exa.android.reflekt.R

@Composable
fun ImageUsingCoil(
    context: Context,
    imageUrl: String?,
    placeholder: Int,
    modifier: Modifier = Modifier,
    errorImage: Int = R.drawable.placeholder
) {
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .placeholder(placeholder) // shown while loading
            .error(errorImage) // shown on load failure
            .build(),
        contentDescription = "Image message",
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}

@Composable
fun ImageFromUri(uri: Uri, modifier: Modifier = Modifier) {
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(uri)
            .error(R.drawable.placeholder)
            .crossfade(true)
            .build()
    )

    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop // Or Fit, FillBounds etc.
    )
}

@Composable
fun TrackableImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val imageLoader = ImageLoader.Builder(context)
        .components {
            add(SvgDecoder.Factory())
        }
        .build()

    val request = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build()
    }

    var isValidImage by remember { mutableStateOf(true) }

    if (!isValidImage) {
        Image(
            painter = painterResource(id = R.drawable.htmx_ic),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
        return
    }

    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        modifier = modifier,
        imageLoader = imageLoader,
        contentScale = ContentScale.Crop,
        onState = {
            when (it) {
                is AsyncImagePainter.State.Error -> {
                    Log.e("TrackableImage", "Failed to load: ${it.result.throwable}")
                    isValidImage = false
                }

                is AsyncImagePainter.State.Success -> {
                    val drawable = it.result.drawable
                    val width = drawable.intrinsicWidth
                    val height = drawable.intrinsicHeight
                    Log.d("TrackableImage", "Loaded image with size: ${width}x$height")

                    // Detect blank SVGs (0 or 1 px typically)
                    if (width <= 1 || height <= 1) {
                        isValidImage = false
                    }
                }

                is AsyncImagePainter.State.Loading -> {
                    Log.d("TrackableImage", "Loading image: $imageUrl")
                }

                else -> {}
            }
        }
    )

//    if (error) {
//        Text("⚠️ Failed to load image", modifier = Modifier.padding(4.dp))
//    }
}
