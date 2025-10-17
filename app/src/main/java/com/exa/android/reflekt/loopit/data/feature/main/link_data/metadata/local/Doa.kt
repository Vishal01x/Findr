package com.exa.android.reflekt.loopit.data.feature.main.link_data.metadata.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.exa.android.reflekt.loopit.data.feature.main.link_data.metadata.model.LinkMetadata

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