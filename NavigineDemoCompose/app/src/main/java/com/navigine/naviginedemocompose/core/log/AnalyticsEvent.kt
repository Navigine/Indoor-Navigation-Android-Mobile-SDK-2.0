package com.navigine.naviginedemocompose.core.log

sealed interface AnalyticsEvent {
    val name: String get() = this::class.simpleName!!
    fun params(): Map<String, Any?>
}

data class LoginResult(val success: Boolean, val error: String?) : AnalyticsEvent {
    override fun params() = mapOf("ok" to success, "err" to (error ?: ""))
}

// convenience
fun AppLogger.log(ev: AnalyticsEvent) = event(ev.name, ev.params())