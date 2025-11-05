package com.navigine.naviginedemocompose.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class User(
    val id: Long,
    val name: String,
    val companyName: String? = null,
    val email: String? = null,
    val hash: String,
    val role: String? = null,
    val active: Boolean = true,
    val lastEntryIso: String? = null,
    val language: String? = null,
    val phone: String? = null,
    val registeredAtIso: String? = null,
    val position: String? = null,
    val linkedin: String? = null,
    val avatarUrl: String? = null
)
