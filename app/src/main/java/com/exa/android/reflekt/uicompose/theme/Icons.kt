package com.exa.android.reflekt.uicompose.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

interface FindrIconsWrapper {
    val id: String
    val icon: ImageVector
}

object FindrIcons {
    
    /**
     * Default (Outlined) Icons
     */
    enum class Default(
        override val id: String,
        override val icon: ImageVector
    ) : FindrIconsWrapper {
        // Navigation Icons
        HOME("home", Icons.Outlined.Home),
        SEARCH("search", Icons.Outlined.Search),
        NOTIFICATIONS("notifications", Icons.Outlined.Notifications),
        PROFILE("profile", Icons.Outlined.Person),
        SETTINGS("settings", Icons.Outlined.Settings),
        
        // Action Icons
        ADD("add", Icons.Outlined.Add),
        EDIT("edit", Icons.Outlined.Edit),
        DELETE("delete", Icons.Outlined.Delete),
        CLOSE("close", Icons.Outlined.Close),
        CHECK("check", Icons.Outlined.Check),
        BACK("back", Icons.Outlined.ArrowBack),
        FORWARD("forward", Icons.Outlined.ArrowForward),
        MORE_VERT("more_vert", Icons.Outlined.MoreVert),
        MORE_HORIZ("more_horiz", Icons.Outlined.MoreHoriz),
        MENU("menu", Icons.Outlined.Menu),
        
        // Content Icons
        FAVORITE("favorite", Icons.Outlined.FavoriteBorder),
        SHARE("share", Icons.Outlined.Share),
        BOOKMARK("bookmark", Icons.Outlined.BookmarkBorder),
        COMMENT("comment", Icons.Outlined.ChatBubbleOutline),
        SEND("send", Icons.Outlined.Send),
        ATTACH("attach", Icons.Outlined.AttachFile),
        DOWNLOAD("download", Icons.Outlined.Download),
        UPLOAD("upload", Icons.Outlined.Upload),
        
        // Media Icons
        CAMERA("camera", Icons.Outlined.CameraAlt),
        IMAGE("image", Icons.Outlined.Image),
        VIDEO("video", Icons.Outlined.VideoLibrary),
        PLAY("play", Icons.Outlined.PlayArrow),
        PAUSE("pause", Icons.Outlined.Pause),
        
        // Communication Icons
        CALL("call", Icons.Outlined.Call),
        EMAIL("email", Icons.Outlined.Email),
        MESSAGE("message", Icons.Outlined.Message),
        
        // Status Icons
        INFO("info", Icons.Outlined.Info),
        WARNING("warning", Icons.Outlined.Warning),
        ERROR("error", Icons.Outlined.Error),
        SUCCESS("success", Icons.Outlined.CheckCircle),
        
        // Utility Icons
        LOCK("lock", Icons.Outlined.Lock),
        UNLOCK("unlock", Icons.Outlined.LockOpen),
        VISIBILITY("visibility", Icons.Outlined.Visibility),
        VISIBILITY_OFF("visibility_off", Icons.Outlined.VisibilityOff),
        REFRESH("refresh", Icons.Outlined.Refresh),
        FILTER("filter", Icons.Outlined.FilterList),
        SORT("sort", Icons.Outlined.Sort),
        CALENDAR("calendar", Icons.Outlined.CalendarToday),
        LOCATION("location", Icons.Outlined.LocationOn),
        STAR("star", Icons.Outlined.StarBorder),
        
        // Arrow Icons
        ARROW_UP("arrow_up", Icons.Outlined.KeyboardArrowUp),
        ARROW_DOWN("arrow_down", Icons.Outlined.KeyboardArrowDown),
        ARROW_LEFT("arrow_left", Icons.Outlined.KeyboardArrowLeft),
        ARROW_RIGHT("arrow_right", Icons.Outlined.KeyboardArrowRight),
        EXPAND_MORE("expand_more", Icons.Outlined.ExpandMore),
        EXPAND_LESS("expand_less", Icons.Outlined.ExpandLess)
    }
    
    /**
     * Filled Icons
     */
    enum class Filled(
        override val id: String,
        override val icon: ImageVector
    ) : FindrIconsWrapper {
        // Navigation Icons
        HOME("home_filled", Icons.Filled.Home),
        SEARCH("search_filled", Icons.Filled.Search),
        NOTIFICATIONS("notifications_filled", Icons.Filled.Notifications),
        PROFILE("profile_filled", Icons.Filled.Person),
        SETTINGS("settings_filled", Icons.Filled.Settings),
        
        // Action Icons
        ADD("add_filled", Icons.Filled.Add),
        EDIT("edit_filled", Icons.Filled.Edit),
        DELETE("delete_filled", Icons.Filled.Delete),
        CLOSE("close_filled", Icons.Filled.Close),
        CHECK("check_filled", Icons.Filled.Check),
        BACK("back_filled", Icons.Filled.ArrowBack),
        FORWARD("forward_filled", Icons.Filled.ArrowForward),
        MORE_VERT("more_vert_filled", Icons.Filled.MoreVert),
        MORE_HORIZ("more_horiz_filled", Icons.Filled.MoreHoriz),
        MENU("menu_filled", Icons.Filled.Menu),
        
        // Content Icons
        FAVORITE("favorite_filled", Icons.Filled.Favorite),
        SHARE("share_filled", Icons.Filled.Share),
        BOOKMARK("bookmark_filled", Icons.Filled.Bookmark),
        COMMENT("comment_filled", Icons.Filled.ChatBubble),
        SEND("send_filled", Icons.Filled.Send),
        ATTACH("attach_filled", Icons.Filled.AttachFile),
        DOWNLOAD("download_filled", Icons.Filled.Download),
        UPLOAD("upload_filled", Icons.Filled.Upload),
        
        // Media Icons
        CAMERA("camera_filled", Icons.Filled.CameraAlt),
        IMAGE("image_filled", Icons.Filled.Image),
        VIDEO("video_filled", Icons.Filled.VideoLibrary),
        PLAY("play_filled", Icons.Filled.PlayArrow),
        PAUSE("pause_filled", Icons.Filled.Pause),
        
        // Communication Icons
        CALL("call_filled", Icons.Filled.Call),
        EMAIL("email_filled", Icons.Filled.Email),
        MESSAGE("message_filled", Icons.Filled.Message),
        
        // Status Icons
        INFO("info_filled", Icons.Filled.Info),
        WARNING("warning_filled", Icons.Filled.Warning),
        ERROR("error_filled", Icons.Filled.Error),
        SUCCESS("success_filled", Icons.Filled.CheckCircle),
        
        // Utility Icons
        LOCK("lock_filled", Icons.Filled.Lock),
        UNLOCK("unlock_filled", Icons.Filled.LockOpen),
        VISIBILITY("visibility_filled", Icons.Filled.Visibility),
        VISIBILITY_OFF("visibility_off_filled", Icons.Filled.VisibilityOff),
        REFRESH("refresh_filled", Icons.Filled.Refresh),
        FILTER("filter_filled", Icons.Filled.FilterList),
        SORT("sort_filled", Icons.Filled.Sort),
        CALENDAR("calendar_filled", Icons.Filled.CalendarToday),
        LOCATION("location_filled", Icons.Filled.LocationOn),
        STAR("star_filled", Icons.Filled.Star),
        
        // Arrow Icons
        ARROW_UP("arrow_up_filled", Icons.Filled.KeyboardArrowUp),
        ARROW_DOWN("arrow_down_filled", Icons.Filled.KeyboardArrowDown),
        ARROW_LEFT("arrow_left_filled", Icons.Filled.KeyboardArrowLeft),
        ARROW_RIGHT("arrow_right_filled", Icons.Filled.KeyboardArrowRight),
        EXPAND_MORE("expand_more_filled", Icons.Filled.ExpandMore),
        EXPAND_LESS("expand_less_filled", Icons.Filled.ExpandLess)
    }
}

@Immutable
data class FindrIconSet(
    val default: FindrIcons.Default,
    val filled: FindrIcons.Filled
)


/**
 * Get icon by ID from Default icons
 */
fun getDefaultIconById(id: String): ImageVector? {
    return FindrIcons.Default.values().find { it.id == id }?.icon
}

/**
 * Get icon by ID from Filled icons
 */
fun getFilledIconById(id: String): ImageVector? {
    return FindrIcons.Filled.values().find { it.id == id }?.icon
}

/**
 * Get icon by ID from either Default or Filled icons
 */
fun getIconById(id: String, filled: Boolean = false): ImageVector? {
    return if (filled) {
        getFilledIconById(id)
    } else {
        getDefaultIconById(id)
    }
}

val LocalFindrIcons = staticCompositionLocalOf<FindrIcons> { FindrIcons }