package com.navigine.locationview.internal.listeners

import com.navigine.idl.java.Building
import com.navigine.idl.java.BuildingListener
import com.navigine.idl.java.Sublocation

internal class BuildingListenerBridge(
    private val onBuildingFocused: (
        sublocations: List<Sublocation>,
        activeSublocationId: Int,
        switchSublocation: (sublocationId: Int) -> Unit,
    ) -> Unit,
    private val onBuildingLeft: () -> Unit,
    private val onActiveSubLocationChanged: (sublocationId: Int) -> Unit,
) : BuildingListener() {

    override fun onActiveBuildingFocused(building: Building) {
        onBuildingFocused(
            building.sublocations,
            building.activeSublocationId,
            { id -> building.activeSublocationId = id}
        )
    }

    override fun onActiveBuildingLeft() {
        onBuildingLeft()
    }

    override fun onActiveSublocationChanged(activeSublocationId: Int) {
        onActiveSubLocationChanged(activeSublocationId)
    }
}