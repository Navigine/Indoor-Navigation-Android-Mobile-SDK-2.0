package com.navigine.naviginedemocompose.data.repository

import com.navigine.naviginedemocompose.core.sdk.NavigineSdkManager
import com.navigine.naviginedemocompose.data.local.UserStore
import com.navigine.naviginedemocompose.data.network.profile.ProfileApi
import com.navigine.naviginedemocompose.domain.model.User
import com.navigine.naviginedemocompose.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApi,
    private val userStore: UserStore,
    private val sdk: NavigineSdkManager
) : ProfileRepository {

    override val userFlow: Flow<User?> = combine(
        combine(
            userStore.userIdFlow,
            userStore.userNameFlow,
            userStore.userEmailFlow
        ) { id, name, email -> Triple(id, name, email) },

        combine(
            userStore.userCompanyFlow,
            userStore.userHashFlow
        ) { company, hash -> company to hash },

        userStore.userAvatarFlow
    ) { (id, name, email), (company, hash), avatar ->

        if (hash.isBlank() || id.isBlank()) null
        else
        User(
            id = id.toLong(),
            name = name,
            email = email,
            companyName = company,
            hash = hash,
            avatarUrl = avatar
        )
    }


    override suspend fun refresh(): Result<Unit>  = runCatching {
        val hash = userStore.userHashFlow.firstOrEmpty()
        require(hash.isNotBlank()) { "No user hash" }
        val payload = api.getUser(hash)
        userStore.setLoggedIn(
            hash = payload.hash,
            name = payload.name,
            avatar = payload.avatar,
            company = payload.company,
            email = payload.email,
            id = payload.id.toString()
        )
    }

    override suspend fun update(
        name: String,
        company: String?
    ): Result<Unit> = runCatching {
        val current = userFlow.firstOrNull() ?: error("Not logged in")
        val resp = api.editUser(
            id = current.id.toString(),
            body = mapOf(
                "userHash" to current.hash,
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

    override suspend fun logout(): Result<Unit> = runCatching {
        userStore.logout()
        sdk.clear()
    }

    private suspend fun Flow<String>.firstOrEmpty(): String =
        this.map { it }.firstOrNull().orEmpty()

}