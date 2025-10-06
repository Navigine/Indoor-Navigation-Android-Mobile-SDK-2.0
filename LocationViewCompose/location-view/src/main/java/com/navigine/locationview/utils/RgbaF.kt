package com.navigine.locationview.utils


/** ARGB Int -> RGBA floats in [0f, 1f]. */
internal data class RgbaF(val r: Float, val g: Float, val b: Float, val a: Float)

internal fun Int.toRgbaF(): RgbaF {
    val a = ((this ushr 24) and 0xFF) / 255f
    val r = ((this ushr 16) and 0xFF) / 255f
    val g = ((this ushr 8)  and 0xFF) / 255f
    val b = ( this         and 0xFF) / 255f
    return RgbaF(r, g, b, a)
}
