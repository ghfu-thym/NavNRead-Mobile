package com.example.navnreadmobile.navigation

object Navigation {
    const val MAIN_SCREEN = "main"
    const val DETAIL_SCREEN = "detail/{title}/{description}/{imageUrl}/{link}"

    fun detailScreenRoute(
        title: String,
        description: String,
        imageUrl: String,
        link: String
    ): String {
        return "detail/${title}/${description}/${imageUrl}/${link}"
    }
}