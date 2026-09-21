package com.navigine.locationview.utils

import android.opengl.GLSurfaceView
import android.view.View
import android.view.ViewGroup

internal fun findGlChild(root: View): View? {
    if (root is GLSurfaceView) return root
    if (root is ViewGroup) {
        for (i in 0 until root.childCount) {
            val c = root.getChildAt(i)
            val found = findGlChild(c)
            if (found != null) return found
        }
    }
    return null
}