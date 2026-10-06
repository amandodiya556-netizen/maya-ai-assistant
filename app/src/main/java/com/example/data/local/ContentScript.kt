package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ScriptPlatform(val displayName: String) {
    INSTAGRAM_REEL("Instagram Reel"),
    YOUTUBE_SHORTS("YouTube Shorts"),
    YOUTUBE_LONG("YouTube Video"),
    TECH_REVIEW("Tech Review"),
    VIRAL_HOOK("Viral Hook Idea")
}

@Entity(tableName = "content_scripts")
data class ContentScript(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val topic: String,
    val platform: ScriptPlatform = ScriptPlatform.INSTAGRAM_REEL,
    val hook: String = "",
    val scriptBody: String = "",
    val visualNotes: String = "",
    val callToAction: String = "",
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
