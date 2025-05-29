package com.example.navnreadmobile.ui.component

import android.graphics.drawable.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.navnreadmobile.navigation.Navigation

@Composable
fun BottomNavBar (
    navController: NavController,
    currentRoute: String?
){
    NavigationBar {
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Tin tức"
                )
            },
            label = {
                Text(text = "Tin tức")
            },
            selected = currentRoute == Navigation.MAIN_SCREEN,
            onClick = {
                if(currentRoute != Navigation.MAIN_SCREEN){
                    navController.navigate(Navigation.MAIN_SCREEN){
                        popUpTo(Navigation.MAIN_SCREEN){
                            saveState = true
                            inclusive = true
                        }
                    }
                }
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Tìm kiếm"
                )
            },
            label = {
                Text(text = "Tìm kếm")
            },
            selected = currentRoute == Navigation.SEARCH_SCREEN,
            onClick = {
                if(currentRoute != Navigation.SEARCH_SCREEN){
                    navController.navigate(Navigation.SEARCH_SCREEN){
                        popUpTo(Navigation.MAIN_SCREEN)
                    }
                }
            }
        )


        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Face,
                    contentDescription = "Đọc báo"
                )
            },
            label = {
                Text(text = "AI")
            },
            selected = currentRoute == Navigation.VOICE_COMMAND_SCREEN,
            onClick = {
                if(currentRoute != Navigation.VOICE_COMMAND_SCREEN){
                    navController.navigate(Navigation.VOICE_COMMAND_SCREEN){
                        popUpTo(Navigation.MAIN_SCREEN)
                    }
                }
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Cài đặt",
                )
            },
            label = {
                Text(text = "Cài đặt")
            },
            selected = currentRoute == Navigation.SETTINGS_SCREEN,
            onClick = {
                if(currentRoute != Navigation.SETTINGS_SCREEN){
                    navController.navigate(Navigation.SETTINGS_SCREEN){
                        popUpTo(Navigation.MAIN_SCREEN)
                    }
                }
            }
        )

    }
}