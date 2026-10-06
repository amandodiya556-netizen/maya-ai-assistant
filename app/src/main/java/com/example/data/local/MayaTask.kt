package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class TaskCategory {
    WORK, CONTENT, SYSTEM, PERSONAL, ROUTINE
}

@Entity(tableName = "maya_tasks")
data class MayaTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: TaskCategory = TaskCategory.WORK,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val dueTimeLabel: String = ""
)
