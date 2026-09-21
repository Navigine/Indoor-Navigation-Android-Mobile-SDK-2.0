package com.navigine.locationview.tracking.internal

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.MainThread
import com.navigine.view.DefaultNavigineViewConfig
import com.navigine.view.DefaultTrackingView
import com.navigine.view.widgets.FloorSelectorViewConfig
import com.navigine.view.widgets.ZoomControlsConfig

internal class DefaultTrackingViewHolder {

    private var _view : DefaultTrackingView? = null

    val view : DefaultTrackingView?
        get() = _view

    @MainThread
    fun createView(
        context: Context,
        viewConfig: DefaultNavigineViewConfig,
        zoomConfig: ZoomControlsConfig?,
        floorConfig: FloorSelectorViewConfig?,
    ): DefaultTrackingView {
        val lv = DefaultTrackingView(context, null, viewConfig, zoomConfig, floorConfig)
        _view = lv
        return lv
    }

    @MainThread
    fun onStart() {
        _view?.onStart()
    }

    @MainThread
    fun onStop() {
        _view?.onStop()
    }

    @MainThread
    fun onLowMemory() {
        _view?.onLowMemory()
    }

    @MainThread
    fun clear() {
        _view?.let { view ->
            runCatching { view.onStop() }
            runCatching { view.onLowMemory() }
            if (view is ViewGroup) {
                runCatching { view.removeAllViews() }
            }
        }
        _view = null
    }
}