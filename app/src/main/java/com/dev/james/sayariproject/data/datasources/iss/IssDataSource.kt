package com.dev.james.sayariproject.data.datasources.iss

import com.dev.james.sayariproject.data.remote.service.EventsApiService
import com.dev.james.sayariproject.data.remote.service.LaunchApiService
import com.dev.james.sayariproject.models.astronaut.Astronaut
import com.dev.james.sayariproject.models.events.EventResponse
import com.dev.james.sayariproject.models.iss.IntSpaceStation
import com.dev.james.sayariproject.repository.TopArticlesBaseRepo
import com.dev.james.sayariproject.utilities.NetworkResource
import javax.inject.Inject

class IssDataSource @Inject constructor(
    private val api : LaunchApiService ,
    private val eventsApi : EventsApiService
) : BaseIssDataSource , TopArticlesBaseRepo() {
    override suspend fun getSpaceStation(id: Int) = safeApiCall {
        api.getSpaceStation(id)
    }

    override suspend fun getSpaceStationEvents() = safeApiCall {
        eventsApi.getAllIssEvents(null , EVENTS_OFFSET , EVENTS_LIMIT , PROGRAM_ID)
    }

    override suspend fun getAstronaut(id: Int): NetworkResource<Astronaut> = safeApiCall {
        api.getAstronaut(id)
    }


}
private const val EVENTS_LIMIT = 10
private const val EVENTS_OFFSET = 0
private const val PROGRAM_ID = 17
