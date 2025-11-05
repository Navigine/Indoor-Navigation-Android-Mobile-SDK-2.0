package com.navigine.naviginedemocompose.core.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.navigine.naviginedemocompose.R

/**
 * Top-level destinations shown in the BottomBar.
 * Each one has its own back stack thanks to Navigation's multiple back stacks support.
 */
enum class TopLevelRoute(
    val route: String,
    @StringRes val labelRes: Int,
    @DrawableRes val icon: Int
) {
    Navigation("navigation", R.string.tab_navigation, R.drawable.ic_navigation),
    Locations("locations", R.string.tab_locations, R.drawable.ic_locations),
    Debug("debug", R.string.tab_debug, R.drawable.ic_debug),
    Profile("profile", R.string.tab_profile, R.drawable.ic_profile),
}