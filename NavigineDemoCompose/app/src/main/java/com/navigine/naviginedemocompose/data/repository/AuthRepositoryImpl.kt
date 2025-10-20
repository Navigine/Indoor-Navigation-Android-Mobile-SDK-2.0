package com.navigine.naviginedemocompose.data.repository

import com.navigine.naviginedemocompose.core.di.network.RetrofitFactory
import com.navigine.naviginedemocompose.data.local.HostUrlStore
import com.navigine.naviginedemocompose.data.network.auth.AuthApi
import com.navigine.naviginedemocompose.data.network.auth.toDomain
import com.navigine.naviginedemocompose.domain.model.User
import com.navigine.naviginedemocompose.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Retrofit
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val hostStore: HostUrlStore,
    private val retrofitFactory: RetrofitFactory
) : AuthRepository {

    override fun currentServerUrlFlow(): Flow<String> = hostStore.serverUrlFlow.map { it }

    override suspend fun login(userHash: String, baseUrl: String?): Result<User> = runCatching {
    // baseUrl is optional because interceptor already swaps base URL using store.
        api.getUser(userHash).toDomain()
    }

    override suspend fun checkServer(baseUrl: String): Result<Unit> = runCatching {
        val normalizedUrl = normalizeBaseUrl(baseUrl)
        val temp = retrofitFactory.create(normalizedUrl).create(AuthApi::class.java)
        val res = temp.ping()
        if (!res.isSuccessful) throw Exception("Server is not reachable")
        hostStore.setServerUrl(normalizedUrl)
    }

    override suspend fun setServerUrl(baseUrl: String) {
        hostStore.setServerUrl(baseUrl)
    }

    private fun normalizeBaseUrl(url : String) : String {
        var u = url.trim()
        require(u.isNotEmpty()) { "URL should not be empty" }

        u = u.replaceFirst(Regex("^(?i)(?:https?://|//)+"), "https://")

        if (!u.startsWith("https://", ignoreCase = true)) {
            u = "https://$u"
        }
        val cut = u.indexOfAny(charArrayOf('?', '#')).let { if (it == -1) u.length else it }
        val head = u.substring(0, cut)
        val tail = u.substring(cut)
        val normalizedHead = head.trimEnd('/')

        return normalizedHead + tail
    }
}