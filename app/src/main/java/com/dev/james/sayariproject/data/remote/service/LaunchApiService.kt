package com.dev.james.sayariproject.data.remote.service

import com.dev.james.sayariproject.models.launch.Agency
import com.dev.james.sayariproject.models.launch.Launch
import com.dev.james.sayariproject.models.launch.RocketInstance
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LaunchApiService {

    @GET("launch/previous")
    suspend fun getPreviousLaunches(
        @Query("search")query :String?,
        @Query("limit") limit : Int?,
        @Query("offset") offset : Int?
    ) : Launch

    @GET("launch/upcoming")
    suspend fun getUpcomingLaunches(
        @Query("search")query :String?,
        @Query("limit") limit : Int?,
        @Query("offset") offset : Int?
    ) : Launch

    @GET("agencies/{id}")
    suspend fun getAgency(
        @Path("id") id : Int
    ) : Agency

    @GET("config/launcher/{id}")
    suspend fun getRocketInstance(
        @Path("id") id : Int
    ) : RocketInstance
}