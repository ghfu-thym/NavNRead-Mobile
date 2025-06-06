package com.example.navnreadmobile.utils

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * Lớp quản lý Text-to-Speech (TTS) - chuyển văn bản thành giọng nói
 */
class TextToSpeechManager(private val context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    init {
        initializeTextToSpeech()
    }

    private fun initializeTextToSpeech() {
        try {
            Log.d(TAG, "Initializing TextToSpeech")
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    try {
                        val result = textToSpeech?.setLanguage(Locale("vi", "VN"))
                        Log.d(TAG, "Set language result: $result")

                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e(TAG, "Vietnamese not supported, falling back to default")
                            textToSpeech?.language = Locale.getDefault()
                        }

                        textToSpeech?.setPitch(1.0f)
                        textToSpeech?.setSpeechRate(0.9f)
                        isInitialized = true

                        // Test if TTS is working with a short utterance
//                        val testParams = Bundle()
//                        testParams.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "testUtterance")
//                        textToSpeech?.speak("test", TextToSpeech.QUEUE_FLUSH, testParams, "testUtterance")

                        Log.d(TAG, "TTS initialized successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "TTS language error: ${e.message}")
                    }
                } else {
                    Log.e(TAG, "TTS init failed with status: $status")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating TTS: ${e.message}")
        }
    }

    fun stopSpeaking() {
        Log.d(TAG, "Stopping TTS speech")
        if (isInitialized && textToSpeech != null) {
            textToSpeech?.stop()
            Log.d(TAG, "TTS speech stopped")
        } else {
            Log.e(TAG, "Cannot stop speech - TTS not initialized")
        }
    }

    fun speakText(text: String) {
        Log.d(TAG, "Speaking text: '$text', initialized: $isInitialized")

        if (text.isEmpty()) {
            Log.e(TAG, "Cannot speak empty text")
            return
        }

        if (!isInitialized || textToSpeech == null) {
            Log.e(TAG, "TTS not initialized, reinitializing")
            initializeTextToSpeech()
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                attemptSpeak(text)
            }, 1000)
            return
        }

        attemptSpeak(text)
    }

    private fun attemptSpeak(text: String) {
        val utteranceId = "utterance_${System.currentTimeMillis()}"

        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {
                Log.d(TAG, "TTS started speaking: $id")
            }

            override fun onDone(id: String?) {
                Log.d(TAG, "TTS finished speaking: $id")
            }

            override fun onError(id: String?) {
                Log.e(TAG, "TTS error with utterance: $id")
            }
        })

        val bundle = Bundle()
        bundle.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)

        // Try direct API first (most reliable)
        val result = textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, bundle, utteranceId)
        Log.d(TAG, "TTS speak result: $result for text: '$text'")
    }

    fun release() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }

    companion object {
        private const val TAG = "TextToSpeechManager"
    }
}
