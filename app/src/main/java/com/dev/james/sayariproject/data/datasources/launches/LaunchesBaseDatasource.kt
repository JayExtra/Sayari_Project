package com.dev.james.sayariproject.data.datasources.launches

import androidx.paging.PagingData
import com.dev.james.sayariproject.models.launch.LaunchList
import kotlinx.coroutines.flow.Flow

interface LaunchesBaseDatasource {

  fun getLaunches(query : String , fragmentId : Int) : Flow<PagingData<LaunchList>>

}