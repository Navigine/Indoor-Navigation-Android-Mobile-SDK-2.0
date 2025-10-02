package com.navigine.locationviewcompose.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private data class DemoTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val tabs = listOf(
    DemoTab(route = "camera", label = "Camera", icon = Icons.Filled.Settings),
    DemoTab(route = "shapes", label = "Shapes", icon = Icons.Filled.Star),
    DemoTab(route = "input",  label = "Input",  icon = Icons.Filled.ThumbUp),
    DemoTab(route = "icons",  label = "Icons",  icon = Icons.Filled.Place),
)


@Composable
fun SamplesNav() {
    val navController = rememberNavController()
    val backstackEntry by navController.currentBackStackEntryAsState()
    val current = backstackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = current.isRouteInHierarchy(tab.route)
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = tabs.first().route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable("camera") {
                MapCameraDemo(Modifier.fillMaxSize()) // your demo
            }
            composable("shapes") {
                MapShapesDemo(Modifier.fillMaxSize()) // your demo
            }
            composable("input") {
                MapInputPickDemo(Modifier.fillMaxSize()) // your demo
            }
            composable("icons") {
                MapIconsList(Modifier.fillMaxSize()) // your demo
            }
        }
    }
}

private fun NavDestination?.isRouteInHierarchy(route: String): Boolean {
    var current = this
    while (current != null) {
        if (current.route == route) return true
        current = current.parent
    }
    return false
}