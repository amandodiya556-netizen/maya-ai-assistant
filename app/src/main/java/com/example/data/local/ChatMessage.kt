package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MessageSender {
    USER, MAYA
}

enum class PersonalityMode(val displayName: String, val subtitle: String) {
    JARVIS("JARVIS Protocol", "Sharp, Tactical & Automated"),
    COMPANION("Companion Mode", "Warm, Empathetic & Caring")
}

enum class ActionTriggerType {
    NONE,
    OPEN_YOUTUBE,
    OPEN_INSTAGRAM,
    SEARCH_WEB,
    ADD_TASK,
    CREATE_SCRIPT,
    SYSTEM_DIAGNOSTICS
}

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val sender: MessageSender,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: PersonalityMode = PersonalityMode.JARVIS,
    val actionType: ActionTriggerType = ActionTriggerType.NONE,
    val actionPayload: String = ""
)
