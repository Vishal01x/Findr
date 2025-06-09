package com.exa.android.reflekt.loopit.fcm

import android.Manifest
import android.app.NotificationManager
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
//import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.exa.android.reflekt.MyApp
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.remote.main.Repository.FirestoreService
import com.exa.android.reflekt.loopit.data.remote.main.Repository.ProjectRepository
import com.exa.android.reflekt.loopit.data.remote.main.Repository.UserRepository
import com.exa.android.reflekt.loopit.util.clearChatNotifications
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

/*
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var chatRepository: FirestoreService // For chat operations

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var projectRepository: ProjectRepository // For projec

    override fun onReceive(context: Context, intent: Intent) {
        // Verify intent came from your app
        if (intent.`package` != context.packageName) return

        val action = intent.action
        val groupKey = intent.getStringExtra("groupKey")

        val remoteInput = RemoteInput.getResultsFromIntent(intent)

        when (ActionType.fromActionId(action)) {
            ActionType.Reply -> handleReply(context, groupKey, remoteInput, intent)
            ActionType.Accept -> handleProjectAction(context, groupKey, true)
            ActionType.Reject -> handleProjectAction(context, groupKey, false)
            ActionType.Update -> TODO()
            null -> TODO()
        }
    }

    private fun handleReply(context: Context, chatId: String?, remoteInput: Bundle?, intent: Intent) {
        val replyText = remoteInput?.getString("key_text_reply")
        val senderId = intent.getStringExtra("otherUser")
        val recieverFcm = intent.getStringExtra("receiverFcm")
        chatId ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val curUser = userRepository.getCurUser()
            if (senderId != null) {
                chatRepository.createChatAndSendMessage(
                    userId2 = senderId ,
                    isCurUserBlocked = false,
                    text = replyText ?: "New message",
                    replyTo = null,
                    media = null,
                    receiverToken = recieverFcm,
                    curUser = curUser,
                    messageId = null
                )
            }
        }

        // Send reply message
//        val repository = (context.applicationContext as MyApp).repository
      // chatRepository.createChatAndSendMessage(chatId, false,replyText ?: "")

        // Dismiss notification
        NotificationManagerCompat.from(context).cancel(chatId.hashCode())
    }

    private fun handleProjectAction(context: Context, projectId: String?, accept: Boolean) {
        projectId ?: return

        // Update project status
//        val repository = (context.applicationContext as MyApp).repository
//        if (accept) repository.acceptProjectInvite(projectId)
//        else repository.rejectProjectInvite(projectId)

        // Dismiss notification
        NotificationManagerCompat.from(context).cancel(projectId.hashCode())
    }
}


/*class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val actionId = intent.getStringExtra("action_id")
        val deepLink = intent.getStringExtra("deep_link")?.let { Uri.parse(it) }

        when (actionId) {
            "accept_project" -> handleProjectAcceptance(deepLink)
            "reject_project" -> handleProjectRejection(deepLink)
            "reply_message" -> handleMessageReply(intent)
            // Add more action handlers
        }
    }

    private fun handleMessageReply(intent: Intent) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        val message = remoteInput?.getString("response")
        // Handle message reply
    }

    private fun handleProjectAcceptance(deepLink: Uri?) {
        // Extract project ID from deepLink and handle
    }

    private fun handleProjectRejection(deepLink: Uri?) {
        // Extract project ID from deepLink and handle
    }
}*/
*/

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var chatRepository: FirestoreService

    @Inject
    lateinit var userRepository: UserRepository

    override fun onReceive(context: Context, intent: Intent) {
        // Verify intent came from your app
        if (intent.`package` != context.packageName) return

        val action = intent.action
        val groupKey = intent.getStringExtra("groupKey")
        val remoteInput = RemoteInput.getResultsFromIntent(intent)

        when (ActionType.fromActionId(action)) {
            ActionType.Reply -> handleReply(context, groupKey, remoteInput, intent)
            ActionType.Accept -> handleProjectAction(context, groupKey, true, intent)
            ActionType.Reject -> handleProjectAction(context, groupKey, false, intent)
            ActionType.Update -> {}
            ActionType.Mark_As_Read -> {
                handleMarkRead(context, intent)
            }

            ActionType.Ignore -> {
                handleIgnore(context, intent)
            }

            null -> { /*Log.e("NotificationAction", "Unknown action type") */
            }
        }
    }

    private fun handleReply(
        context: Context,
        chatId: String?,
        remoteInput: Bundle?,
        intent: Intent
    ) {
        val replyText = remoteInput?.getString("key_text_reply")
        val senderId = intent.getStringExtra("otherUser")
        val receiverFcm = intent.getStringExtra("receiverFcm")

        if (chatId == null || senderId == null || receiverFcm == null) {
            //Log.e("NotificationAction", "Missing required parameters for reply")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val curUser = userRepository.getCurUser() ?: run {
                    //Log.e("NotificationAction", "User not logged in")
                    return@launch
                }

                //Log.d("FCM", "$replyText $chatId, ${Firebase.auth.currentUser?.uid}, $senderId, $receiverFcm, $curUser")

                chatRepository.createChatAndSendMessage(
                    userId2 = senderId,
                    isCurUserBlocked = false,
                    text = replyText ?: "New message",
                    replyTo = null,
                    media = null,
                    receiverToken = receiverFcm,
                    curUser = curUser,
                    messageId = null
                ).also {
                    // Only dismiss notification after successful send
                    //chatRepository.getMessages(Firebase.auth.currentUser?.uid?:"", senderId ?: "")
                    //Log.d("FCM", "success")
                    withContext(Dispatchers.Main) {
                        //clearChatNotifications(context, chatId)
                        val notificationManager =
                            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.cancel(chatId.hashCode())
                        //Log.d("FCM", "success22")
                    }
                }
            } catch (e: Exception) {
                //Log.e("NotificationAction", "Failed to send message", e)
                // Show error notification
                showErrorNotification(context, "Failed to send message")
            }
        }
    }

    private fun handleMarkRead(context: Context, intent: Intent) {
        //Log.d("FCM", "readCalled")

        val notificationId = intent.getIntExtra("notificationId", -1)
        val groupKey = intent.getStringExtra("groupKey")

        if (!groupKey.isNullOrEmpty()) {
            chatRepository.updateUnreadMessages(groupKey)
        }

        // Clear from SharedPreferences
        context.getSharedPreferences("chat_notifications", Context.MODE_PRIVATE)
            .edit()
            .remove(groupKey)
            .apply()

        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }

        //Log.d("FCM", "readFinish")
    }

    private fun handleIgnore(context: Context, intent: Intent) {
        //Log.d("FCM", "ignorecallee")
        val notificationId = intent.getIntExtra("notificationId", -1)
        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }
        //Log.d("FCM", "ignorefinish")
    }

    private fun handleProjectAction(
        context: Context,
        projectId: String?,
        accept: Boolean,
        originalIntent: Intent
    ) {
        if (projectId == null) {
            return
        }
        try {
            // Create deep link to open project screen
            val deepLinkUri = "findr://project/$projectId"

            val deepLinkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkUri)).apply {
                setPackage(context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            context.startActivity(deepLinkIntent)

        } catch (e: Exception) {
            showErrorNotification(context, "Action failed ${e.localizedMessage}")
        }
    }


    private fun showErrorNotification(context: Context, message: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                //Log.e("Post Notification", "Notification permission not granted!")
                return
            }
        }

        NotificationCompat.Builder(context, MyApp.CHANNEL_SYSTEM)
            .setSmallIcon(R.drawable.findr_logo)
            .setContentTitle("Action Failed")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build().also {
                NotificationManagerCompat.from(context).notify(
                    UUID.randomUUID().hashCode(),
                    it
                )
            }
    }
}

