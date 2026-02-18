package com.exa.android.reflekt.loopit.core.utils

import com.google.firebase.Timestamp

fun formatTimestamp(timestamp: Timestamp?): String {
    val now = System.currentTimeMillis()
    if (timestamp == null) return "Just now"

    val diff = now - timestamp.toDate().time

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m"
        diff < 86400_000 -> "${diff / 3600_000}h"
        diff < 604800_000 -> "${diff / 86400_000}d"
        else -> "${diff / 604800_000}w"
    }
}
