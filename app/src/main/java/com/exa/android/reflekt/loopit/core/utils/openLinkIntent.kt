package com.exa.android.reflekt.loopit.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Patterns

fun openLinkIntent(url : String, context : Context){
    try {
        if (Patterns.EMAIL_ADDRESS.matcher(url).matches()) {
            // It's a valid email
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${url}")
            }
            context.startActivity(intent)

        } else if (Patterns.WEB_URL.matcher(url).matches()) {
            // It's a valid URL
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } else {
            showToast(context, "Invalid link or email")
        }

    } catch (e: Exception) {
        e.printStackTrace()
        showToast(context, "Cannot open link")
    }
}
