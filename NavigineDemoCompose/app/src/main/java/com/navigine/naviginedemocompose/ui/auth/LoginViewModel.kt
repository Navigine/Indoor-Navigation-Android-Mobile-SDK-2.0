package com.navigine.naviginedemocompose.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.naviginedemocompose.data.local.UserStore
import com.navigine.naviginedemocompose.domain.model.QrPayload
import com.navigine.naviginedemocompose.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri
import com.navigine.naviginedemocompose.core.sdk.NavigineSdkManager
import com.navigine.naviginedemocompose.core.util.Constants.DL_QUERY_LOCATION_ID
import com.navigine.naviginedemocompose.core.util.Constants.DL_QUERY_SERVER
import com.navigine.naviginedemocompose.core.util.Constants.DL_QUERY_SUBLOCATION_ID
import com.navigine.naviginedemocompose.core.util.Constants.DL_QUERY_USERHASH
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

private val HASH_REGEX = """^[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$""".toRegex()

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val authStore: UserStore,
    private val sdk: NavigineSdkManager
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _serverCheck = MutableStateFlow<ServerCheckUi>(ServerCheckUi.Idle)
    val serverCheck: StateFlow<ServerCheckUi> = _serverCheck

    init {
        viewModelScope.launch {
            repo.currentServerUrlFlow().collect { s ->
                _state.value = _state.value.copy(currentServer = s)
            }
        }
    }

    fun onHashChange(input: String) {
        val formatted = input.uppercase()
        _state.value = _state.value.copy(
            userHash = formatted,
            isValidHash = HASH_REGEX.matches(formatted),
            error = null
        )
    }

    fun login(onSuccess: () -> Unit) {
        val hash = _state.value.userHash
        if (!HASH_REGEX.matches(hash)) {
            _state.value = _state.value.copy(error = "Invalid hash format")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val res = repo.login(hash)
            _state.value = _state.value.copy(isLoading = false)
            res.onSuccess {
                authStore.setLoggedIn(
                    it.hash,
                    it.name,
                    it.avatarUrl,
                    it.companyName,
                    it.email,
                    it.id.toString()
                )

                val server = repo.currentServerUrlFlow().first()
                val ok = sdk.tryConfigure(server, it.hash)
                if (ok) onSuccess() else _state.value = _state.value.copy(error = "SDK init failed. Check server/hash.")
                onSuccess()
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message ?: "Login failed")
            }
        }
    }

    fun validateAndSaveServer(
        url: String,
        autoClose: Boolean = true,
        onSaved: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _serverCheck.value = ServerCheckUi.Loading
            val result = repo.checkServer(url)
            result.onSuccess {
                _serverCheck.value = ServerCheckUi.Success()
                if (autoClose) {
                    delay(1100)
                    onSaved()
                    _serverCheck.value = ServerCheckUi.Idle
                }
            }.onFailure { e ->
                val msg = e.message ?: "Server is not reachable"
                _serverCheck.value = ServerCheckUi.Error(msg)
                onError(msg)
            }
        }
    }

    fun handleQrScan(
        raw: String,
        onNavigate: (loc: Long?, subloc: Long?) -> Unit
    ) {
        val p = parseQrPayload(raw)
        viewModelScope.launch {
            p.server?.let { safeUrl ->
                runCatching { repo.setServerUrl(safeUrl) }
            }

            p.userHash?.let { hash ->
                authStore.setLoggedIn(
                    hash = hash,
                    name = null,
                    avatar = null,
                    company = null,
                    email = null,
                    id = ""
                )
            }

            val server = p.server ?: repo.currentServerUrlFlow().first()
            val hash = p.userHash ?: authStore.userHashFlow.first()
            val ok = sdk.tryConfigure(server, hash)

            if (ok) onNavigate(p.loc, p.subloc)
        }
    }

    private fun parseQrPayload(raw: String): QrPayload {
        val uri = runCatching { raw.toUri() }.getOrNull()
        uri?.let {
            val server = it.getQueryParameter(DL_QUERY_SERVER)
            val hash = it.getQueryParameter(DL_QUERY_USERHASH)
            val loc = it.getQueryParameter(DL_QUERY_LOCATION_ID)?.toLongOrNull()
            val subloc = it.getQueryParameter(DL_QUERY_SUBLOCATION_ID)?.toLongOrNull()
            return QrPayload(server, hash, loc, subloc)
        }
        return QrPayload()
    }

}