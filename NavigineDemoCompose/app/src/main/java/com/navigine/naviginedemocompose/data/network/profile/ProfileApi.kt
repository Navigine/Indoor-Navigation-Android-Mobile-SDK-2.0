package com.navigine.naviginedemocompose.data.network.profile

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfileApi {

    @GET("/mobile/v1/users/get")
    suspend fun getUser(
        @Query("userHash") userHash: String
    ): UserProfileResponse

    @PUT("/mobile/v1/users/edit/{id}")
    suspend fun editUser(
        @Path("id") id: String,
        @Body body: Map<String, String?>
    ): EditProfileResponse

}