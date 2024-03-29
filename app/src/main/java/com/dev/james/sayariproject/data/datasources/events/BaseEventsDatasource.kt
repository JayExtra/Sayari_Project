package com.dev.james.sayariproject.data.datasources.events

import androidx.paging.PagingData
import com.dev.james.sayariproject.models.events.EventResponse
import com.dev.james.sayariproject.models.events.Events
import com.dev.james.sayariproject.models.events.ScheduledEventAlert
import com.dev.james.sayariproject.models.favourites.Result
import com.dev.james.sayariproject.utilities.NetworkResource
import kotlinx.coroutines.flow.Flow

interface BaseEventsDatasource {
    //return a flowable containing paging data from the paging3 library
    //which contains a list of events. Will be gotten as per the query defined
    fun getEvents(query : String?) : Flow<PagingData<Events>>
    //returns list of events for collapsible appbar filters
    //suspend fun getEventsForAppBar() : NetworkResource<List<Events>>
    suspend fun getEventsAppBar() : NetworkResource<EventResponse>
    suspend fun getFavouriteAgencies() : List<Result>

    suspend fun getScheduledEvent() : List<ScheduledEventAlert>
    suspend fun addEvent(scheduledEventAlert: ScheduledEventAlert ) : Int
    suspend fun deleteScheduledEvent(id : Int)
}