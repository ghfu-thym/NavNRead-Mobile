package com.example.navnreadmobile.utils

import android.content.Context

/**
 * Lớp tích hợp quản lý cả Text-to-Speech và Speech-to-Text
 */
class SpeechManager(context: Context) {

    private val speechToTextManager = SpeechToTextManager(context)
    private val textToSpeechManager = TextToSpeechManager(context)

    /**
     * Bắt đầu nhận diện giọng nói
     */
    fun startListening(
        onPartialResult: ((String) -> Unit)? = null,
        onFinalResult: (String) -> Unit
    ) {
        speechToTextManager.startListening(onPartialResult, onFinalResult)
    }

    /**
     * Dừng nhận diện giọng nói
     */
    fun stopListening() {
        speechToTextManager.stopListening()
    }

    /**
     * Đọc văn bản thành giọng nói
     */
    fun speakText(text: String) {
        textToSpeechManager.speakText(text)
    }

    fun stopSpeaking() {
        textToSpeechManager.stopSpeaking()
    }

    /**
     * Giải phóng tài nguyên
     */
    fun release() {
        speechToTextManager.release()
        textToSpeechManager.release()
    }
}
