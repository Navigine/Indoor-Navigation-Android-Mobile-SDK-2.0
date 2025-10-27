package com.navigine.naviginedemocompose.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.navigine.naviginedemocompose.core.navigation.MainNavHost
import com.navigine.naviginedemocompose.core.navigation.TopLevelRoute
import com.navigine.naviginedemocompose.core.navigation.MainBottomBar

/**
 * Single-activity scaffold with a BottomBar and a single NavHost.
 * Uses Navigation's built-in multiple back stacks (2.7+), so the Map screen
 * state is preserved and not recreated on tab switches.
 */
@Composable
fun MainScaffold(
    navController: NavHostController,  //for navigation between root screens
    initialSublocationId: Int? = null,
    initialVenueId: Int? = null
) {

    var current by rememberSaveable { mutableStateOf(TopLevelRoute.Locations) }

    val locationsNav  = rememberNavController()
    val navigationNav = rememberNavController()
    val debugNav      = rememberNavController()
    val profileNav    = rememberNavController()

    val tabs = remember {
        listOf(
            TopLevelRoute.Locations to locationsNav,
            TopLevelRoute.Navigation to navigationNav,
            TopLevelRoute.Debug     to debugNav,
            TopLevelRoute.Profile   to profileNav
        )
    }

    fun popToRootOf(route: TopLevelRoute, controller: NavHostController) {
        controller.navigate(route.route) {
            popUpTo(controller.graph.startDestinationId) { inclusive = false }
            launchSingleTop = true
        }
    }

    Scaffold(
        bottomBar = {
            MainBottomBar(
                current = current,
                destinations = tabs.map { it.first },
                onSelect = { dest ->
                    val (route, controller) = tabs.first { it.first == dest }
                    if (dest == current) {
                        popToRootOf(route, controller)
                    } else {
                        current = dest
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            tabs.forEach { (route, controller) ->
                KeepAliveTab(visible = current == route) {
                    MainNavHost(
                        rootController = navController,
                        navController = controller,
                        startDestination = route.route,
                        modifier = Modifier.fillMaxSize(),
                        navigationVisible = current == TopLevelRoute.Navigation
                    )
                }
            }
        }
    }

    //for qr parameters
    LaunchedEffect(initialSublocationId, initialVenueId) {
        if (initialSublocationId != null) {
            current = TopLevelRoute.Navigation
            navigationNav.navigate(TopLevelRoute.Navigation.route) {
                popUpTo(navigationNav.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
            navigationNav.currentBackStackEntry
                ?.savedStateHandle
                ?.apply {
                    set("initial_subloc", initialSublocationId)
                    initialVenueId?.let { set("initial_venue_id", it) }
                }
        }
    }
}

@Composable
private fun KeepAliveTab(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    // important: dont use if (visible) {...}
    Box(
        Modifier
            .fillMaxSize()
            .zIndex(if (visible) 1f else 0f)
            .alpha(if (visible) 1f else 0f)
    ) {
        content()
    }
}