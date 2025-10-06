package com.navigine.locationview.internal

import android.content.Context
import androidx.annotation.MainThread
import com.navigine.view.LocationView

internal class LocationViewHolder {

    private var _view: LocationView? = null

    val view: LocationView?
        get() = _view

    @MainThread
    fun createView(context: Context): LocationView {
        val lv = LocationView(context)
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
        _view = null
    }
}