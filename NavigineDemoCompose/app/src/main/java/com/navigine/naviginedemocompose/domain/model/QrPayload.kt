package com.navigine.naviginedemocompose.domain.model

data class QrPayload(
    val server: String? = null,
    val userHash: String? = null,
    val loc: Long? = null,
    val subloc: Long? = null,
)
