package com.navigine.naviginedemocompose.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class User(
    val id: Long,
    val name: String,
    val companyName: String?,
    val email: String?,
    val hash: String,
    val role: String?,
    val active: Boolean,
    val lastEntryIso: String?,
    val language: String?,
    val phone: String?,
    val registeredAtIso: String?,
    val position: String?,
    val linkedin: String?,
    val avatarUrl: String?
)
