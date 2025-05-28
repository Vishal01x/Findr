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
