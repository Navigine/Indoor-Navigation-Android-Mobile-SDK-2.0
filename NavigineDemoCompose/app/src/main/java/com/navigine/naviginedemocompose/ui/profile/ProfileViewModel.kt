package com.navigine.naviginedemocompose.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.naviginedemocompose.core.log.AppLogger
import com.navigine.naviginedemocompose.data.local.UserStore
import com.navigine.naviginedemocompose.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: ProfileRepository,
    private val userStore: UserStore,
    private val log: AppLogger
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState(isLoading = true))
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ProfileEffect>()
    val effect = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                userStore.userNameFlow,
                userStore.userEmailFlow,
                userStore.userCompanyFlow,
                userStore.userHashFlow,
                userStore.userAvatarFlow
            ) { name, email, company, hash, avatar ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        name = name,
                        email = email,
                        company = company,
                        userHash = hash,
                        avatarUrl = avatar.ifBlank { null },
                        canSave = false
                    )
                }
            }.collect { /* no-op */ }
        }

        refresh()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.Init -> TODO()
            is ProfileEvent.CompanyChange -> onCompanyChange(event.value)
            is ProfileEvent.NameChange -> onNameChange(event.value)
            ProfileEvent.Refresh -> refresh()
            ProfileEvent.Save -> onSave()
            ProfileEvent.EditToggle -> onEditToggle()
            ProfileEvent.Logout -> logout()
        }
    }

    private fun refresh() {
        reduce { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            val res = repo.refresh()
            res.onSuccess { reduce { it.copy(isRefreshing = false, error = null) } }
            res.onFailure { err ->
                reduce { it.copy(error = err.message, isRefreshing = false) }
                log.nonFatal(err, mapOf("where" to "refresh_profile"))
            }
        }
    }

    private fun onEditToggle() {
        _state.update { it.copy(isEditing = !it.isEditing, canSave = false) }
    }

    private fun onNameChange(name: String) {
        _state.update { it.copy(name = name, canSave = true) }
    }

    private fun onCompanyChange(company: String) {
        _state.update { it.copy(company = company, canSave = true) }
    }

    private fun onSave() {
        val name = state.value.name.trim()
        val company = state.value.company.trim().ifBlank { null }
        if (name.isBlank()) {
            viewModelScope.launch { _effect.emit(ProfileEffect.Message("Name can't be empty")) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val res = repo.update(name, company)
            _state.update {
                it.copy(
                    isLoading = false,
                    isEditing = res.isFailure.not(),
                    canSave = false
                )
            }
            if (res.isSuccess) {
                _effect.emit(ProfileEffect.Message("Profile updated"))
                onEditToggle()
            } else {
                _effect.emit(
                    ProfileEffect.Message(
                        res.exceptionOrNull()?.message ?: "Update failed"
                    )
                )
                log.nonFatal(res.exceptionOrNull() ?: RuntimeException("Update failed"), mapOf("where" to "update_profile"))
                onEditToggle()
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            val res = repo.logout()
            if (res.isSuccess)
                _effect.emit(ProfileEffect.NavigateToLogin)
            else {
                _effect.emit(
                    ProfileEffect.Message(
                        res.exceptionOrNull()?.message ?: "Logout failed"
                    ))
                log.nonFatal(res.exceptionOrNull() ?: RuntimeException("Logout failed"), mapOf("where" to "logout"))
            }
        }
    }

    private inline fun reduce(block: (ProfileState) -> ProfileState) {
        _state.update { block(it) }
    }
}
