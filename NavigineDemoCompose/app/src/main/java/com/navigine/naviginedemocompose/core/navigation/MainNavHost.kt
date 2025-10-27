package com.navigine.naviginedemocompose.core.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.navigine.naviginedemocompose.ui.locations.LocationsScreen
import com.navigine.naviginedemocompose.ui.navigation.NavigationScreen
import com.navigine.naviginedemocompose.ui.profile.ProfileScreen

/**
 * Main NavHost with "top-level" destinations.
 * We keep them as independent graphs to enable multiple back stacks.
 */
@Composable
fun MainNavHost(
    rootController: NavHostController,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = TopLevelRoute.Locations.route,
    navigationVisible: Boolean = true
) {

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {

        navigationGraph(navigationVisible)
        locationsGraph()
        debugGraph()
        profileGraph(
            onRequireReLogin = {
                rootController.navigate(AppRoute.Login.route) {
                    popUpTo(AppRoute.Gate.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }
}

private fun NavGraphBuilder.locationsGraph() {
    composable(TopLevelRoute.Locations.route) { LocationsScreen() }
}

private fun NavGraphBuilder.navigationGraph(
    isVisible: Boolean
) {
    composable(TopLevelRoute.Navigation.route) { NavigationScreen(isVisible = isVisible) }
}

private fun NavGraphBuilder.debugGraph() {
    composable(TopLevelRoute.Debug.route) {
        Text("debug")
    }
}

private fun NavGraphBuilder.profileGraph(
    onRequireReLogin: () -> Unit
) {
    composable(TopLevelRoute.Profile.route) {
        ProfileScreen(onRequireReLogin = onRequireReLogin)
    }
}