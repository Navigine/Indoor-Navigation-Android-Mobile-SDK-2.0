package com.navigine.naviginedemocompose.data.network.profile

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfileApi {


    @GET("user")
    suspend fun getUser(
        @Query("userHash") userHash: String
    ): UserProfileResponse

    @PUT("user/{id}")
    suspend fun editUser(
        @Path("id") id: String,
        @Body body: Map<String, String?>
    ): EditProfileResponse

    @POST("user/delete")
    suspend fun deleteUser(
        @Header("X-User-Hash") userHash: String,
        @Body body: Map<String, String>
    ): DeleteProfileResponse

}