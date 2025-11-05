package com.navigine.naviginedemocompose.core.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy

/**
 * Bottom bar wired for multiple back stacks:
 * - saveState on popUpTo()
 * - restoreState on navigate()
 * - launchSingleTop to avoid piling destinations
 */
@Composable
fun MainBottomBar(
    current: TopLevelRoute,
    destinations: List<TopLevelRoute>,
    onSelect: (TopLevelRoute) -> Unit,
    modifier: Modifier = Modifier,
) {

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        destinations.forEach { dest ->
            NavigationBarItem(
                selected = current == dest,
                alwaysShowLabel = true,
                onClick = { onSelect(dest) },
                icon = {
                    Icon(painterResource(dest.icon), contentDescription = null)
                },
                label = { Text(stringResource(dest.labelRes)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

private fun NavDestination?.isOnDestination(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}