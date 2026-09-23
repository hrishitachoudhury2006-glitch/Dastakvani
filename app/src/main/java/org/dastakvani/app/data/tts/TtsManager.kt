package org.dastakvani.app.data.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Try Hindi first, fallback to English
                val hindiLocale = Locale("hi", "IN")
                val langResult = textToSpeech?.setLanguage(hindiLocale)
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech?.setLanguage(Locale.ENGLISH)
                }
                textToSpeech?.setSpeechRate(0.9f) // Slightly slower for low-literacy clarity
                isTtsReady = true

                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            } else {
                isTtsReady = false
            }
        }
    }

    /**
     * Speaks the Proof of Hearing confirmation back to the citizen.
     * e.g., "आप कह रहे हैं कि स्कूल में शिक्षक नहीं आ रहे हैं। क्या यह सही है?"
     */
    fun speakProofOfHearing(transcript: String, onDone: (() -> Unit)? = null) {
        val speechText = "आप कह रहे हैं कि: $transcript । क्या यह सही है? पुष्टि करने के लिए हाँ दबाएं।"
        speak(speechText, onDone)
    }

    /**
     * Speaks complaint status aloud for the citizen.
     */
    fun speakStatus(complaintId: String, statusText: String, categoryName: String) {
        val speechText = "शिकायत संख्या $complaintId, श्रेणी $categoryName की वर्तमान स्थिति है: $statusText।"
        speak(speechText)
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isTtsReady) return
        stop()
        val utteranceId = "utterance_${System.currentTimeMillis()}"
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        textToSpeech?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        try {
            stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            textToSpeech = null
            isTtsReady = false
        }
    }
}
