package com.dev.james.sayariproject.data.remote.service

import com.dev.james.sayariproject.models.astronaut.Astronaut
import com.dev.james.sayariproject.models.favourites.AgencyResponse
import com.dev.james.sayariproject.models.iss.IntSpaceStation
import com.dev.james.sayariproject.models.launch.Agency
import com.dev.james.sayariproject.models.launch.Launch
import com.dev.james.sayariproject.models.launch.RocketInstance
import retrofit2.Response
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

    @GET("spacestation/{id}")
    suspend fun getSpaceStation(
        @Path("id") id :Int
    ) : IntSpaceStation

    @GET("astronaut/{id}")
    suspend fun getAstronaut(
        @Path("id") id:Int
    ) : Astronaut

    @GET("agencies")
    suspend fun getAgencyFromApi(
        @Query("search")name : String,
        @Query("limit") limit : Int?,
        @Query("offset") offset : Int?
    ) : AgencyResponse

    @GET("launch/upcoming")
    suspend fun getUpcomingLaunchesForSync(
        @Query("search")query :String?,
        @Query("limit") limit : Int?,
        @Query("offset") offset : Int?
    ) : Response<Launch>


}