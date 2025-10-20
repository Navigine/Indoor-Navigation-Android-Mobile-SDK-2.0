package com.navigine.naviginedemocompose.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.navigine.naviginedemocompose.data.local.UserStore
import com.navigine.naviginedemocompose.ui.auth.GateViewModel
import com.navigine.naviginedemocompose.ui.auth.LoginScreen
import com.navigine.naviginedemocompose.ui.screens.MainScaffold
import com.navigine.naviginedemocompose.ui.auth.PermissionGateScreen

sealed class AppRoute(val route : String){
    data object Gate : AppRoute("gate")
    data object Login : AppRoute("login")
    data object Main : AppRoute("main?loc={loc}&subloc={subloc}")
}

/**
 * Root graph:
 *   Gate (permissions) -> Login -> Main (bottom bar)
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Gate.route,
        modifier = modifier
    ) {
        composable(AppRoute.Gate.route) {

            val vm : GateViewModel = hiltViewModel()

            val sdkState by vm.sdkState.collectAsState()
            val hash: String? by vm.userHash.collectAsState(initial = null)
            var permissionsReady by remember { mutableStateOf(false) }
            var navigated by rememberSaveable { mutableStateOf(false) }

            LaunchedEffect(permissionsReady, hash, sdkState) {
                if (!navigated && permissionsReady && hash != null) {
                    when {
                        hash!!.isBlank() -> {
                            navController.navigate(AppRoute.Login.route) {
                                popUpTo(AppRoute.Gate.route) { inclusive = true }
                                launchSingleTop = true
                            }
                            navigated = true
                        }
                        sdkState is com.navigine.naviginedemocompose.core.sdk.NavigineSdkManager.SdkState.Ready -> {
                            navController.navigate(AppRoute.Main.route) {
                                popUpTo(AppRoute.Gate.route) { inclusive = true }
                                launchSingleTop = true
                            }
                            navigated = true
                        }
                        else -> Unit
                    }
                }
            }

            PermissionGateScreen(viewmodel = vm, onAllGranted = { permissionsReady = true })
        }
        composable(AppRoute.Login.route) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(AppRoute.Main.route){
                        popUpTo(AppRoute.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onOpenMain = { loc, subloc ->
                    val l  = (loc ?: -1L)
                    val sl = (subloc ?: -1L)
                    navController.navigate("main?loc=$l&subloc=$sl") {
                        popUpTo(AppRoute.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = AppRoute.Main.route,
            arguments = listOf(
                navArgument("loc") { type = NavType.IntType; defaultValue = -1},
                navArgument("subloc") { type = NavType.IntType; defaultValue = -1}
            )
        ) { backStackEntry ->
            val loc    = backStackEntry.arguments?.getInt("loc")?.takeIf { it > 0 }
            val subloc = backStackEntry.arguments?.getInt("subloc")?.takeIf { it > 0 }
            MainScaffold(navController, loc, subloc)
        }
    }
}