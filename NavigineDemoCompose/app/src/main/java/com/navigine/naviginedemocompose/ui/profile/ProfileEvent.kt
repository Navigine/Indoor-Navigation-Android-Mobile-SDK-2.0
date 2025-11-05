package com.navigine.naviginedemocompose.ui.profile

sealed interface ProfileEvent {
    data object Init : ProfileEvent
    data object Refresh : ProfileEvent
    data object EditToggle : ProfileEvent
    data class NameChange(val value: String) : ProfileEvent
    data class CompanyChange(val value: String) : ProfileEvent
    data object Save : ProfileEvent
    data object Logout : ProfileEvent
}

sealed interface ProfileEffect {
    data class Message(val text: String) : ProfileEffect
    data object NavigateToLogin : ProfileEffect
}