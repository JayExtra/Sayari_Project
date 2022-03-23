package com.dev.james.sayariproject.data.datasources.launches

import androidx.paging.PagingData
import com.dev.james.sayariproject.models.launch.Agency
import com.dev.james.sayariproject.models.launch.LaunchList
import com.dev.james.sayariproject.models.launch.RocketInstance
import com.dev.james.sayariproject.utilities.NetworkResource
import kotlinx.coroutines.flow.Flow

interface LaunchesBaseDatasource {

  fun getLaunches(query : String , fragmentId : Int) : Flow<PagingData<LaunchList>>

  suspend fun getRocketConfiguration(id : Int) : NetworkResource<RocketInstance>

  suspend fun getAgencyDetails(id : Int) : NetworkResource<Agency>

}