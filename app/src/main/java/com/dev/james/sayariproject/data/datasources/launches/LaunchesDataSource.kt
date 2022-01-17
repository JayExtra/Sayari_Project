package com.dev.james.sayariproject.data.datasources.launches

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dev.james.sayariproject.data.remote.paging.LaunchesPagingSource
import com.dev.james.sayariproject.data.remote.service.LaunchApiService
import com.dev.james.sayariproject.models.launch.LaunchList
import com.dev.james.sayariproject.utilities.LAUNCHES_SEARCH_LOAD_SIZE
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LaunchesDataSource @Inject constructor(
    private var launchesApi : LaunchApiService
) : LaunchesBaseDatasource {

    override fun getLaunches(query: String, fragmentId : Int): Flow<PagingData<LaunchList>> {
        return Pager(
            config = PagingConfig(pageSize = LAUNCHES_SEARCH_LOAD_SIZE , enablePlaceholders = false),
            pagingSourceFactory = {
                LaunchesPagingSource(launchesApi,fragmentId,query)
            }
        ).flow
    }
}