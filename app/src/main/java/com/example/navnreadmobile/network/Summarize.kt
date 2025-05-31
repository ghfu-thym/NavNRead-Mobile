package com.example.navnreadmobile.network


import com.google.ai.client.generativeai.GenerativeModel
import com.example.navnreadmobile.BuildConfig

class Summarize {
    val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-8B",
        apiKey = BuildConfig.GEMINI_API_KEY
    )
}