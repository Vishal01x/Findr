package com.exa.android.reflekt.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import com.exa.android.reflekt.R
import java.net.URL

object SafeLinkLauncher {
    fun launchUrl(
        context: Context,
        url: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        try {
            val parsedUrl = URL(url).toString()

            CustomTabsIntent.Builder()
                //.setToolbarColor(MaterialTheme.colorScheme.primary.toArgb())
                .setShowTitle(true)
                .build()
                .launchUrl(context, Uri.parse(parsedUrl))

            onSuccess()
        } catch (e: Exception) {
            //onError(context.getString(R.string.invalid_url))
        }
    }
}