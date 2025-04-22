package com.exa.android.reflekt.loopit.presentation.main.Home

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.image.openImageIntent
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.presentation.main.Home.component.ImageUsingCoil
import com.exa.android.reflekt.loopit.util.formatTimestamp
import com.exa.android.reflekt.loopit.util.model.ChatList
import com.exa.android.reflekt.loopit.util.model.User


@Composable
fun ChatListItem(chat: ChatList, zoomImage: (Int) -> Unit, openChat: (user : User) -> Unit) {

    Card(modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        shape = CircleShape) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .clickable {
                    openChat(
                        User(
                            userId = chat.userId,
                            name = chat.name,
                            fcmToken = chat.fcmToken
                        )
                    )
                }
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            val context = LocalContext.current

            ImageUsingCoil(context,chat.profilePicture,R.drawable.placeholder,Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(!chat.profilePicture.isNullOrEmpty()) { openImageIntent(context,chat.profilePicture!!) })

            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    chat.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    chat.lastMessage,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.DarkGray,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                val timestampInMillis = chat.lastMessageTimestamp.seconds * 1000L
                Text(
                    formatTimestamp(timestampInMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp
                )
                if (chat.unreadMessages > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
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
            color = Color.DarkGray,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ZoomPhoto(modifier: Modifier = Modifier, imageId: Int, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .height(240.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(id = R.drawable.arrow_back),
            contentDescription = "to pop back stack",
            tint = Color.Black,
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


/*
@Composable
fun HomeListItem(chat: String, zoomImage: (Int) -> Unit, openChat: () -> Unit) {
     Card(
          elevation = CardDefaults.cardElevation(8.dp),
          modifier = Modifier
              .padding(vertical = 8.dp, horizontal = 16.dp)
              .fillMaxWidth()
              .clickable {
                  openChat()
              },
          shape = MaterialTheme.shapes.medium,
          colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)

      ) {
          Row(
              modifier = Modifier
                  .padding(16.dp)
                  .fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
          ) {
              Image(
                  painter = painterResource(id = R.drawable.ic_launcher_background),
                  contentDescription = "Profile Picture",
                  modifier = Modifier
                      .size(56.dp)
                      .clip(CircleShape)
                      .shadow(8.dp, shape = CircleShape)
                      .clickable {
                          zoomImage(R.drawable.ic_launcher_background)
                      },
                  contentScale = ContentScale.Crop
              )

              Spacer(modifier = Modifier.width(12.dp))

              Column(
                  modifier = Modifier
                      .weight(1f)
                      .padding(end = 8.dp)

              ) {
                  Text(
                      text = chat,
                      fontSize = 16.sp,
                      maxLines = 1,
                      fontWeight = FontWeight.Bold,
                      overflow = TextOverflow.Ellipsis
                  )
                  Text(
                      text = "What is the status of your work What is the status of your work",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium,
                      style = MaterialTheme.typography.bodyMedium,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                  )
              }
              Column(
                  horizontalAlignment = Alignment.End,
                  verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                  Text(
                      text = "12.20",
                      fontSize = 12.sp,
                      color = Color.Gray
                  )
                  val unreadMessage = 2
                  if (unreadMessage > 0) {
                      Box(
                          modifier = Modifier
                              .size(24.dp)
                              .background(Color.Red, shape = CircleShape),
                          contentAlignment = Alignment.Center
                      ) {
                          Text(
                              text = unreadMessage.toString(),
                              fontSize = 12.sp,
                              color = Color.White,
                              fontWeight = FontWeight.Bold
                          )
                      }
                  }
              }
          }
      }
}
*/