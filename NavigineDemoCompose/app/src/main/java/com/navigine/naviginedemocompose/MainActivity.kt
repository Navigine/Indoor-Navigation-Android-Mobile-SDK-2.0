package com.navigine.naviginedemocompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.navigine.naviginedemocompose.core.navigation.AppNavGraph
import com.navigine.naviginedemocompose.ui.theme.NavigineDemoComposeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NavigineDemoComposeTheme {
                val nav = rememberNavController()
                AppNavGraph(
                    navController = nav,
                )
            }
        }
    }
}
