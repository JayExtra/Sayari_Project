package com.dev.james.sayariproject.data.remote.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dev.james.sayariproject.data.remote.service.LaunchApiService
import com.dev.james.sayariproject.models.launch.LaunchList
import com.dev.james.sayariproject.utilities.ARTICLE_SEARCH_LOAD_SIZE
import com.dev.james.sayariproject.utilities.LAUNCHES_SEARCH_LOAD_SIZE
import com.dev.james.sayariproject.utilities.STARTING_OFFSET_INDEX
import java.io.IOException

class LaunchesPagingSource(
    private val launchApi: LaunchApiService,
    private val fragId : Int,
    private val query : String?
): PagingSource<Int,LaunchList>() {
    private val TAG = "LaunchDataSource"
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LaunchList> {
        val offset = params.key ?: STARTING_OFFSET_INDEX
        val limit = if(query == null) params.loadSize else LAUNCHES_SEARCH_LOAD_SIZE

        return try {
            if(fragId == 1){
                val launches = launchApi.getUpcomingLaunches(query, limit , offset)
                Log.d(TAG, "load: ${launches.toString()}")
                LoadResult.Page(
                    data = launches.launchList,
                    prevKey = if(offset == STARTING_OFFSET_INDEX) null else offset - limit,
                    nextKey = if(launches.next == null) null else offset + limit

                )
            }else{
                val launches = launchApi.getPreviousLaunches(query ,limit , offset)
                Log.d(TAG, "load: ${launches.toString()}")
                LoadResult.Page(
                    data = launches.launchList,
                    prevKey = if(offset == STARTING_OFFSET_INDEX) null else offset - limit,
                    nextKey = if(launches.next == null) null else offset + limit

                )

            }

        }catch (t : Throwable){
            var excepton = t
            if(t is IOException){
                excepton = IOException("Check your imternet connection!")
            }
            LoadResult.Error(excepton)
        }
    }
    override fun getRefreshKey(state: PagingState<Int, LaunchList>): Int? {
        return state.anchorPosition
    }


}