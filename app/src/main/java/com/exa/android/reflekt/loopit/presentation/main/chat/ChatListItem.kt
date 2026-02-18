package com.exa.android.reflekt.loopit.presentation.main.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.exa.android.reflekt.loopit.theme.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.presentation.main.component.ImageUsingCoil
import com.exa.android.reflekt.loopit.core.utils.formatTimestamp
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.ChatList
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.User


@Composable
fun ChatListItem(chat: ChatList, openImage: (String?) -> Unit, openChat: (user : User) -> Unit) {

    Card(modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        shape = CircleShape) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    openChat(
                        User(
                            userId = chat.userId,
                            name = chat.name,
                            fcmToken = chat.fcmToken
                        )
                    )
                }
                .padding(4.dp)
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            val context = LocalContext.current

            val profilePic = if(chat.isCurUserBlock)"" else chat.profilePicture

            ImageUsingCoil(context,profilePic,R.drawable.placeholder,Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(!profilePic.isNullOrEmpty()) {
                    openImage(chat.profilePicture)
                    /*openImageIntent(context,chat.profilePicture!!)*/
                })

            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    chat.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    chat.lastMessage,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End , modifier = Modifier.padding(end = 4.dp)) {
                val timestampInMillis = chat.lastMessageTimestamp.seconds * 1000L
                if(timestampInMillis > 0) {
                    Text(
                        if (chat.isOtherBlock) "" else formatTimestamp(timestampInMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )
                }
                if (chat.unreadMessages > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.primary
                            )
                    ) {
                        Text(
                            "${chat.unreadMessages}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StoryItem(image: Int, name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ZoomPhoto(modifier: Modifier = Modifier, imageId: Int, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .height(240.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(id = R.drawable.arrow_back),
            contentDescription = "to pop back stack",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .align(alignment = Alignment.TopStart)
                .size(24.dp)
                .clickable {
                    onBack()
                }
        )
        Image(
            painter = painterResource(id = imageId), contentDescription = "ZoomedImage",
            contentScale = ContentScale.Crop
        )
    }
}