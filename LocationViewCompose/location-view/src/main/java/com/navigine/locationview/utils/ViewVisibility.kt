package com.navigine.locationview.utils

import android.view.View
import android.view.ViewGroup

/**
 * Sets GONE on every *direct* child of [root].
 *
 * Deliberately single-level: root's direct children are exactly the GL surface
 * plus the SDK's top-level chrome widgets (ZoomControls, FloorSelectorView,
 * FollowMeButton), added via addView() in DefaultNavigineView/DefaultNavigationView.
 * Those widgets are themselves composite views with their own internal children
 * (e.g. individual floor buttons). Recursing into them would mark those internals
 * GONE too — but the restore path (DefaultNavigineView#applyConfigVisibility, invoked
 * via setViewConfig) only resets visibility on the top-level widget containers, not
 * their internals. Recursing here would leave those internals permanently GONE after
 * the first hide/show cycle. GONE (not INVISIBLE) matches SDK's own convention and
 * the historically-verified toggle for the GL surface - see findGlChild usage.
 */
internal fun setChildrenGone(root: View) {
    if (root !is ViewGroup) return
    for (i in 0 until root.childCount) {
        root.getChildAt(i).visibility = View.GONE
    }
}