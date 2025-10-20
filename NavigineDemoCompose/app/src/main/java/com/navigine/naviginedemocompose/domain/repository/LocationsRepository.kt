package com.navigine.naviginedemocompose.domain.repository

import com.navigine.naviginedemocompose.domain.model.LocationModel
import kotlinx.coroutines.flow.Flow

interface LocationsRepository {
    fun observeLocations(): Flow<List<LocationModel>>
    suspend fun selectLocation(id : Int)
    fun isCurrentLocation(locationId: Int): Boolean
    fun getSelectedLocationId() : Int?
    suspend fun refresh()
}