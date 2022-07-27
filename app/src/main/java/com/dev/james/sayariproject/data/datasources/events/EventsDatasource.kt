package com.dev.james.sayariproject.data.datasources.events

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dev.james.sayariproject.data.local.room.Dao
import com.dev.james.sayariproject.data.remote.paging.EventsPagingSource
import com.dev.james.sayariproject.data.remote.service.EventsApiService
import com.dev.james.sayariproject.models.events.Events
import com.dev.james.sayariproject.models.favourites.Result
import com.dev.james.sayariproject.repository.TopArticlesBaseRepo
import com.dev.james.sayariproject.utilities.EVENTS_SEARCH_LOAD_SIZE
import com.dev.james.sayariproject.utilities.NetworkResource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EventsDatasource @Inject constructor(
    private val eventsApi : EventsApiService,
    private val dao : Dao
) : BaseEventsDatasource , TopArticlesBaseRepo(){

    override fun getEvents(query: String?): Flow<PagingData<Events>> {
        return Pager(
            config = PagingConfig(pageSize = EVENTS_SEARCH_LOAD_SIZE , enablePlaceholders = false),
            pagingSourceFactory = {
                EventsPagingSource(eventsApi , query)
            }
        ).flow
    }

    override suspend fun getEventsAppBar() = safeApiCall {
        eventsApi.getAllEvents(EVENTS_DEFAULT_QUERY , EVENTS_OFFSET  , EVENTS_LIMIT )
    }

    override suspend fun getFavouriteAgencies(): List<Result> {
        val agencies = dao.favouriteAgenciesSnapshot()
        Log.d("EventsDataSource", "getFavouriteAgencies: $agencies")
        return agencies
    }


}

const val EVENTS_LIMIT = 30
const val EVENTS_DEFAULT_QUERY = ""
const val DEFAULT_PROGRAM = ""
const val EVENTS_OFFSET = 0