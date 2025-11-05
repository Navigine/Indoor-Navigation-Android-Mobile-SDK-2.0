package com.navigine.naviginedemocompose.ui.profile

import androidx.compose.runtime.Immutable

@Immutable
data class ProfileState(
    val name: String = "",
    val company: String = "",
    val email: String = "",
    val userHash: String = "",
    val avatarUrl: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isEditing: Boolean = false,
    val canSave: Boolean = false,
)
