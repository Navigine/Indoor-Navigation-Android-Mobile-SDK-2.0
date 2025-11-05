package com.navigine.naviginedemocompose.ui.locations

import androidx.compose.runtime.Immutable
import com.navigine.naviginedemocompose.domain.model.LocationModel

@Immutable
data class LocationsState(
    val query: String = "",
    val items: List<LocationModel> = emptyList(),
    val selectedId: Int? = null,
    val isRefreshing: Boolean = false,
    val error: String? = null
)
