package com.navigine.locationview.internal.listeners

import android.graphics.PointF
import com.navigine.idl.java.MapObjectPickResult
import com.navigine.idl.java.PickListener
import java.util.HashMap

/**
 * Bridges Navigine [PickListener] to Kotlin lambdas.
 */
public class PickListenerBridge(
    private val onObject: (result: MapObjectPickResult, viewPoint: PointF) -> Unit,
    private val onFeature: (attributes: Map<String?, String?>, viewPoint: PointF) -> Unit,
) : PickListener() {

    override fun onMapObjectPickComplete(
        result: MapObjectPickResult,
        point: PointF
    ) {
        onObject(result, point)
    }

    override fun onMapFeaturePickComplete(
        attrs: HashMap<String?, String?>,
        point: PointF
    ) {
        onFeature(attrs, point)
    }

}