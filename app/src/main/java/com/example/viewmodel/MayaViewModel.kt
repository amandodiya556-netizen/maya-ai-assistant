package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.automation.CommandDispatcher
import com.example.automation.SystemTelemetry
import com.example.automation.VoiceManager
import com.example.data.api.GeminiRepository
import com.example.data.local.ActionTriggerType
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessage
import com.example.data.local.ContentScript
import com.example.data.local.MayaTask
import com.example.data.local.MessageSender
import com.example.data.local.PersonalityMode
import com.example.data.local.ScriptPlatform
import com.example.data.local.TaskCategory
import com.example.data.local.TaskPriority
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MayaNavTab(val label: String) {
    CHAT("Maya Core"),
    TASKS("Tasks HUD"),
    CONTENT_STUDIO("Content Studio"),
    DIAGNOSTICS("Diagnostics")
}

class MayaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val taskDao = db.taskDao()
    private val scriptDao = db.scriptDao()
    private val chatDao = db.chatDao()

    val commandDispatcher = CommandDispatcher(application)
    val voiceManager = VoiceManager(application)
    private val geminiRepository = GeminiRepository(application)

    private val _currentTab = MutableStateFlow(MayaNavTab.CHAT)
    val currentTab: StateFlow<MayaNavTab> = _currentTab.asStateFlow()

    private val _personalityMode = MutableStateFlow(PersonalityMode.JARVIS)
    val personalityMode: StateFlow<PersonalityMode> = _personalityMode.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _isVoiceMuted = MutableStateFlow(false)
    val isVoiceMuted: StateFlow<Boolean> = _isVoiceMuted.asStateFlow()

    private val _hudNotification = MutableStateFlow<String?>(null)
    val hudNotification: StateFlow<String?> = _hudNotification.asStateFlow()

    private val _telemetry = MutableStateFlow(commandDispatcher.getSystemTelemetry())
    val telemetry: StateFlow<SystemTelemetry> = _telemetry.asStateFlow()

    val messages: StateFlow<List<ChatMessage>> = chatDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<MayaTask>> = taskDao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scripts: StateFlow<List<ContentScript>> = scriptDao.getAllScripts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Seed initial friendly greeting from Maya if message table is empty
        viewModelScope.launch {
            if (chatDao.getMessageCount() == 0) {
                chatDao.insertMessage(
                    ChatMessage(
                        text = "System Initialized. Greetings, Boss! Maya AI is fully synced and at your command.\n\nI can execute device automations like launching YouTube or Instagram, conduct web intelligence, manage your task matrix, and craft high-retention video scripts in Hinglish and English. What are we conquering today?",
                        sender = MessageSender.MAYA,
                        mode = PersonalityMode.JARVIS,
                        actionType = ActionTriggerType.NONE
                    )
                )

                // Seed some default useful tasks & viral script
                taskDao.insertTask(
                    MayaTask(
                        title = "Shoot Tech Unboxing Reel",
                        description = "Record hook and b-roll shots with 60fps lighting",
                        category = TaskCategory.CONTENT,
                        priority = TaskPriority.HIGH,
                        dueTimeLabel = "Today 5:00 PM"
                    )
                )
                taskDao.insertTask(
                    MayaTask(
                        title = "Review Weekly Analytics & Growth",
                        description = "Check Instagram insights and YouTube CTR metrics",
                        category = TaskCategory.WORK,
                        priority = TaskPriority.MEDIUM,
                        dueTimeLabel = "Tomorrow 10:00 AM"
                    )
                )

                scriptDao.insertScript(
                    ContentScript(
                        title = "5 AI Tools That Feel Illegal",
                        topic = "Secret AI Productivity Tools",
                        platform = ScriptPlatform.INSTAGRAM_REEL,
                        hook = "Boss, agar aap abhi bhi manual tasks me 4 ghante waste kar rahe ho, toh stop scrolling right now!",
                        scriptBody = "Tool #1 automates your entire research.\nTool #2 creates realistic avatars in seconds.\nTool #3 handles your post-production cuts automatically.",
                        visualNotes = "Fast pacing, dynamic text popups, split screen demonstration",
                        callToAction = "Save this reel before algorithms hide it! Follow for daily tech hacks.",
                        tags = "#TechIndia #AITools #ProductivityHacks #CreatorEconomy"
                    )
                )
            }
        }
    }

    fun selectTab(tab: MayaNavTab) {
        _currentTab.value = tab
    }

    fun togglePersonalityMode() {
        val newMode = if (_personalityMode.value == PersonalityMode.JARVIS) {
            PersonalityMode.COMPANION
        } else {
            PersonalityMode.JARVIS
        }
        _personalityMode.value = newMode
        triggerHudNotification("PROTOCOL SWITCHED: ${newMode.displayName.uppercase()}")

        val announcement = if (newMode == PersonalityMode.JARVIS) {
            "JARVIS Protocol armed. Precision automation online, Boss."
        } else {
            "Companion Mode active! Main hamesha aapke saath hoon, Boss. Tell me everything! 💖"
        }
        viewModelScope.launch {
            chatDao.insertMessage(
                ChatMessage(
                    text = announcement,
                    sender = MessageSender.MAYA,
                    mode = newMode
                )
            )
            voiceManager.speak(announcement, newMode)
        }
    }

    fun toggleVoiceMute() {
        val newMuted = !_isVoiceMuted.value
        _isVoiceMuted.value = newMuted
        voiceManager.isVoiceEnabled = !newMuted
        if (newMuted) {
            voiceManager.stop()
            triggerHudNotification("VOICE SYNTHESIS: MUTED")
        } else {
            triggerHudNotification("VOICE SYNTHESIS: ACTIVE")
        }
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isBlank() || _isThinking.value) return

        val currentMode = _personalityMode.value

        viewModelScope.launch {
            // 1. Insert user message
            chatDao.insertMessage(
                ChatMessage(
                    text = trimmed,
                    sender = MessageSender.USER,
                    mode = currentMode
                )
            )

            _isThinking.value = true

            // 2. Build history
            val history = messages.value.takeLast(6).map {
                (if (it.sender == MessageSender.USER) "user" else "model") to it.text
            }

            // 3. Request Gemini API
            val response = geminiRepository.chatWithMaya(history, trimmed, currentMode)

            // 4. Handle any action tag
            var actionType = ActionTriggerType.NONE
            var actionData = ""

            when (response.actionTag) {
                "OPEN_YOUTUBE" -> {
                    actionType = ActionTriggerType.OPEN_YOUTUBE
                    actionData = response.actionParam ?: ""
                    triggerHudNotification("AUTOMATION: LAUNCHING YOUTUBE")
                    commandDispatcher.openYouTube(response.actionParam)
                }
                "OPEN_INSTAGRAM" -> {
                    actionType = ActionTriggerType.OPEN_INSTAGRAM
                    actionData = response.actionParam ?: ""
                    triggerHudNotification("AUTOMATION: LAUNCHING INSTAGRAM")
                    commandDispatcher.openInstagram(response.actionParam)
                }
                "SEARCH_WEB" -> {
                    actionType = ActionTriggerType.SEARCH_WEB
                    actionData = response.actionParam ?: trimmed
                    triggerHudNotification("INTEL: SEARCHING WEB")
                    commandDispatcher.searchWeb(actionData)
                }
                "ADD_TASK" -> {
                    actionType = ActionTriggerType.ADD_TASK
                    val parts = response.actionParam?.split("|") ?: listOf("New task for Boss")
                    val title = parts.getOrNull(0) ?: "New Task"
                    val priority = runCatching { TaskPriority.valueOf(parts.getOrElse(1) { "HIGH" }) }.getOrDefault(TaskPriority.HIGH)
                    val category = runCatching { TaskCategory.valueOf(parts.getOrElse(2) { "WORK" }) }.getOrDefault(TaskCategory.WORK)
                    taskDao.insertTask(
                        MayaTask(
                            title = title,
                            priority = priority,
                            category = category,
                            dueTimeLabel = "Added via Maya Chat"
                        )
                    )
                    triggerHudNotification("TASK MATRIX UPDATED: $title")
                }
                "CREATE_SCRIPT" -> {
                    actionType = ActionTriggerType.CREATE_SCRIPT
                    val parts = response.actionParam?.split("|") ?: listOf(trimmed)
                    val title = parts.getOrNull(0) ?: "Viral Video Idea"
                    val platform = runCatching { ScriptPlatform.valueOf(parts.getOrElse(1) { "INSTAGRAM_REEL" }) }.getOrDefault(ScriptPlatform.INSTAGRAM_REEL)
                    scriptDao.insertScript(
                        ContentScript(
                            title = title,
                            topic = trimmed,
                            platform = platform,
                            scriptBody = response.spokenText
                        )
                    )
                    triggerHudNotification("SCRIPT SAVED TO STUDIO: $title")
                }
                "SYSTEM_DIAGNOSTICS" -> {
                    actionType = ActionTriggerType.SYSTEM_DIAGNOSTICS
                    refreshTelemetry()
                    triggerHudNotification("DIAGNOSTICS SCAN COMPLETE")
                }
            }

            // 5. Insert Maya response
            chatDao.insertMessage(
                ChatMessage(
                    text = response.spokenText,
                    sender = MessageSender.MAYA,
                    mode = currentMode,
                    actionType = actionType,
                    actionPayload = actionData
                )
            )

            _isThinking.value = false

            // 6. Speak response
            voiceManager.speak(response.spokenText, currentMode)
        }
    }

    fun executeQuickAction(label: String) {
        when {
            label.contains("YouTube", ignoreCase = true) -> {
                sendMessage("Boss, open YouTube for me please")
            }
            label.contains("Instagram", ignoreCase = true) -> {
                sendMessage("Boss, let's open Instagram")
            }
            label.contains("Reel", ignoreCase = true) || label.contains("Script", ignoreCase = true) -> {
                sendMessage("Maya, write a viral 30-second Reel script on trending AI tech hacks")
            }
            label.contains("Status", ignoreCase = true) || label.contains("Diagnostics", ignoreCase = true) -> {
                sendMessage("Maya, run full system diagnostics")
            }
            label.contains("Task", ignoreCase = true) -> {
                sendMessage("Add task: Finish video edit and export final master")
            }
            label.contains("Search", ignoreCase = true) -> {
                sendMessage("Search the web for top trending tech topics today")
            }
            else -> {
                sendMessage(label)
            }
        }
    }

    fun addTask(title: String, description: String, category: TaskCategory, priority: TaskPriority, dueTime: String) {
        viewModelScope.launch {
            taskDao.insertTask(
                MayaTask(
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    dueTimeLabel = dueTime
                )
            )
            triggerHudNotification("TASK REGISTERED: $title")
        }
    }

    fun toggleTaskComplete(task: MayaTask) {
        viewModelScope.launch {
            taskDao.setTaskCompleted(task.id, !task.isCompleted)
            triggerHudNotification(if (!task.isCompleted) "TASK COMPLETED!" else "TASK REOPENED")
        }
    }

    fun deleteTask(task: MayaTask) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
            triggerHudNotification("TASK PURGED FROM MATRIX")
        }
    }

    fun generateNewScript(topic: String, platform: ScriptPlatform) {
        if (topic.isBlank() || _isThinking.value) return
        viewModelScope.launch {
            _isThinking.value = true
            triggerHudNotification("GENERATING SCRIPT IN CYBER CORE...")
            val result = geminiRepository.generateScriptContent(topic, platform.displayName, _personalityMode.value)

            val newScript = ContentScript(
                title = topic.take(40),
                topic = topic,
                platform = platform,
                scriptBody = result,
                hook = "Attention Hook for $topic",
                callToAction = "Save & Share with creators!",
                tags = "#ContentCreator #${platform.name} #ViralHacks"
            )
            scriptDao.insertScript(newScript)
            _isThinking.value = false
            triggerHudNotification("SCRIPT READY & SAVED TO STUDIO!")
        }
    }

    fun deleteScript(script: ContentScript) {
        viewModelScope.launch {
            scriptDao.deleteScript(script)
            triggerHudNotification("SCRIPT DELETED")
        }
    }

    fun refreshTelemetry() {
        _telemetry.value = commandDispatcher.getSystemTelemetry()
    }

    fun triggerHudNotification(message: String) {
        viewModelScope.launch {
            _hudNotification.value = message
            delay(3200)
            if (_hudNotification.value == message) {
                _hudNotification.value = null
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatDao.clearHistory()
            triggerHudNotification("CHAT MATRIX PURGED")
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.shutdown()
    }
}
