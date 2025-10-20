package com.navigine.naviginedemocompose.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.naviginedemocompose.core.sdk.NavigineSdkManager
import com.navigine.naviginedemocompose.data.local.UserStore
import com.navigine.naviginedemocompose.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GateViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userStore: UserStore,
    private val sdk: NavigineSdkManager
): ViewModel() {

    val sdkState: StateFlow<NavigineSdkManager.SdkState> = sdk.state
    val userHash = userStore.userHashFlow

    fun autoInitSdk(){
        viewModelScope.launch {
            val server = authRepository.currentServerUrlFlow().first()
            val hash = userHash.first()
            val locationId = userStore.savedLocationIdFlow.first()

            if (server.isNotBlank() && hash.isNotBlank()) {
                sdk.tryConfigure(server, hash)
                sdk.locationManager.locationId = locationId
            }
        }
    }
}