package com.dev.james.sayariproject.data.remote.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dev.james.sayariproject.data.remote.service.EventsApiService
import com.dev.james.sayariproject.models.events.Events
import com.dev.james.sayariproject.utilities.EVENTS_SEARCH_LOAD_SIZE
import com.dev.james.sayariproject.utilities.STARTING_OFFSET_INDEX
import java.io.IOException

class EventsPagingSource(
    private val eventsApiService: EventsApiService,
    private val query : String?,
    private val program : Int?
) : PagingSource<Int , Events>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Events> {
        val offset = params.key ?: STARTING_OFFSET_INDEX
        val limit = if(query == null) params.loadSize else EVENTS_SEARCH_LOAD_SIZE
        
        return try {
            val response = eventsApiService.getAllEvents(query , limit , offset , program)
            val list = response.results
  //          Log.d("EventsPag", "load: ${response.results}")
            Log.d("EventsPag", "offset: $offset , limit: $limit")

            LoadResult.Page(
                data = list,
                prevKey = if(offset == STARTING_OFFSET_INDEX) null else offset - limit,
                nextKey = if(response.next == null) null else offset + limit
            )
        } catch (t : Throwable){
            var exception = t
            if(t is IOException){
                exception = IOException("Check your connection")
            }
            LoadResult.Error(exception)
        }
    }
    override fun getRefreshKey(state: PagingState<Int, Events>): Int? {
        return state.anchorPosition
    }
}