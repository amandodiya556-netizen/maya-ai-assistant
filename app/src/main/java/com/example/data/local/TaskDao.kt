package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM maya_tasks ORDER BY isCompleted ASC, priority DESC, id DESC")
    fun getAllTasks(): Flow<List<MayaTask>>

    @Query("SELECT * FROM maya_tasks WHERE isCompleted = 0 ORDER BY priority DESC, id DESC")
    fun getPendingTasks(): Flow<List<MayaTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: MayaTask): Long

    @Update
    suspend fun updateTask(task: MayaTask)

    @Delete
    suspend fun deleteTask(task: MayaTask)

    @Query("DELETE FROM maya_tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    @Query("UPDATE maya_tasks SET isCompleted = :completed WHERE id = :taskId")
    suspend fun setTaskCompleted(taskId: Long, completed: Boolean)
}
