package com.exa.android.reflekt.loopit.fcm
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.exa.android.reflekt.MainActivity
import com.exa.android.reflekt.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationHelper @Inject constructor(@ApplicationContext private val context: Context) {



    fun showNotification(data: NotificationData) {
        Log.d("FireStore Operation", "notification received - $data")
        val notificationId = System.currentTimeMillis().toInt()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context, data.type.channelId)
            .setContentTitle(data.title)
            .setContentText(data.body)
            .setSmallIcon(R.drawable.findr_logo)
            .setAutoCancel(true)
            .setContentIntent(createDeepLinkIntent(data.deepLink))

        data.largeIcon?.let {
            builder.setLargeIcon(loadBitmapFromUri(it))
        }

        data.groupKey?.let {
            builder.setGroup(it)
        }

        data.actions.forEach { action ->
            builder.addAction(createAction(action, data.deepLink))
        }

        manager.notify(notificationId, builder.build())
    }

    private fun createDeepLinkIntent(uri: Uri): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = uri
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createAction(action: NotificationAction, deepLink: Uri): NotificationCompat.Action {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            putExtra("deep_link", deepLink.toString())
            putExtra("action_id", action.actionId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return if (action.requiresInput) {
            val remoteInput = RemoteInput.Builder("response")
                .setLabel(action.title)
                .build()

            NotificationCompat.Action.Builder(
                action.iconResId,
                action.title,
                pendingIntent
            ).addRemoteInput(remoteInput).build()
        } else {
            NotificationCompat.Action.Builder(
                action.iconResId,
                action.title,
                pendingIntent
            ).build()
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        // Implement your bitmap loading logic
        return BitmapFactory.decodeFile(uri.path)
    }
}