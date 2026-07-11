package com.exa.android.reflekt.loopit.core.utils

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.File
import kotlin.random.Random
import java.text.SimpleDateFormat
import java.util.*

//fun showToast(context : Context, message : String){
//    Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
//}

fun showToast(context: Context, message: String) {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}


@Composable
fun AudioWaveForm(
    modifier: Modifier = Modifier,
    isPaused: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition()
    val amplitudeAnimation by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val waveAmplitudes = remember {
        List(10) { Random.nextFloat() } // Mock random wave amplitudes
    }

    Canvas(
        modifier = modifier
        .fillMaxWidth()
        .height(40.dp)) {
        val waveWidth = size.width / waveAmplitudes.size
        val centerY = size.height / 2

        waveAmplitudes.forEachIndexed { index, amplitude ->
            val barHeight = if (isPaused) 0f else centerY * amplitude * amplitudeAnimation
            drawRoundRect(
                color = Color.Gray,
                topLeft = Offset(
                    x = index * waveWidth + waveWidth / 4,
                    y = centerY - barHeight / 2
                ),
                size = Size(waveWidth / 2, barHeight),
                cornerRadius = CornerRadius(x = 4.dp.toPx(), y = 4.dp.toPx())
            )
        }
    }
}



fun formatTimestamp(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    val todayStart = calendar.apply {
        timeInMillis = currentTime
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val yesterdayStart = todayStart - 24 * 60 * 60 * 1000

    return when {
        timestamp >= todayStart -> {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            timeFormat.format(Date(timestamp))
        }
        timestamp >= yesterdayStart -> "Yesterday"
        else -> {
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}

fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val mins = seconds / 60
    val hours = mins / 60
    val days = hours / 24
    val weeks = days / 7
    val months = days / 30
    val years = days / 365

    return when {
        years >= 1  -> if (years == 1L) "1 year ago" else "$years years ago"
        months >= 1 -> if (months == 1L) "1 month ago" else "$months months ago"
        weeks >= 1  -> if (weeks == 1L) "1 week ago" else "$weeks weeks ago"
        days >= 1   -> "${days}d ago"
        hours >= 1  -> "${hours}h ago"
        mins >= 1   -> "${mins}m ago"
        else -> "Just now"
    }
}

/** Always returns just the time part (hh:mm a), used inside message bubbles where
 *  the date is already shown by the [DateSeparator]. */
fun formatTimeOnly(timestamp: Long): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

fun Context.createImageFile(): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        externalCacheDir      /* directory */
    )
    return image
}

fun generateChatId(user1: String, user2: String): String {
    return if (user1 > user2) "$user1-$user2" else "$user2-$user1"
}


fun getVibrator(context : Context) : Vibrator? {
    val vibratorManager =
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        }else{
            null
        }

    return  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
        vibratorManager ?. defaultVibrator
    }else{
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
}