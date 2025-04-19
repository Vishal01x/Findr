package com.exa.android.reflekt.loopit.presentation.main.Home.component

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.exa.android.reflekt.R

@Composable
fun ImageUsingCoil(context : Context, imageUrl : String?, placeholder : Int, modifier: Modifier){
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .placeholder(R.drawable.placeholder) // shown while loading
            //.error(R.drawable.chat_img3) // shown on load failure
            .build(),
        contentDescription = "Image message",
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
