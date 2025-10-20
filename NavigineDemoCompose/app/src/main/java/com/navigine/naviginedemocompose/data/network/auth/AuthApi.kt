package com.navigine.naviginedemocompose.data.network.auth

import retrofit2.http.GET
import retrofit2.http.HEAD
import retrofit2.http.Query

interface AuthApi {

    @GET("mobile/v1/users/get")
    suspend fun getUser(
        @Query("userHash") userHash: String
    ): UserDto

    @HEAD("/mobile/health_check")
    suspend fun ping(): retrofit2.Response<Unit>
}