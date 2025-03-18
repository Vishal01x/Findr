package com.exa.android.reflekt.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.exa.android.reflekt.domain.LinkMetadata

@Database(entities = [LinkMetadata::class], version = 1)
abstract class LinkMetadataDatabase : RoomDatabase() {
    abstract fun metadataDao(): LinkMetadataDao
}

@Dao
interface LinkMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: LinkMetadata)

    @Query("SELECT * FROM link_metadata WHERE url = :url")
    suspend fun get(url: String): LinkMetadata?
}