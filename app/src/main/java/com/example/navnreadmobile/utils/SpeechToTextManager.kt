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
import android.util.Log

/**
 * Lớp quản lý Speech-to-Text (STT) - chuyển giọng nói thành văn bản
 */
class SpeechToTextManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

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
                Log.d(TAG, "Speech has started")
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d(TAG, "Speech has ended")
            }

            override fun onError(error: Int) {
                if (error != SpeechRecognizer.ERROR_CLIENT) {
                    Log.d(TAG, "Error code: $error")
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

    fun release() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun vibrate(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }

    companion object {
        private const val TAG = "SpeechToTextManager"
    }
}
