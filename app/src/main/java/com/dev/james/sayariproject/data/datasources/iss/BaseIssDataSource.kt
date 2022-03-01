package com.dev.james.sayariproject.data.datasources.iss

import com.dev.james.sayariproject.models.events.EventResponse
import com.dev.james.sayariproject.models.events.Events
import com.dev.james.sayariproject.models.iss.IntSpaceStation
import com.dev.james.sayariproject.utilities.NetworkResource

interface BaseIssDataSource {
    //get the iss response
    suspend fun getSpaceStation(id : Int) : NetworkResource<IntSpaceStation>

    //get space station events
    suspend fun getSpaceStationEvents() : NetworkResource<EventResponse>
}