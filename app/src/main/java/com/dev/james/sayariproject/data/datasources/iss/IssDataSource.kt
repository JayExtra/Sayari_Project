package com.dev.james.sayariproject.data.datasources.iss

import com.dev.james.sayariproject.data.remote.service.LaunchApiService
import com.dev.james.sayariproject.models.iss.IntSpaceStation
import com.dev.james.sayariproject.repository.TopArticlesBaseRepo
import com.dev.james.sayariproject.utilities.NetworkResource
import javax.inject.Inject

class IssDataSource @Inject constructor(
    private val api : LaunchApiService
) : BaseIssDataSource , TopArticlesBaseRepo() {
    override suspend fun getSpaceStation(id: Int) = safeApiCall {
        api.getSpaceStation(id)
    }
}
