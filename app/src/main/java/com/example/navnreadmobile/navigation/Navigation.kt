package com.example.navnreadmobile.navigation

object Navigation {
    const val MAIN_SCREEN = "main"
    const val DETAIL_SCREEN = "detail/{title}/{description}/{imageUrl}/{link}"
    const val VOICE_COMMAND_SCREEN = "voice_command"
    const val SEARCH_SCREEN = "search"
    const val SETTINGS_SCREEN = "settings"

    fun detailScreenRoute(
        title: String,
        description: String,
        imageUrl: String,
        link: String
    ): String {
        return "detail/${title}/${description}/${imageUrl}/${link}"
    }
}