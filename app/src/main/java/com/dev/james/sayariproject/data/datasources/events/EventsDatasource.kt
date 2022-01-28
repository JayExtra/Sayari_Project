package com.dev.james.sayariproject.data.datasources.events

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dev.james.sayariproject.data.remote.paging.EventsPagingSource
import com.dev.james.sayariproject.data.remote.service.EventsApiService
import com.dev.james.sayariproject.models.events.Events
import com.dev.james.sayariproject.utilities.EVENTS_SEARCH_LOAD_SIZE
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EventsDatasource @Inject constructor(
    private val eventsApi : EventsApiService
) : BaseEventsDatasource {
    override fun getEvents(query: String?): Flow<PagingData<Events>> {
        return Pager(
            config = PagingConfig(pageSize = EVENTS_SEARCH_LOAD_SIZE , enablePlaceholders = false),
            pagingSourceFactory = {
                EventsPagingSource(eventsApi , query)
            }
        ).flow
    }
}