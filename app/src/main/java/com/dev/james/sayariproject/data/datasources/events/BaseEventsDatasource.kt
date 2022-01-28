package com.dev.james.sayariproject.data.datasources.events

import androidx.paging.PagingData
import com.dev.james.sayariproject.models.events.Events
import kotlinx.coroutines.flow.Flow

interface BaseEventsDatasource {
    //return a flowable containing paging data from the paging3 library
    //which contains a list of events. Will be gotten as per the query defined
    fun getEvents(query : String?) : Flow<PagingData<Events>>
}