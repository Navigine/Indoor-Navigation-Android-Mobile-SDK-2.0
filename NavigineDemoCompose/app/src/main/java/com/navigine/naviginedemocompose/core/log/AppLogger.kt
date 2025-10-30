package com.navigine.naviginedemocompose.core.log

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

interface AppLogger {
    fun setUserId(id : String?)
    fun setUserProperty(key: String, value: String?)
    fun event(name: String, params: Map<String, Any?> = emptyMap())
    fun breadcrumb(message: String, params: Map<String, Any?> = emptyMap())
    fun nonFatal(t: Throwable, keys: Map<String, Any?> = emptyMap())
}

class FirebaseAppLogger(
    private val analytics: FirebaseAnalytics,
    private val crash: FirebaseCrashlytics
) : AppLogger {

    override fun setUserId(id: String?) {
        analytics.setUserId(id?.take(64))
        crash.setUserId(id?.take(64) ?: "")
    }

    override fun setUserProperty(key: String, value: String?) {
        analytics.setUserProperty(key.take(24), value?.take(36))
    }

    override fun event(
        name: String,
        params: Map<String, Any?>
    ) {
        val b = Bundle()
        params.forEach { (k, v) -> put(b, k, v) }
        analytics.logEvent(name.take(40), b)
    }

    override fun breadcrumb(
        message: String,
        params: Map<String, Any?>
    ) {
        val map = (params + ("msg" to message)).entries
            .joinToString(", ") { (k, v) -> "$k=$v" }
        crash.log(map.take(1000))
    }

    override fun nonFatal(
        t: Throwable,
        keys: Map<String, Any?>
    ) {
        keys.take(64).forEach { (k, v) ->
            crash.setCustomKey(k, v?.toString()?.take(100) ?: "null")
        }
        crash.recordException(t)
    }

    private fun put(bundle: Bundle, k: String, v: Any?) {
        when (v) {
            is String -> bundle.putString(k, v.take(100))
            is Int -> bundle.putInt(k, v)
            is Long -> bundle.putLong(k, v)
            is Double -> bundle.putDouble(k, v)
            is Float -> bundle.putDouble(k, v.toDouble())
            is Boolean -> bundle.putString(k, v.toString())
            null -> {}
            else -> bundle.putString(k, v.toString().take(100))
        }
    }

    private fun Map<String, Any?>.take(n: Int): Map<String, Any?> {
        return this.entries.take(n).associate { it.key to it.value }
    }

}

