package com.exa.android.reflekt.loopit.fcm

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.exa.android.reflekt.MyApp
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.remote.main.Repository.FirestoreService
import com.exa.android.reflekt.loopit.util.Constants.FINDR_GROUP_KEY
import com.exa.android.reflekt.loopit.util.CurChatManager.activeChatId
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseService : FirebaseMessagingService() {

    @Inject
    lateinit var repository: FirestoreService

    private val notificationHelper by lazy { NotificationHelperr(applicationContext) }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        repository.updateToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        //Log.d("FCM", "Message recieved - ${message.data}")
        val data = message.data

        val typeString = message.data["type"]
        val notificationType = typeString?.let { NotificationType.fromChannelId(it) }
        // Log.d("FCM", "type = $notificationType, $typeString")
        notificationType?.let { type ->

            when (type) {
                NotificationType.CHAT_MESSAGE -> {
                    handleChatMessage(data, type.actions)
                }

                NotificationType.PROJECT_UPDATE -> {
                    handleProjectMessage(data, type.actions)
                }

                NotificationType.POST -> {
                    handlePostMessage(data, type.actions)
                }

                NotificationType.PROFILE -> {
                    handleProfileMessage(data, type.actions)
                }

                NotificationType.APP_UPDATE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.e("Post Notification", "Notification permission not granted!")
                            return
                        }
                    }

                    NotificationCompat.Builder(this, MyApp.CHANNEL_SYSTEM)
                        .setSmallIcon(R.drawable.findr_logo)
                        .setContentTitle("Action Failed")
                        .setContentText("New app update")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .build().also {
                            NotificationManagerCompat.from(this).notify(
                                UUID.randomUUID().hashCode(),
                                it
                            )
                        }
                }
            }

        }
    }

    private fun handleChatMessage(data: Map<String, String>, actions: List<NotificationAction>) {

        if (Firebase.auth.currentUser == null || Firebase.auth.currentUser?.uid == null) return

        val senderId = data["senderId"] ?: return
        if (senderId == Firebase.auth.currentUser?.uid) return
        // Log.d("FCM", "sending to show")
        val targetId = data["targetId"] ?: return
        // Log.d("FCM", "sending to show2")
        if (targetId == activeChatId) return
        // Log.d("FCM", "sending to show3")


        notificationHelper.showNotification(
            NotificationData(
                type = NotificationType.CHAT_MESSAGE,
                title = data["title"] ?: "User",
                body = data["body"] ?: "New Message",
                deepLink = Uri.parse("findr://chat/$senderId"),
                actions = actions,
                largeIcon = data["imageUrl"]?.let { Uri.parse(it) },
                groupKey = targetId,
                senderId = senderId,
                mediaUri = data["mediaUrl"]?.let { Uri.parse(it) },
                link = data["link"],
                fcm = data["fcm"]
            ),
            NotificationCompat.CATEGORY_MESSAGE
        )
    }

    private fun handleProjectMessage(data: Map<String, String>, actions: List<NotificationAction>) {

        if (Firebase.auth.currentUser == null || Firebase.auth.currentUser?.uid == null) return

        val projectId = data["targetId"] ?: return
        val senderId = data["senderId"] ?: ""
        if (senderId == Firebase.auth.currentUser?.uid) return

        notificationHelper.showNotification(
            NotificationData(
                type = NotificationType.PROJECT_UPDATE,
                title = data["title"] ?: "Project",
                body = data["body"] ?: "New project update",
                deepLink = Uri.parse("findr://project/$projectId"),
                actions = actions,
                largeIcon = data["imageUrl"]?.let { Uri.parse(it) },
                groupKey = projectId,
                senderId = senderId,
                mediaUri = data["mediaUrl"]?.let { Uri.parse(it) },
                link = data["link"],
                fcm = data["fcm"]
            ),
            NotificationCompat.CATEGORY_SOCIAL
        )
    }

    private fun handlePostMessage(data: Map<String, String>, actions: List<NotificationAction>) {

        if (Firebase.auth.currentUser == null || Firebase.auth.currentUser?.uid == null) return

        val projectId = data["targetId"] ?: return
        val senderId = data["senderId"] ?: ""
        if (senderId == Firebase.auth.currentUser?.uid) return

        notificationHelper.showNotification(
            NotificationData(
                type = NotificationType.POST,
                title = data["title"] ?: "Post",
                body = data["body"] ?: "New Post",
                deepLink = Uri.parse("findr://project/$projectId"),
                actions = actions,
                largeIcon = data["imageUrl"]?.let { Uri.parse(it) },
                groupKey = projectId,
                mediaUri = data["mediaUrl"]?.let { Uri.parse(it) },
                link = data["link"]
            ),
            NotificationCompat.CATEGORY_SOCIAL
        )
    }

    private fun handleProfileMessage(data: Map<String, String>, actions: List<NotificationAction>) {
        if (Firebase.auth.currentUser == null || Firebase.auth.currentUser?.uid == null) return

        val senderId = data["senderId"] ?: Firebase.auth.currentUser?.uid ?: ""
        val targetId = data["targetId"] ?: Firebase.auth.currentUser?.uid ?: ""
        val category = data["postcategory"] ?: ""

        //Log.d("FCM", "category - $category")

        val deepLink =
            if (category == "viewer") Uri.parse("findr://profile_views/$senderId") else Uri.parse("findr://profile/$senderId")

        notificationHelper.showNotification(
            NotificationData(
                type = NotificationType.PROFILE,
                title = data["title"] ?: "Profile Views",
                body = data["body"] ?: "Someone viewed your profile",
                deepLink = deepLink,
                actions = actions,
                largeIcon = data["imageUrl"]?.let { Uri.parse(it) },
                groupKey = targetId
            ),
            NotificationCompat.CATEGORY_SOCIAL
        )
    }
}

class NotificationHelpe(private val context: Context) {

    fun showNotification(data: NotificationData, category: String) {
        val notificationId = data.groupKey?.hashCode() ?: UUID.randomUUID().hashCode()
        val channelId = data.type.channelId

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("Post Notification", "Notification permission not granted!")
                return
            }
        }


        // Create notification channel if needed
        //createNotificationChannel(channelId, data.type.channelId)

        // Base intent setup
        val intent = Intent(Intent.ACTION_VIEW, data.deepLink).apply {
            setPackage(context.packageName)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build appropriate style
        val builder = when (data.type) {
            NotificationType.CHAT_MESSAGE -> buildChatStyle(data, pendingIntent)
            NotificationType.PROFILE -> buildProfileStyle(data, pendingIntent)
            NotificationType.POST -> buildPostStyle(data, pendingIntent)
            NotificationType.PROJECT_UPDATE -> buildProjectStyle(data, pendingIntent)
            else -> TODO()
        }.apply {
            setSmallIcon(R.drawable.findr_logo)
            setCategory(category)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setAutoCancel(true)
            setContentIntent(pendingIntent)
            setGroup(FINDR_GROUP_KEY)  // Set group key
            setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
        }

        // Add actions with proper PendingIntent flags
        addNotificationActions(builder, data)

        // Load and set large icon
        loadAndSetLargeIcon(data, builder, notificationId)


        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, builder.build())

    }

    private fun buildChatStyle(
        data: NotificationData,
        pendingIntent: PendingIntent
    ): NotificationCompat.Builder {
        val person = Person.Builder()
            .setName(data.title)
            /*.apply {
                data.largeIcon?.let { uri ->
                    try {
                        val bitmap = Glide.with(context)
                            .asBitmap()
                            .load(uri)
                            .submit(128, 128)
                            .get()
                        setIcon(IconCompat.createWithBitmap(bitmap))
                    } catch (e: Exception) {
                        Log.e("Notification", "Error loading avatar", e)
                    }
                }
            }*/
            .build()

        val sharedPreferences =
            context.getSharedPreferences("chat_notifications", Context.MODE_PRIVATE)
        val messagesSet =
            sharedPreferences.getStringSet(data.groupKey, mutableSetOf())!!.toMutableSet()
        messagesSet.add(data.body)
        sharedPreferences.edit().putStringSet(data.groupKey, messagesSet).apply()


        val messagingStyle = NotificationCompat.MessagingStyle(person)
            .setConversationTitle(data.title)

        messagesSet.forEach { msg ->
            messagingStyle.addMessage(msg, System.currentTimeMillis(), person)
        }

        return NotificationCompat.Builder(context, NotificationType.CHAT_MESSAGE.channelId)
            .setStyle(
                messagingStyle
            )
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
    }

    private fun buildPostStyle(
        data: NotificationData,
        pendingIntent: PendingIntent
    ): NotificationCompat.Builder {
        val bitmap = getLargeBitmap(data.mediaUri)

        return NotificationCompat.Builder(context, NotificationType.POST.channelId)
            .setContentTitle(data.title)
            .setContentText(data.body)
            .apply {
                if (bitmap != null) {
                    setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .setBigContentTitle(data.title)
                            .setSummaryText(data.body)
                    )
                    setLargeIcon(bitmap) // Show thumbnail in collapsed state
                } else {
                    setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(data.body)
                            .setBigContentTitle(data.title)
                    )
                }
            }
    }


    private fun getLargeBitmap(uri: Uri?): Bitmap? {
        if (uri == null) return null

        return try {
            // Using Glide for better image handling
            Glide.with(context)
                .asBitmap()
                .load(uri)
                .submit(512, 512) // Max dimensions for notification images
                .get() // Blocking call - should be called on background thread
        } catch (e: Exception) {
            Log.e("Notification", "Error loading large bitmap", e)
            null
        }
    }

    private fun buildProfileStyle(
        data: NotificationData,
        pendingIntent: PendingIntent
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NotificationType.PROFILE.channelId)
            .setContentTitle(data.title)
            .setContentText(data.body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(data.body)
                    .setBigContentTitle(data.title)
            )
    }

    private fun buildProjectStyle(
        data: NotificationData,
        pendingIntent: PendingIntent
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NotificationType.PROJECT_UPDATE.channelId)
            .setContentTitle(data.title)
            .setContentText(data.body)
            .setStyle(
                NotificationCompat.InboxStyle()
                    .addLine(data.body)
                    .setBigContentTitle(data.title)
                    .setSummaryText("Project update")
            )
    }

    private fun addNotificationActions(
        builder: NotificationCompat.Builder,
        data: NotificationData
    ) {
        data.actions.forEach { action ->
            val intent = Intent(action.actionId.actionId).apply {
                putExtra("groupKey", data.groupKey)
                putExtra("receiverFcm", data.fcm)
                putExtra("otherUser", data.senderId)
                `package` = context.packageName
            }

            val flags = if (action.requiresInput) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                UUID.randomUUID().hashCode(),
                intent,
                flags
            )

            NotificationCompat.Action.Builder(
                action.iconResId,
                action.title,
                pendingIntent
            ).apply {
                if (action.requiresInput) {
                    addRemoteInput(
                        RemoteInput.Builder("key_text_reply")
                            .setLabel(action.title)
                            .build()
                    )
                }
            }.build().let { builder.addAction(it) }
        }
    }

    private fun createNotificationChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for $channelName"
                enableLights(true)
                lightColor = ContextCompat.getColor(context, R.color.notification_color)
            }

            context.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    private fun loadAndSetLargeIcon(
        data: NotificationData,
        builder: NotificationCompat.Builder,
        notificationId: Int
    ) {
        data.largeIcon?.let { uri ->
            Glide.with(context)
                .asBitmap()
                .load(uri)
                .transform(CircleCrop())
                .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                    ) {
                        builder.setLargeIcon(resource)
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return
                        }

                        val notificationManager = NotificationManagerCompat.from(context)
                        notificationManager.notify(notificationId, builder.build())
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }


}


class NotificationHelperr(private val context: Context) {

    fun showNotification(data: NotificationData, category: String) {
        val groupKey = getGroupKeyForType(data.type, data)
        val notificationId = data.groupKey?.hashCode() ?: UUID.randomUUID().hashCode()
        val channelId = data.type.channelId

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("Notification", "Notification permission not granted")
            return
        }

        val intent = Intent(Intent.ACTION_VIEW, data.deepLink).apply {
            setPackage(context.packageName)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = when (data.type) {
            NotificationType.CHAT_MESSAGE -> buildChatStyle(data, pendingIntent, groupKey)
            NotificationType.PROFILE -> buildProfileStyle(data, pendingIntent, groupKey)
            NotificationType.POST -> buildPostStyle(data, pendingIntent, groupKey)
            NotificationType.PROJECT_UPDATE -> buildProjectStyle(data, pendingIntent, groupKey)
            NotificationType.APP_UPDATE -> buildAppUpdateStyle(data, pendingIntent, groupKey)
        }.apply {
            setSmallIcon(R.drawable.findr_logo)
            setColor(ContextCompat.getColor(context,R.color.notification_color))
            setCategory(category)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setAutoCancel(true)
            setContentIntent(pendingIntent)
            setGroup(FINDR_GROUP_KEY)
            //setGroup(groupKey)
            setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
        }

        addNotificationActions(builder, data, notificationId)
        loadAndSetLargeIcon(data, builder, notificationId)
        createOrUpdateGroupSummary(data.type, FINDR_GROUP_KEY, channelId, category, notificationId) // actually use group key

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    private fun getGroupKeyForType(type: NotificationType, data: NotificationData): String {
        return when (type) {
            NotificationType.CHAT_MESSAGE -> "${data.groupKey}"
            NotificationType.PROJECT_UPDATE -> "${data.groupKey}"
            NotificationType.POST -> "${data.groupKey}"
            NotificationType.PROFILE -> "profile_view"
            NotificationType.APP_UPDATE -> "group_system"
        }
    }

    private fun createOrUpdateGroupSummary(
        type: NotificationType,
        groupKey: String,
        channelId: String,
        category: String,
        notificationId: Int
    ) {
        val activeNotifications = NotificationManagerCompat.from(context).activeNotifications
        val groupNotifications = activeNotifications.filter {
            it.notification.group == groupKey
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("Post Notification", "Notification permission not granted!")
                return
            }
        }

        val summaryNotification = when (type) {
            NotificationType.CHAT_MESSAGE -> createChatSummary(
                groupKey,
                channelId,
                category,
                groupNotifications.size
            )

            NotificationType.PROJECT_UPDATE -> createProjectSummary(
                groupKey,
                channelId,
                category,
                groupNotifications.size
            )

            NotificationType.POST -> createPostSummary(
                groupKey,
                channelId,
                category,
                groupNotifications.size
            )

            NotificationType.PROFILE -> createProfileSummary(
                groupKey,
                channelId,
                category,
                groupNotifications.size
            )

            NotificationType.APP_UPDATE -> createAppUpdateSummary(groupKey, channelId, category)
            else -> null
        }

        summaryNotification?.let {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(groupKey.hashCode(), it) // no relavance
        }
    }


    // use this method to group messages just after single message
    /*private fun createOrUpdateGroupSummary(
        type: NotificationType,
        groupKey: String,
        channelId: String,
        category: String
    ) {
        val activeNotifications = NotificationManagerCompat.from(context).activeNotifications
        val groupNotifications = activeNotifications.filter {
            it.notification.group == groupKey
        }

        if (groupNotifications.size > 1) {
            val summaryNotification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.findr_logo)
                .setStyle(NotificationCompat.InboxStyle().apply {
                    setSummaryText(when (type) {
                        NotificationType.CHAT_MESSAGE -> "${groupNotifications.size} new messages"
                        NotificationType.PROJECT_UPDATE -> "${groupNotifications.size} project updates"
                        NotificationType.POST -> "${groupNotifications.size} new posts"
                        NotificationType.PROFILE -> "${groupNotifications.size} profile activities"
                        else -> "${groupNotifications.size} notifications"
                    })
                })
                .setGroup(groupKey)
                .setGroupSummary(true)
                .setCategory(category)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(groupKey.hashCode(), summaryNotification)
        }
    }*/

    // region Summary Creators
    private fun createChatSummary(
        groupKey: String,
        channelId: String,
        category: String,
        count: Int
    ): Notification {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.findr_logo)
            .setStyle(
                NotificationCompat.InboxStyle()
                    .setSummaryText(
                        context.resources.getQuantityString(
                            R.plurals.message_count, count, count
                        )
                    )
            )
            .setGroup(groupKey)
            .setGroupSummary(true)
            .setCategory(category)
            .setAutoCancel(true)
            .build()
    }

    private fun createProjectSummary(
        groupKey: String,
        channelId: String,
        category: String,
        count: Int
    ): Notification {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.findr_logo)
            .setContentTitle(context.getString(R.string.project_updates))
            .setStyle(
                NotificationCompat.InboxStyle()
                    .setSummaryText(
                        context.resources.getQuantityString(
                            R.plurals.project_update_count, count, count
                        )
                    )
            )
            .setGroup(groupKey)
            .setGroupSummary(true)
            .setCategory(category)
            .setAutoCancel(true)
            .build()
    }

    private fun createPostSummary(
        groupKey: String,
        channelId: String,
        category: String,
        count: Int
    ): Notification {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.findr_logo)
            .setContentTitle(context.getString(R.string.new_posts))
            .setStyle(
                NotificationCompat.InboxStyle()
                    .setSummaryText(
                        context.resources.getQuantityString(
                            R.plurals.post_count, count, count
                        )
                    )
            )
            .setGroup(groupKey)
            .setGroupSummary(true)
            .setCategory(category)
            .setAutoCancel(true)
            .build()
    }

    private fun createProfileSummary(
        groupKey: String,
        channelId: String,
        category: String,
        count: Int
    ): Notification {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.findr_logo)
            .setContentTitle(context.getString(R.string.profile_activity))
            .setStyle(
                NotificationCompat.InboxStyle()
                    .setSummaryText(
                        context.resources.getQuantityString(
                            R.plurals.profile_activity_count, count, count
                        )
                    )
            )
            .setGroup(groupKey)
            .setGroupSummary(true)
            .setCategory(category)
            .setAutoCancel(true)
            .build()
    }

    private fun createAppUpdateSummary(
        groupKey: String,
        channelId: String,
        category: String
    ): Notification {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.findr_logo)
            .setContentTitle(context.getString(R.string.system_updates))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setSummaryText(context.getString(R.string.system_updates_summary))
            )
            .setGroup(groupKey)
            .setGroupSummary(true)
            .setCategory(category)
            .setAutoCancel(true)
            .build()
    }
    // endregion

    // region Style Builders
    private fun buildChatStyle(
        data: NotificationData,
        pendingIntent: PendingIntent,
        groupKey: String
    ): NotificationCompat.Builder {

        val sharedPrefs = context.getSharedPreferences("chat_notifications", Context.MODE_PRIVATE)
        val messages =
            sharedPrefs.getStringSet(groupKey, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        messages.add(data.body)
        sharedPrefs.edit().putStringSet(groupKey, messages).apply()

        val person = Person.Builder()
            .setName(data.title)
            /*.apply {
                data.largeIcon?.let { uri ->
                    try {
                        val bitmap = Glide.with(context)
                            .asBitmap()
                            .load(uri)
                            .transform(CircleCrop())
                            .submit(128, 128)
                            .get()
                        setIcon(IconCompat.createWithBitmap(bitmap))
                    } catch (e: Exception) {
                        Log.e("Notification", "Error loading avatar", e)
                    }
                }
            }*/
            .build()

        val messagingStyle = NotificationCompat.MessagingStyle(person)
            .setConversationTitle(data.title)

        messages.forEach { msg ->
            messagingStyle.addMessage(msg, System.currentTimeMillis(), person)
        }

        return NotificationCompat.Builder(context, NotificationType.CHAT_MESSAGE.channelId)
            .setStyle(messagingStyle)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setNumber(messages.size)
    }

    private fun buildPostStyle(
        data: NotificationData,
        pendingIntent: PendingIntent,
        groupKey: String
    ): NotificationCompat.Builder {
        val sharedPrefs = context.getSharedPreferences("post_notifications", Context.MODE_PRIVATE)
        val posts =
            sharedPrefs.getStringSet(groupKey, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        posts.add(data.body)
        sharedPrefs.edit().putStringSet(groupKey, posts).apply()

        val bitmap = getLargeBitmap(data.mediaUri)
        val builder = NotificationCompat.Builder(context, NotificationType.POST.channelId)
            .setContentTitle(data.title)
            .setContentText(data.body)
            .setNumber(posts.size)

        return if (bitmap != null) {
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .setBigContentTitle(data.title)
                    .setSummaryText(data.body)
            ).setLargeIcon(bitmap)
        } else {
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(data.body)
                    .setBigContentTitle(data.title)
            )
        }
    }

    private fun buildProfileStyle(
        data: NotificationData,
        pendingIntent: PendingIntent,
        groupKey: String
    ): NotificationCompat.Builder {
        val sharedPrefs =
            context.getSharedPreferences("profile_notifications", Context.MODE_PRIVATE)
        val activities =
            sharedPrefs.getStringSet(groupKey, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        activities.add(data.body)
        sharedPrefs.edit().putStringSet(groupKey, activities).apply()

        return NotificationCompat.Builder(context, NotificationType.PROFILE.channelId)
            .setContentTitle(data.title)
            .setContentText(data.body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(data.body)
                    .setBigContentTitle(data.title)
            )
            .setNumber(activities.size)
    }

    private fun buildProjectStyle(
        data: NotificationData,
        pendingIntent: PendingIntent,
        groupKey: String
    ): NotificationCompat.Builder {
        val sharedPrefs =
            context.getSharedPreferences("project_notifications", Context.MODE_PRIVATE)
        val updates =
            sharedPrefs.getStringSet(groupKey, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        updates.add(data.body)
        sharedPrefs.edit().putStringSet(groupKey, updates).apply()

        return NotificationCompat.Builder(context, NotificationType.PROJECT_UPDATE.channelId)
            .setContentTitle(data.title)
            .setContentText(data.body)
            .setStyle(
                NotificationCompat.InboxStyle()
                    .addLine(data.body)
                    .setBigContentTitle(data.title)
                    .setSummaryText(
                        context.resources.getQuantityString(
                            R.plurals.project_update_count, updates.size, updates.size
                        )
                    )
            )
            .setNumber(updates.size)
    }

    private fun buildAppUpdateStyle(
        data: NotificationData,
        pendingIntent: PendingIntent,
        groupKey: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NotificationType.APP_UPDATE.channelId)
            .setContentTitle(data.title)
            .setContentText(data.body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(data.body)
                    .setBigContentTitle(data.title)
            )
    }
    // endregion

    // region Helper Methods
    private fun getLargeBitmap(uri: Uri?): Bitmap? {
        if (uri == null) return null
        return try {
            Glide.with(context)
                .asBitmap()
                .load(uri)
                .submit(512, 512)
                .get()
        } catch (e: Exception) {
            Log.e("Notification", "Error loading bitmap", e)
            null
        }
    }

    private fun addNotificationActions(
        builder: NotificationCompat.Builder,
        data: NotificationData,
        notificationId: Int
    ) {
        data.actions.forEach { action ->
            val intent = Intent(action.actionId.actionId).apply {
                putExtra("groupKey", data.groupKey)
                putExtra("receiverFcm", data.fcm)
                putExtra("otherUser", data.senderId)
                putExtra("notificationId", notificationId)
                `package` = context.packageName
            }

            val flags = if (action.requiresInput) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                //UUID.randomUUID().hashCode(),
                0,
                intent,
                flags
            )

            NotificationCompat.Action.Builder(
                action.iconResId,
                action.title,
                pendingIntent
            ).apply {
                if (action.requiresInput) {
                    addRemoteInput(
                        RemoteInput.Builder("key_text_reply")
                            .setLabel(action.title)
                            .build()
                    )
                }
            }.build().let { builder.addAction(it) }
        }
    }

    private fun loadAndSetLargeIcon(
        data: NotificationData,
        builder: NotificationCompat.Builder,
        notificationId: Int
    ) {
        data.largeIcon?.let { uri ->
            Glide.with(context)
                .asBitmap()
                .load(uri)
                .transform(CircleCrop())
                .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                    ) {
                        builder.setLargeIcon(resource)
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return
                        }

                        val notificationManager = NotificationManagerCompat.from(context)
                        notificationManager.notify(notificationId, builder.build())
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }
    // endregion
}

// Add these to res/values/strings.xml
/*
<resources>
    <plurals name="message_count">
        <item quantity="one">%d new message</item>
        <item quantity="other">%d new messages</item>
    </plurals>

    <plurals name="project_update_count">
        <item quantity="one">%d project update</item>
        <item quantity="other">%d project updates</item>
    </plurals>

    <plurals name="post_count">
        <item quantity="one">%d new post</item>
        <item quantity="other">%d new posts</item>
    </plurals>

    <plurals name="profile_activity_count">
        <item quantity="one">%d profile activity</item>
        <item quantity="other">%d profile activities</item>
    </plurals>

    <string name="project_updates">Project Updates</string>
    <string name="new_posts">New Posts</string>
    <string name="profile_activity">Profile Activity</string>
    <string name="system_updates">System Updates</string>
    <string name="system_updates_summary">System updates available</string>
</resources>
*/