package com.example.navnreadmobile.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class SpeechRecognitionManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    init {
        initializeTextToSpeech()
    }

    private fun initializeTextToSpeech() {
        try {
            Log.d("SpeechRecognitionManager", "Initializing TextToSpeech")
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    try {
                        val result = textToSpeech?.setLanguage(Locale("vi", "VN"))
                        Log.d("SpeechRecognitionManager", "Set language result: $result")

                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("SpeechRecognitionManager", "Vietnamese not supported, falling back to default")
                            textToSpeech?.language = Locale.getDefault()
                        }

                        textToSpeech?.setPitch(1.0f)
                        textToSpeech?.setSpeechRate(0.9f)
                        isInitialized = true

                        // Test if TTS is working with a short utterance
                        val testParams = Bundle()
                        testParams.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "testUtterance")
                        //textToSpeech?.speak("test", TextToSpeech.QUEUE_FLUSH, testParams, "testUtterance")

                        Log.d("SpeechRecognitionManager", "TTS initialized successfully")
                    } catch (e: Exception) {
                        Log.e("SpeechRecognitionManager", "TTS language error: ${e.message}")
                    }
                } else {
                    Log.e("SpeechRecognitionManager", "TTS init failed with status: $status")
                }
            }
        } catch (e: Exception) {
            Log.e("SpeechRecognitionManager", "Error creating TTS: ${e.message}")
        }
    }

    fun startListening(
        onPartialResult: ((String) -> Unit)? = null,
        onFinalResult: (String) -> Unit
    ) {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        }

        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                vibrate(context)
            }

            override fun onBeginningOfSpeech() {
                Log.d("SpeechRecognizer", "Speech has started")
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d("SpeechRecognizer", "Speech has ended")
            }

            override fun onError(error: Int) {
                if (error != SpeechRecognizer.ERROR_CLIENT) {
                    Log.d("SpeechRecognizer", "Error code: $error")
                    onFinalResult("Lỗi nhận diện giọng nói")
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onFinalResult(matches[0])
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onPartialResult?.invoke(matches[0])
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(speechRecognizerIntent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    fun speakText(text: String) {
        Log.d("SpeechRecognitionManager", "Speaking text: '$text', initialized: $isInitialized")

        if (text.isEmpty()) {
            Log.e("SpeechRecognitionManager", "Cannot speak empty text")
            return
        }

        if (!isInitialized || textToSpeech == null) {
            Log.e("SpeechRecognitionManager", "TTS not initialized, reinitializing")
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

        textToSpeech?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(id: String?) {
                Log.d("SpeechRecognitionManager", "TTS started speaking: $id")
            }

            override fun onDone(id: String?) {
                Log.d("SpeechRecognitionManager", "TTS finished speaking: $id")
            }

            override fun onError(id: String?) {
                Log.e("SpeechRecognitionManager", "TTS error with utterance: $id")
            }
        })

        val bundle = Bundle()
        bundle.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)

        // Try direct API first (most reliable)
        val result = textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, bundle, utteranceId)
        Log.d("SpeechRecognitionManager", "TTS speak result: $result for text: '$text'")
    }

    fun release() {
        speechRecognizer?.destroy()
        speechRecognizer = null

        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }

    private fun vibrate(context: Context){
        val vibrator = context.getSystemService((Context.VIBRATOR_SERVICE)) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }
}

