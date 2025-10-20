package com.navigine.naviginedemocompose.data.network.profile

import com.squareup.moshi.Json


data class UserProfileResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "company_name") val company: String?,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "salt") val salt: String,
    @Json(name = "hash") val hash: String,
    @Json(name = "avatar_url") val avatar: String?,
    @Json(name = "role") val role: String,
    @Json(name = "active") val active: Int,
    @Json(name = "last_entry") val lastEntry: String,
    @Json(name = "language") val language: String,
    @Json(name = "phone_number") val phoneNumber: String?,
    @Json(name = "registered_at") val registeredAt: String,
    @Json(name = "position") val position: String?,
    @Json(name = "linkedin") val linkedin: String?
)

data class EditProfileResponse(
    @Json(name = "user") val success: Boolean
)

data class DeleteProfileResponse(
    @Json(name = "message") val message: String
)