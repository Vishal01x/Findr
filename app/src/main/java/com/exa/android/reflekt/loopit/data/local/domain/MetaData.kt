package com.exa.android.reflekt.loopit.data.local.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "link_metadata")
data class LinkMetadata(
    @PrimaryKey val url: String,
    val title: String?,
    val description: String?,
    val imageUrl: String?,
    val domain: String,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    val timestamp: Long = System.currentTimeMillis()
)