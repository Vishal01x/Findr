package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.presentation.main.Home.component.ImageUsingCoil
import com.exa.android.reflekt.loopit.util.formatTimestamp
import com.exa.android.reflekt.loopit.util.model.Message
import com.exa.android.reflekt.loopit.util.model.Status
import com.exa.android.reflekt.loopit.util.model.User



@Composable
fun ChatHeader(
//    profilePictureUrl: String,
//    userName: String,
//    userStatus: String,
    user : User?,
    status: Status?, // status of other User
    curUser: String, // cur User Id
    //members: List<User>, // all the members of chat
    selectedMessages: Set<Message>, // messages Selected to show its count
    isBlock : Boolean,
    onProfileClick: () -> Unit, // when otherUserProfile Click show its details
    onBackClick: () -> Unit, // navigate to ChatListDetail
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onUnselectClick: () -> Unit, // unselect all the messages
    onCopyClick: () -> Unit,
    onEditClick: (Message?) -> Unit,
    onForwardClick: () -> Unit,
    onDeleteClick: (Int) -> Unit,
    onBlockClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(Color.White),
        shape = RectangleShape,
        modifier = Modifier.clickable { onProfileClick() }
    )  {
    if (selectedMessages.isNotEmpty()) { // if messages are selected then show Header options and hide other profile
        HeaderWithOptions(
            curUser = curUser,
            selectedMessages = selectedMessages,
            onUnselectClick = { onUnselectClick() },
            onCopyClick = { onCopyClick() },
            onForwardClick = { onForwardClick() },
            onDeleteClick = { onDeleteClick(it) },
            onReplyClick = { message -> },
            onEditClick = { message ->
                onEditClick(message)
            }
        )
    } else {
        HeaderWithProfile( // when selectedMessages are 0 now show profile
            otherUser = user,
            status = status,
            curUser = curUser,
            isSelected = selectedMessages.isNotEmpty(),
            isBlock,
            onBackClick = { onBackClick() },
            onProfileClick = { onProfileClick() },
            onVoiceCallClick = { onVoiceCallClick() },
            onVideoCallClick = { onVideoCallClick() },
            onBlockClick = { onBlockClick() }
        )
    }
}
}

@Composable
fun HeaderWithOptions(
    curUser: String,
    selectedMessages: Set<Message>,
    onUnselectClick: () -> Unit,
    onCopyClick: () -> Unit,
    onForwardClick: () -> Unit,
    onDeleteClick: (Int) -> Unit,
    onReplyClick: (Message?) -> Unit,
    onEditClick: (Message?) -> Unit
) {

    var showDialog by remember { mutableStateOf(false) }
    val singleMessage = if (selectedMessages.size == 1) selectedMessages.first() else null
    val endIcons = mutableListOf<IconData>()
    val copy = IconData(
        iconType = IconType.PainterIcon(R.drawable.ic_copy),
        contentDescription = "Copy Selected",
        onClick = { onCopyClick() }
    )

    val delete = IconData(
        iconType = IconType.VectorIcon(Icons.Default.Delete),
        contentDescription = "Delete Selected",
        onClick = { showDialog = true }
    )
    val edit = IconData(
        iconType = IconType.VectorIcon(Icons.Default.Edit),
        contentDescription = "Edit Selected",
        onClick = { onEditClick(singleMessage) }
    )


    getVisibleIcons(selectedMessages, curUser).forEach {
        when (it) {
            IconsName.REPLY ->{}
            IconsName.COPY -> endIcons.add(copy)
            IconsName.EDIT -> endIcons.add(edit)
            IconsName.DELETE -> endIcons.add(delete)
            IconsName.FORWARD -> {}

        }
    }

    PlaceIcons( // see docs
        selectedMessages = selectedMessages.size,
        startIcons = listOf(
            IconData(
                iconType = IconType.VectorIcon(Icons.AutoMirrored.Filled.ArrowBack),
                contentDescription = "Unselect fields",
                onClick = { onUnselectClick() }
            )
        ),
        endIcons = endIcons
    ) { iconData, index ->
        val rotation = if (iconData.contentDescription == "Forward Selected") 90f else 0f
        ShowIcon(iconData = iconData, rotationAngle = rotation)
        // since we use want to use lambda for only one icon but now we need to manage for all
    }

    if (showDialog) {
        DeleteMessageDialog(
            checkAllFromCurrentUser(selectedMessages, curUser),
            checkDeleted(selectedMessages),
            onDelete = { deleteOption ->
                if (deleteOption == "Delete for Me") {
                    onDeleteClick(1)
                } else {
                    onDeleteClick(2)
                }
                showDialog = false
            },
            onCancel = {
                showDialog = false
            }
        )
    }
}

@Composable
fun PlaceIcons(
    selectedMessages: Int,
    startIcons: List<IconData>,
    endIcons: List<IconData>,
    iconContent: @Composable (iconData: IconData, index: Int) -> Unit = { iconData, _ ->
        ShowIcon(iconData = iconData) // it is created to maintain some operations on particular icon
        // like adding some modifiers, rotation etc functioning we make it default like you can utilize
        // when needed else it is optional, here we use it for forward icon in which we set its rotation to 180
        // and for the remaining one it remains no change and we directly call ShowIcon
    }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            startIcons.forEachIndexed { index, iconData ->
                iconContent(iconData, index)
            }

            Text(
                text = selectedMessages.toString(),
                color = Color.Black,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f)) // for keeping endIcons at last

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            endIcons.forEachIndexed { index, iconData ->
                iconContent(iconData, index)
            }
        }
    }
}

@Composable
fun ShowIcon(iconData: IconData, rotationAngle: Float = 0f) {
    IconButton(onClick = { iconData.onClick() }) {
        when (val icon = iconData.iconType) {
            is IconType.VectorIcon -> {
                Icon(
                    imageVector = icon.imageVector,
                    contentDescription = iconData.contentDescription,
                    modifier = Modifier
                        .rotate(rotationAngle)
                        .size(24.dp),
                    tint = Color.Black,
                    // for forward icon we rotate it opposite of reply
                )
            }

            is IconType.PainterIcon -> {
                Icon(
                    painter = painterResource(id = icon.painter),
                    contentDescription = iconData.contentDescription,
                    modifier = Modifier
                        .rotate(rotationAngle)
                        .size(24.dp),
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
fun HeaderWithProfile(
    otherUser: User?,
    status: Status? = Status(),
    curUser: String,
    isSelected : Boolean,
    isBlock: Boolean,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
    onBlockClick : () -> Unit,
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth(),
//            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Profile Picture
        ImageUsingCoil(
            context, otherUser?.profilePicture,
            placeholder = R.drawable.placeholder,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, Color.Black, CircleShape)
                .clickable { onProfileClick() },
            errorImage = R.drawable.placeholder
        )

        Spacer(modifier = Modifier.width(8.dp))

        // User Name and Status
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(!isSelected) {
                    onProfileClick()
                }
                .padding(vertical = 4.dp, horizontal = 8.dp),
        ) {
            // Chat Name
            Text(
                text = otherUser?.name?:"---",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(fraction = 0.9f) // Restrict width to avoid overlapping
            )

            // Chat Status

            val statusText = when {
                        status?.typingTo == curUser -> "typing..."
                        status?.isOnline == true -> "•Active"
                        status?.lastSeen != null -> {
                            val time =
                                formatTimestamp(status.lastSeen * 1000L) // Convert to milliseconds
                            "last active at $time"
                        }

                        else -> "last active at "
                    }


            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = if(statusText == "•Active") Color(0xFF4CAF50) else Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(fraction = 0.9f) // Restrict width to avoid overlapping
            )
        }

//        Box(
//            modifier = Modifier
//                .padding(end = 12.dp)
//                .clip(CircleShape)
//                .background(MaterialTheme.colorScheme.errorContainer)
//                .clickable { onBlockClick() }
//                .padding(horizontal = 4.dp, vertical = 4.dp)
//        ) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(
//                    imageVector = Icons.Filled.Block,
//                    contentDescription = "Block",
//                    tint = MaterialTheme.colorScheme.error
//                )
//                Spacer(modifier = Modifier.width(4.dp))
//                Text(
//                    text = if(!isBlock)"Block" else "UnBlock",
//                    color = MaterialTheme.colorScheme.error,
//                    style = MaterialTheme.typography.labelMedium
//                )
//            }
//        }


//        //Video Call Icon
//        IconButton(onClick = onVideoCallClick) {
//            Icon(
//                imageVector = Icons.Default.Call,
//                contentDescription = "Video Call",
//                tint = Color.Black,
//                modifier = Modifier.size(24.dp)
//            )
//        }
//        //CallButton()
//
//        // Voice Call Icon
//        IconButton(onClick = onVoiceCallClick) {
//            Icon(
//                imageVector = Icons.Default.Call,
//                contentDescription = "Call",
//                tint = Color.Black
//            )
//        }
    }
}





fun getVisibleIcons(selectedMessages: Set<Message>, curUser: String): List<IconsName> {
    if (selectedMessages.isEmpty()) return emptyList()

    val isSingleSelection = selectedMessages.size == 1
    val allFromCurrentUser = checkAllFromCurrentUser(selectedMessages, curUser)
    val anyDeleted = checkDeleted(selectedMessages)

    if (anyDeleted) {
        return listOf(IconsName.DELETE)
    } else if (isSingleSelection) {
        val visibleIcos = listOf(
            IconsName.REPLY,
            IconsName.COPY,
            IconsName.FORWARD,
            IconsName.DELETE,
            IconsName.EDIT
        )
        return if (allFromCurrentUser) visibleIcos else visibleIcos.filter { it != IconsName.EDIT }
    } else {
        return listOf(IconsName.COPY, IconsName.FORWARD, IconsName.DELETE)
    }

}

fun checkAllFromCurrentUser(selectedMessages: Set<Message>, curUser: String): Boolean {
    return selectedMessages.all { it.senderId == curUser }
}

fun checkDeleted(selectedMessages: Set<Message>): Boolean {
    return selectedMessages.any { it.message == "deleted" }
}


sealed class IconType { // Since icon can be imageVector or painterResource we create a sealed class and utilize it further
    data class VectorIcon(val imageVector: ImageVector) : IconType()
    data class PainterIcon(val painter: Int) : IconType()
}

data class IconData(
    val iconType: IconType,
    val contentDescription: String,
    val onClick: () -> Unit
)// it's data class, we created it pass details of icon in form of list to another fun
// actually we are creating same ui for all icons so instead of redundant code we create a separate composable and pass list

enum class IconsName {
    COPY, DELETE, FORWARD, EDIT, REPLY
}



//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Back Button
//            IconButton(onClick = onBackClick) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
//                    contentDescription = "Back",
//                    tint = Color.Black,
//                    modifier = Modifier.size(32.dp)
//                )
//            }
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            // Profile Picture
//            Image(
//                painter = painterResource(id = R.drawable.chat_img3),
//                contentDescription = "Profile Picture",
//                modifier = Modifier
//                    .size(40.dp)
//                    .clip(CircleShape)
//                    .border(1.dp, Color.Black, CircleShape),
//                contentScale = ContentScale.Crop
//            )
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            // User Name and Status
//            Column {
//                Text(
//                    text = user.name,
//                    style = MaterialTheme.typography.bodyLarge,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black
//                )
//                Text(
//                    text =  when {
//                        status.typingTo == curUser -> "typing..."
//                        status.isOnline -> "Online"
//                        status.lastSeen != null -> {
//                            val time =
//                                formatTimestamp(status.lastSeen * 1000L) // Convert to milliseconds
//                            "last seen at $time"
//                        }
//
//                        else -> "Offline"
//                    },
//                    style = MaterialTheme.typography.labelSmall,
//                    color = Color.Gray
//                )
//            }
//
//            Spacer(modifier = Modifier.weight(1f))
//
//            // Video Call Icon
//            IconButton(onClick = onVideoCallClick) {
//                Icon(
//                    imageVector = Icons.Default.Call,
//                    contentDescription = "Video Call",
//                    tint = Color.Black,
//                    modifier = Modifier.size(24.dp)
//                )
//            }
//
//            // Call Icon
//            IconButton(onClick = onCallClick) {
//                Icon(
//                    imageVector = Icons.Default.Call,
//                    contentDescription = "Call",
//                    tint = Color.Black
//                )
//            }
//        }
//    }
//}


