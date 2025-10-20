package com.navigine.naviginedemocompose.domain.repository

import com.navigine.naviginedemocompose.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    /** Current user profile from local store (DataStore). Null if not logged in. */
    val userFlow: Flow<User?>

    /** Pulls latest profile from backend by hash and updates local store. */
    suspend fun refresh(): Result<Unit>

    /** Updates user name/company on backend and local store. */
    suspend fun update(name: String, company: String?): Result<Unit>

    /** Deletes user account on backend and clears local store & SDK. */
    suspend fun deleteAccount(password: String): Result<Unit>
}