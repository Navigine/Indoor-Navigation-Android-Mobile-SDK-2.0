package com.navigine.locationview.internal

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.MainThread
import com.navigine.view.DefaultNavigationView

internal class DefaultLocationViewHolder {

    private var _view: DefaultNavigationView? = null

    val view: DefaultNavigationView?
        get() = _view

    @MainThread
    fun createView(context: Context): DefaultNavigationView {
        val lv = DefaultNavigationView(context)
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