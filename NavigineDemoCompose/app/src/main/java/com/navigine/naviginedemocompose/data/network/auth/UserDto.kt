package com.navigine.naviginedemocompose.data.network.auth

import com.squareup.moshi.Json

data class UserDto(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String?,
    @Json(name = "company_name") val companyName: String?,
    @Json(name = "email") val email: String?,
    @Json(name = "password") val passwordHash: String?,
    @Json(name = "salt") val salt: String?,
    @Json(name = "hash") val hash: String,
    @Json(name = "role") val role: String?,
    @Json(name = "active") val active: Int?,
    @Json(name = "last_entry") val lastEntry: String?,
    @Json(name = "language") val language: String?,
    @Json(name = "phone_number") val phoneNumber: String?,
    @Json(name = "registered_at") val registeredAt: String?,
    @Json(name = "position") val position: String?,
    @Json(name = "linkedin") val linkedin: String?,
    @Json(name = "avatar_url") val avatarUrl: String?
)
