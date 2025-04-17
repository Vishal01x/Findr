package com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.image

import android.content.Context
import android.content.Intent
import android.net.Uri

fun openImageIntent(context: Context, imageUrl: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(Uri.parse(imageUrl), "image/*")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}
