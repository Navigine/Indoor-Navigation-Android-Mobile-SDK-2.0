package com.navigine.naviginedemocompose.domain.repository

import com.navigine.naviginedemocompose.domain.model.User

interface AuthRepository {

    suspend fun login(userHash: String, baseUrl: String? = null): Result<User>

    /** Returns true if the server is reachable/valid (endpoint is adjustable). */
    suspend fun checkServer(baseUrl: String): Result<Unit>

    /** Persist last-used server URL. */
    suspend fun setServerUrl(baseUrl: String)

    /** Flow current server URL (default if none saved). */
    fun currentServerUrlFlow(): kotlinx.coroutines.flow.Flow<String>
}