package com.example.navnreadmobile.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.navnreadmobile.ui.screen.DetailScreen
import com.example.navnreadmobile.ui.screen.MainScreen
import RssViewModel

@Composable
fun AppNavGraph(navController: NavHostController, viewModel: RssViewModel) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(navController, viewModel)
        }

        composable(
            route = "detail/{title}/{description}/{imageUrl}/{link}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("description") { type = NavType.StringType },
                navArgument("imageUrl") { type = NavType.StringType },
                navArgument("link") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: ""
            val description = backStackEntry.arguments?.getString("description") ?: ""
            val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
            val link = backStackEntry.arguments?.getString("link") ?: ""
            DetailScreen(
                title = title,
                description = description,
                imageUrl = imageUrl,
                link = link,
                viewModel = viewModel
            )
        }
    }
}
