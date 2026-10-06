package com.example.automation

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.data.local.PersonalityMode
import java.util.Locale

class VoiceManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false
    var isVoiceEnabled = true

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.forLanguageTag("en-IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            isInitialized = true
        }
    }

    fun speak(text: String, mode: PersonalityMode) {
        if (!isVoiceEnabled || !isInitialized || text.isBlank()) return

        val cleanSpeech = cleanTextForSpeech(text)
        if (cleanSpeech.isBlank()) return

        tts?.stop()

        if (mode == PersonalityMode.JARVIS) {
            tts?.setPitch(0.92f)
            tts?.setSpeechRate(1.08f)
        } else {
            tts?.setPitch(1.15f)
            tts?.setSpeechRate(0.98f)
        }

        tts?.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, null, "MAYA_SPEECH_ID")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    private fun cleanTextForSpeech(input: String): String {
        return input
            .replace(Regex("""\[ACTION:[^\]]+\]"""), "")
            .replace(Regex("""[*#_`>~]"""), "")
            .replace(Regex("""[\uD83C-\uDBFF\uDC00-\uDFFF]+"""), "") // remove emojis for clean voice
            .replace(Regex("""\s+"""), " ")
            .trim()
    }
}
