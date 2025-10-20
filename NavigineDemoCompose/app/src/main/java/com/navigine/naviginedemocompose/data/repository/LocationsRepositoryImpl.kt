package com.navigine.naviginedemocompose.data.repository

import android.util.Log
import com.navigine.idl.java.LocationInfo
import com.navigine.idl.java.LocationListListener
import com.navigine.naviginedemocompose.core.sdk.NavigineSdkManager
import com.navigine.naviginedemocompose.data.local.UserStore
import com.navigine.naviginedemocompose.domain.model.LocationModel
import com.navigine.naviginedemocompose.domain.repository.LocationsRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class LocationsRepositoryImpl @Inject constructor(
    private val sdk: NavigineSdkManager,
    private val userStore: UserStore
) : LocationsRepository {

    override fun observeLocations(): Flow<List<LocationModel>>  = callbackFlow {
        val llm = sdk.locationListManager

        // helper to push current snapshot (sorted by name)
        fun emitSnapshot() {
            val map = llm.locationList // HashMap<Int, LocationInfo>
            val list = map.entries.map { (id, info) ->
                LocationModel(id = id, name = info.name ?: id.toString())
            }.sortedBy { it.name.lowercase() }
            trySend(list)
        }

        // initial
        emitSnapshot()

        val listener = object : LocationListListener() {
            override fun onLocationListLoaded(list: HashMap<Int, LocationInfo>) {
                val ui = list.entries.map { (id, info) ->
                    LocationModel(id = id, name = info.name ?: id.toString())
                }.sortedBy { it.name.lowercase() }
                trySend(ui)
                Log.d("LocationsRepository", "onLocationListLoaded: $ui")
            }
            override fun onLocationListFailed(error: Error) {
                Log.e("LocationsRepository", "onLocationListFailed: $error")
            }
        }
        llm.addLocationListListener(listener)

        awaitClose { llm.removeLocationListListener(listener) }
    }

    override suspend fun selectLocation(id: Int) {
        sdk.locationManager.locationId = id
        userStore.saveLocationId(id)
    }

    override suspend fun refresh() {
        sdk.locationListManager.updateLocationList()
    }

    override fun isCurrentLocation(locationId: Int): Boolean = sdk.locationManager.locationId == locationId
    override fun getSelectedLocationId(): Int? = sdk.locationManager.locationId
}