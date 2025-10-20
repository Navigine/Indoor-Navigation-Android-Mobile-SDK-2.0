package com.navigine.naviginedemocompose.data.repository

import com.navigine.naviginedemocompose.core.sdk.NavigineSdkManager
import com.navigine.naviginedemocompose.data.local.UserStore
import com.navigine.naviginedemocompose.data.network.profile.ProfileApi
import com.navigine.naviginedemocompose.domain.model.User
import com.navigine.naviginedemocompose.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApi,
    private val userStore: UserStore,
    private val sdk: NavigineSdkManager
) : ProfileRepository {

    override val userFlow: Flow<User?>
        get() = TODO("Not yet implemented")


    override suspend fun refresh(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun update(
        name: String,
        company: String?
    ): Result<Unit> = runCatching {
        val current = userFlow.firstOrNull() ?: error("Not logged in")
        val resp = api.editUser(
            id = current.id.toString(),
            body = mapOf(
                "name" to name,
                "company_name" to (company ?: "")
            )
        )
        check(resp.success) { "Edit profile failed" }

        userStore.setLoggedIn(
            hash = current.hash,
            name = name,
            avatar = current.avatarUrl,
            company = company,
            email = current.email,
            id = current.id.toString()
        )
    }

    override suspend fun deleteAccount(password: String): Result<Unit> {
        TODO("Not yet implemented")
    }
}