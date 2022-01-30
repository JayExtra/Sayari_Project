package com.dev.james.sayariproject.data.remote.service

import com.dev.james.sayariproject.models.events.EventResponse
import com.dev.james.sayariproject.models.events.Events
import retrofit2.http.GET
import retrofit2.http.Query

interface EventsApiService {
    @GET("event/upcoming")
    suspend fun getAllEvents(
        @Query("search")query : String?,
        @Query("offset")offset : Int?,
        @Query("limit")limit : Int?
    ) : EventResponse
}