package com.example.navnreadmobile

import RssViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

import androidx.compose.runtime.Composable

import androidx.navigation.compose.rememberNavController
import com.example.navnreadmobile.navigation.AppNavGraph


import com.example.navnreadmobile.ui.theme.NavNReadMobileTheme


class MainActivity : ComponentActivity() {

    private val viewModel: RssViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            NavNReadMobileTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainApp(viewModel)
                }
            }
        }
    }
}
@Composable
fun MainApp(viewModel: RssViewModel) {
    val navController = rememberNavController()
    AppNavGraph(navController = navController, viewModel = viewModel)
}

