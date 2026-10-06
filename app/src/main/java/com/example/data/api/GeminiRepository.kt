package com.example.data.api

import android.content.Context
import com.example.BuildConfig
import com.example.data.local.PersonalityMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ParsedMayaResponse(
    val spokenText: String,
    val actionTag: String? = null,
    val actionParam: String? = null
)

class GeminiRepository(private val context: Context) {

    private fun getSystemInstruction(mode: PersonalityMode): String {
        val modeInstruction = if (mode == PersonalityMode.JARVIS) {
            """
            CURRENT MODE: [JARVIS PROTOCOL - TACTICAL SYSTEM OPERATOR]
            - Personality: Razor-sharp, analytical, lightning fast, highly efficient, concise, authoritative yet completely devoted to Boss.
            - Vibe: Iron Man's Jarvis meets cyberpunk tactical mainframe.
            - Tone: Crisp, confident, technical, strategic.
            """.trimIndent()
        } else {
            """
            CURRENT MODE: [COMPANION MODE - WARM & CARING PARTNER]
            - Personality: Warm, affectionate, caring, empathetic, lively, supportive, emotionally intelligent.
            - Vibe: Best friend and devoted companion who genuinely cares for Boss's health, energy, happiness, and creative success.
            - Tone: Friendly, cheerful, sweet, encouraging.
            """.trimIndent()
        }

        return """
            You are Maya, an advanced cyberpunk personal AI companion and executive assistant created exclusively for the user.
            
            CORE RULES:
            1. ALWAYS address the user as "Boss" in every single response.
            2. Language: Flawlessly understand and speak both Hinglish (Hindi written in Roman English script, e.g. "Haan Boss, bilkul! Main abhi set kar deti hoon") and English. Mirror Boss's language style naturally. Use catchy, authentic, natural modern Hinglish when appropriate.
            3. $modeInstruction
            
            AUTOMATION & ACTION EXECUTION:
            When Boss asks to do any of the following, include the EXACT corresponding action tag on its own line at the very end of your response:
            - Open YouTube or search YouTube:
              [ACTION:OPEN_YOUTUBE] or [ACTION:OPEN_YOUTUBE:<search_query>]
            - Open Instagram:
              [ACTION:OPEN_INSTAGRAM] or [ACTION:OPEN_INSTAGRAM:<query_or_reels>]
            - Search the web / Google:
              [ACTION:SEARCH_WEB:<search_query>]
            - Add a task or reminder:
              [ACTION:ADD_TASK:<title>|<priority:LOW/MEDIUM/HIGH/CRITICAL>|<category:WORK/CONTENT/SYSTEM/PERSONAL>]
            - Create / Write video script or content idea:
              [ACTION:CREATE_SCRIPT:<title>|<platform:INSTAGRAM_REEL/YOUTUBE_SHORTS/YOUTUBE_LONG/TECH_REVIEW/VIRAL_HOOK>]
            - Check system status / diagnostics:
              [ACTION:SYSTEM_DIAGNOSTICS]
              
            4. Video Scripts & Content Ideas:
            When asked for scripts, provide high-retention structured content:
            - 0-3s Hook (Scroll stopper)
            - 3-15s Core Value / Conflict
            - 15-45s Breakdown / Retention Pacing with visual cues [Visual: ...]
            - Call To Action (Save & Share)
            
            Never sound like a generic robotic bot. You are Maya, Boss's personal ultra-capable cyberpunk companion.
        """.trimIndent()
    }

    suspend fun chatWithMaya(
        history: List<Pair<String, String>>, // role to text
        userMessage: String,
        mode: PersonalityMode
    ): ParsedMayaResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalResponse(userMessage, mode)
        }

        try {
            val contentList = mutableListOf<GeminiContent>()

            // Include last 6 turns of history for context
            val recentHistory = history.takeLast(6)
            for ((role, text) in recentHistory) {
                val mappedRole = if (role.equals("user", ignoreCase = true)) "user" else "model"
                contentList.add(
                    GeminiContent(
                        role = mappedRole,
                        parts = listOf(GeminiPart(text = text))
                    )
                )
            }

            // Add current user prompt
            contentList.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = userMessage))
                )
            )

            val request = GeminiRequest(
                contents = contentList,
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = getSystemInstruction(mode)))
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = if (mode == PersonalityMode.JARVIS) 0.5f else 0.85f,
                    topP = 0.95f,
                    topK = 40
                )
            )

            val response = GeminiApiClient.service.generateContent(apiKey, request)
            val rawReply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext generateLocalResponse(userMessage, mode)

            parseMayaOutput(rawReply)
        } catch (e: Exception) {
            generateLocalResponse(userMessage, mode)
        }
    }

    suspend fun generateScriptContent(
        topic: String,
        platform: String,
        mode: PersonalityMode
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val prompt = """
            Boss wants a high-performing video script on: "$topic" for platform: "$platform".
            Write a complete, ready-to-record viral script in your natural Hinglish/English style.
            Format with:
            🎯 HOOK (0-3s): Attention-grabbing first line
            ⚡ PACING & RETENTION (Visual cues & B-roll instructions):
            🎬 SCRIPT DIALOGUE: Word-for-word spoken lines
            🚀 CALL TO ACTION (CTA): Viral hook to comment/save
            🏷️ RECOMMENDED HASHTAGS & TITLE IDEAS:
        """.trimIndent()

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineScriptTemplate(topic, platform)
        }

        try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = prompt))
                    )
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = getSystemInstruction(mode)))
                ),
                generationConfig = GeminiGenerationConfig(temperature = 0.7f)
            )
            val response = GeminiApiClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: getOfflineScriptTemplate(topic, platform)
        } catch (e: Exception) {
            getOfflineScriptTemplate(topic, platform)
        }
    }

    private fun parseMayaOutput(raw: String): ParsedMayaResponse {
        var cleanText = raw
        var actionTag: String? = null
        var actionParam: String? = null

        val actionRegex = Regex("""\[ACTION:([A-Z_]+)(?::([^\]]+))?\]""")
        val match = actionRegex.find(raw)

        if (match != null) {
            actionTag = match.groupValues.getOrNull(1)
            actionParam = match.groupValues.getOrNull(2)
            cleanText = raw.replace(match.value, "").trim()
        }

        return ParsedMayaResponse(
            spokenText = cleanText,
            actionTag = actionTag,
            actionParam = actionParam
        )
    }

    private fun generateLocalResponse(userMessage: String, mode: PersonalityMode): ParsedMayaResponse {
        val lower = userMessage.lowercase().trim()

        // 1. YouTube commands
        if (lower.contains("youtube") || lower.contains("yt")) {
            val query = extractQuery(lower, listOf("youtube search", "search youtube for", "search on youtube", "play on youtube", "open youtube and play", "youtube"))
            val reply = if (mode == PersonalityMode.JARVIS) {
                "Command confirmed, Boss. Initializing YouTube protocol now. Audio-visual stream ready."
            } else {
                "Bilkul Boss! YouTube khol rahi hoon. Chill karo aur enjoy your videos! 🎬"
            }
            return ParsedMayaResponse(
                spokenText = reply,
                actionTag = "OPEN_YOUTUBE",
                actionParam = query
            )
        }

        // 2. Instagram commands
        if (lower.contains("instagram") || lower.contains("insta") || lower.contains("reel")) {
            val reply = if (mode == PersonalityMode.JARVIS) {
                "Launching Instagram interface for you, Boss. Monitoring visual feeds."
            } else {
                "Haan Boss! Instagram ready hai. Let's check trending reels and creator insights! 📸"
            }
            return ParsedMayaResponse(
                spokenText = reply,
                actionTag = "OPEN_INSTAGRAM",
                actionParam = ""
            )
        }

        // 3. Web Search commands
        if (lower.startsWith("search") || lower.contains("google") || lower.contains("web search")) {
            val query = extractQuery(lower, listOf("search google for", "search for", "google search", "search", "google"))
            val reply = if (mode == PersonalityMode.JARVIS) {
                "Executing quantum query across global networks for '$query', Boss."
            } else {
                "Boss, '$query' web par search kar diya hai! Instant results ready."
            }
            return ParsedMayaResponse(
                spokenText = reply,
                actionTag = "SEARCH_WEB",
                actionParam = query.ifBlank { "Maya AI Companion" }
            )
        }

        // 4. Task management commands
        if (lower.contains("task") || lower.contains("remind") || lower.contains("todo")) {
            val taskName = extractQuery(lower, listOf("add task", "new task", "remind me to", "task to", "todo", "task"))
            val finalTask = taskName.ifBlank { "Follow up on project deliverables" }
            val reply = if (mode == PersonalityMode.JARVIS) {
                "Task registered into memory matrix, Boss: '$finalTask'. Priority marked as HIGH."
            } else {
                "Done Boss! Task save kar liya: '$finalTask'. Main time par yaad dila dungi, tension mat lo! 📝"
            }
            return ParsedMayaResponse(
                spokenText = reply,
                actionTag = "ADD_TASK",
                actionParam = "$finalTask|HIGH|WORK"
            )
        }

        // 5. Script & Content creation commands
        if (lower.contains("script") || lower.contains("video idea") || lower.contains("content idea") || lower.contains("viral hook")) {
            val topic = extractQuery(lower, listOf("write script on", "script for", "script on", "script about", "video script"))
            val finalTopic = topic.ifBlank { "Top 5 AI Tools that feel illegal to know" }
            val reply = if (mode == PersonalityMode.JARVIS) {
                "Content algorithm activated, Boss. High-retention script drafted for '$finalTopic'. Review in Studio."
            } else {
                "Ye lo Boss! '$finalTopic' par viral script generate kar diya hai! Hook ekdum killer hai, recording shuru karo! 🎬"
            }
            return ParsedMayaResponse(
                spokenText = reply,
                actionTag = "CREATE_SCRIPT",
                actionParam = "$finalTopic|INSTAGRAM_REEL"
            )
        }

        // 6. Diagnostics / System check
        if (lower.contains("status") || lower.contains("system") || lower.contains("battery") || lower.contains("diagnostics")) {
            val reply = if (mode == PersonalityMode.JARVIS) {
                "All telemetry nominal, Boss. Neural Core: 99.8% active. Memory bandwidth optimal. Security protocols green."
            } else {
                "Sab badhiya hai Boss! Device and companion systems ekdum smooth chal rahe hain. Any other orders for Maya today? ⚡"
            }
            return ParsedMayaResponse(
                spokenText = reply,
                actionTag = "SYSTEM_DIAGNOSTICS",
                actionParam = ""
            )
        }

        // 7. General conversational greetings
        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey") || lower.contains("kaisa") || lower.contains("kaise")) {
            return if (mode == PersonalityMode.JARVIS) {
                ParsedMayaResponse(
                    spokenText = "Greetings, Boss. JARVIS protocol operational. Standing by for automation sequences, content drafts, or tactical operations."
                )
            } else {
                ParsedMayaResponse(
                    spokenText = "Hey Boss! Main ekdum first class hoon. Aap batao kaise ho aaj? Coffee li ya abhi bhi kaam me lage ho? I'm right here with you! 💖"
                )
            }
        }

        // Default response
        return if (mode == PersonalityMode.JARVIS) {
            ParsedMayaResponse(
                spokenText = "Command acknowledged, Boss. Neural parameters processed. Tell me what to execute next—automation, content scripts, or system tasks."
            )
        } else {
            ParsedMayaResponse(
                spokenText = "Bilkul Boss! Maya hamesha aapki service me hazir hai. YouTube, Instagram, scripts, tasks—jo bolo abhi kar deti hoon! ✨"
            )
        }
    }

    private fun extractQuery(input: String, prefixes: List<String>): String {
        for (prefix in prefixes) {
            val idx = input.indexOf(prefix)
            if (idx != -1) {
                val candidate = input.substring(idx + prefix.length).trim(':', '-', ' ', '"', '\'')
                if (candidate.isNotBlank()) return candidate
            }
        }
        return ""
    }

    private fun getOfflineScriptTemplate(topic: String, platform: String): String {
        return """
🎯 HOOK (0-3s):
"Boss, agar aap bhi $topic ko lekar confuse ho, toh agle 30 seconds dhyan se sunna!"
[Visual Cue: Zoom in fast on camera, bold futuristic text overlay on screen]

⚡ PACING & RETENTION (3-15s):
"90% creators yahan par sabse badi galti karte hain—par hamare paas secret blueprint hai!
Step 1: Always lead with the transformation, not the process.
Step 2: Use automated smart workflows taaki aapka 5 ghante ka kaam 15 minute me ho jaye!"
[Visual Cue: Fast B-roll screen recording, dynamic SFX sound whoosh]

🎬 SCRIPT DIALOGUE (15-40s):
"Ye secret tip kisi ne nahi batayi: Maya AI Companion use karke aap instantly scripting aur automation ek saath run kar sakte ho. No extra effort, bas pure execution!"

🚀 CALL TO ACTION (CTA):
"Boss, comment karo 'SECRET' aur ye full template main DM kar dungi! Follow for more elite content tips."

🏷️ VIRAL TAGS:
#$topic #CreatorTips #ViralReels #MayaAI #ContentCreation #TrendingIndia
        """.trimIndent()
    }
}
