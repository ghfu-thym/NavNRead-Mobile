package com.example.navnreadmobile

import RssViewModel
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.navigation.compose.rememberNavController
import com.example.navnreadmobile.navigation.AppNavGraph


import com.example.navnreadmobile.ui.theme.NavNReadMobileTheme


class MainActivity : ComponentActivity() {

    private val viewModel: RssViewModel by viewModels()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            NavNReadMobileTheme {
//                Surface(color = MaterialTheme.colorScheme.background) {
//                    MainApp(viewModel)
//                }
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        Text(
                            text = "NavNRead",
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = Color.Red,
                            fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                            fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                        )
                        MainApp(viewModel)
                    }
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

