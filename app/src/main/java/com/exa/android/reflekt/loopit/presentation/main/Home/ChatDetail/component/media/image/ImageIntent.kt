package com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.image

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast

//fun openImageIntent(context: Context, imageUrl: String) {
//    val intent = Intent(Intent.ACTION_VIEW).apply {
//        setDataAndType(Uri.parse(imageUrl), "image/*")
//        flags = Intent.FLAG_ACTIVITY_NEW_TASK
//    }
//    context.startActivity(intent)
//}

fun openImageIntent(context: Context, imageUrl: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(imageUrl), "image/*")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "No app found to view image", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Log.e("IntentError", "Error opening image: ${e.message}")
        Toast.makeText(context, "Unable to open image", Toast.LENGTH_SHORT).show()
    }
}
