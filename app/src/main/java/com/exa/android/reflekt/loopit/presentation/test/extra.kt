package com.exa.android.reflekt.loopit.presentation.test

/* fun showNotification(data: NotificationData, categoryProgress: String) {
        val notificationId = data.groupKey?.hashCode() ?: UUID.randomUUID().hashCode()
        val channelId = data.type.channelId

        //Log.d("FCM", "sender get message - $data")

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                "Chat Notifications",
//                NotificationManager.IMPORTANCE_HIGH
//            )
//            context.getSystemService(NotificationManager::class.java)
//                ?.createNotificationChannel(channel)
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("Post Notification", "Notification permission not granted!")
                return
            }
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

       val builder =  if (channelId == NotificationType.CHAT_MESSAGE.channelId) { // For chat channel, create MessagingStyle with Person
            val person = Person.Builder().setName(data.title).build()
            val messagingStyle = NotificationCompat.MessagingStyle(person)
                .setConversationTitle(data.title)
                .addMessage(data.body, System.currentTimeMillis(), person)

            NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.findr_logo)
                .setColor(ContextCompat.getColor(context, R.color.notification_color))
                .setStyle(messagingStyle)
                .setCategory(categoryProgress)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
        } else {
            // For other channels like social, use simple NotificationCompat.InboxStyle or just null
            // (InboxStyle example to show multiple lines)
//            NotificationCompat.InboxStyle()   for email type show user image as email does
//                .setBigContentTitle(data.title)
//                .addLine(data.body)

            val messagingStyle = if(channelId != NotificationType.POST.channelId)
                NotificationCompat.BigTextStyle()
                .setBigContentTitle(data.title)
                .bigText(data.body)       // Full body when expanded
               else {
                NotificationCompat.BigPictureStyle()
                    .bigPicture(data.largeIcon)
                    .setBigContentTitle(data.title) // Title in expanded view
                    .setSummaryText(data.body)   // Summary in expanded view
            }
           NotificationCompat.Builder(context, channelId)
               .setSmallIcon(R.drawable.findr_logo)
               .setContentTitle(data.title)      // e.g. "John Doe posted"
               .setContentText(data.body)
               .setColor(ContextCompat.getColor(context, R.color.notification_color))
               .setStyle(messagingStyle)
               .setCategory(categoryProgress)
               .setPriority(NotificationCompat.PRIORITY_HIGH)
               .setAutoCancel(true)
               .setContentIntent(pendingIntent)
        }


        data.groupKey?.let { builder.setGroup(it) }
        /*data.actions.forEach {
            val actionIntent = PendingIntent.getBroadcast(
                context,
                it.actionId.hashCode(),
                Intent(it.actionId.actionId),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val actionBuilder = NotificationCompat.Action.Builder(
                it.iconResId,
                it.title,
                actionIntent
            )
            if (it.requiresInput) {
                // Add remote input here if needed
            }

            builder.addAction(actionBuilder.build())
        }*/

        data.actions.forEach { action ->
            val intent = Intent(action.actionId.actionId).apply {
                putExtra("groupKey", data.groupKey)
                `package` = context.packageName // Security measure
            }

            // Use mutable flag for actions requiring input
            val flags = if (action.requiresInput) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            }


            val pendingIntent = PendingIntent.getBroadcast(
                context,
                UUID.randomUUID().hashCode(), // Unique request code
                intent,
                flags
            )

            val actionBuilder = NotificationCompat.Action.Builder(
                action.iconResId,
                action.title,
                pendingIntent
            )

            if (action.requiresInput) {
                val remoteInput = RemoteInput.Builder("key_text_reply")
                    .setLabel(action.title)
                    .build()
                actionBuilder.addRemoteInput(remoteInput)
            }

            builder.addAction(actionBuilder.build())
        }

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, builder.build())

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
                        notificationManager.notify(notificationId, builder.build())
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }*/


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.pager.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalPagerApi::class)
@Composable
fun Posttui() {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            val nextPage = (pagerState.currentPage + 1) % 1
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Schedule, contentDescription = null)
            Text(text = "2h • 456", fontSize = 14.sp)
            Text(
                text = "Urgent",
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color(0xFFFFCDD2), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "DSA Study Session with Vishal - Join at 7 PM! ",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Vishal is leading an intensive DSA discussion session tonight at 7 PM. We'll cover dynamic programming, graph algorithms, and solve some...",
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalPager(count = 1, state = pagerState) { page ->
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = "https://path_to_your_uploaded_image.jpg",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        val prev = (pagerState.currentPage - 1 + 1) % 1
                        scope.launch { pagerState.animateScrollToPage(prev) }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "Previous")
                    }
                    IconButton(onClick = {
                        val next = (pagerState.currentPage + 1) % 1
                        scope.launch { pagerState.animateScrollToPage(next) }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = "Next")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Join DSA Study Session - Google Meet",
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A73E8),
            fontSize = 16.sp,
            modifier = Modifier
                .background(Color(0xFFE8F0FE), RoundedCornerShape(8.dp))
                .padding(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("#DSA", "#Study Group", "#Programming").forEach {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    modifier = Modifier
                        .background(Color(0xFFF0F0F0), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            listOf(89, 67, 34).forEach {
                Text(text = it.toString(), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}


@Preview
@Composable
private fun pre() {
    Posttui()
}