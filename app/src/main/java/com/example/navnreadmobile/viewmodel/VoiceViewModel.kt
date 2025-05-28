package com.example.navnreadmobile.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VoiceViewModel : ViewModel() {
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _spokenText = MutableStateFlow<String?>(null)
    val spokenText: StateFlow<String?> = _spokenText

    fun startListening() {
        _isListening.value = true
        _spokenText.value = null // Reset text cũ
    }

    fun stopListening(text: String? = null) {
        _isListening.value = false
        _spokenText.value = text
    }

    fun clearSpokenText() {
        _spokenText.value = null
    }
}