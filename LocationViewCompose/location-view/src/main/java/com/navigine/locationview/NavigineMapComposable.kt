package com.navigine.locationview

import androidx.compose.runtime.ComposableTargetMarker

/**
 * Marker annotation for composables that must be invoked within [NavigineLocation]'s content.
 */
@Retention(AnnotationRetention.BINARY)
@ComposableTargetMarker(description = "Navigine Location Composable")
@Target(
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FILE,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPE,
    AnnotationTarget.TYPE_PARAMETER,
)
public annotation class NavigineMapComposable


/** Opt-in marker for APIs that expose raw SDK objects. */
@RequiresOptIn("This exposes raw Navigine SDK objects. Prefer IconState metadata when possible.")
@Retention(AnnotationRetention.BINARY)
public annotation class ExperimentalNavigineApi