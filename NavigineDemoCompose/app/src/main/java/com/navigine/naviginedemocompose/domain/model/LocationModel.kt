package com.navigine.naviginedemocompose.domain.model

import com.navigine.idl.java.Location

data class LocationModel(
    val id: Int,
    val name: String,
    val location: Location? = null
)

sealed interface LocationEvent {
    data class Loaded(val location: LocationModel) : LocationEvent
    data class Uploaded(val progress: Int) : LocationEvent
    data class Failed(val code: Int, val message: String) : LocationEvent
}