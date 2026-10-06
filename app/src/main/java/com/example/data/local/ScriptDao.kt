package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScriptDao {
    @Query("SELECT * FROM content_scripts ORDER BY createdAt DESC")
    fun getAllScripts(): Flow<List<ContentScript>>

    @Query("SELECT * FROM content_scripts WHERE platform = :platform ORDER BY createdAt DESC")
    fun getScriptsByPlatform(platform: ScriptPlatform): Flow<List<ContentScript>>

    @Query("SELECT * FROM content_scripts WHERE id = :id")
    suspend fun getScriptById(id: Long): ContentScript?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: ContentScript): Long

    @Update
    suspend fun updateScript(script: ContentScript)

    @Delete
    suspend fun deleteScript(script: ContentScript)
}
