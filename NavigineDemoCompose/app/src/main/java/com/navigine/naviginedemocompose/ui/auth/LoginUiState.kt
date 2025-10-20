package com.navigine.naviginedemocompose.ui.auth

import androidx.compose.runtime.Immutable

@Immutable
data class LoginUiState(
    val userHash: String = "",
    val isValidHash: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentServer: String = ""
)

sealed interface ServerCheckUi {
    data object Idle : ServerCheckUi
    data object Loading : ServerCheckUi
    data class Success(val message: String = "Server updated") : ServerCheckUi
    data class Error(val message: String) : ServerCheckUi
}
