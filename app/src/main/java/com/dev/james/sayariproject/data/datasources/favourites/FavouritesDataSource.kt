package com.dev.james.sayariproject.data.datasources.favourites

import com.dev.james.sayariproject.data.local.room.Dao
import com.dev.james.sayariproject.data.remote.service.LaunchApiService
import com.dev.james.sayariproject.models.favourites.AgencyResponse
import com.dev.james.sayariproject.models.favourites.Result
import com.dev.james.sayariproject.repository.TopArticlesBaseRepo
import com.dev.james.sayariproject.utilities.NetworkResource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FavouritesDataSource @Inject constructor(
    private val dao: Dao ,
    private val api : LaunchApiService
) : BaseFavouritesDataSource , TopArticlesBaseRepo() {
    //uses api to get agency
    override suspend fun getAgencyByName(name: String): NetworkResource<AgencyResponse>  = safeApiCall{
        api.getAgencyFromApi(name , DEFAULT_LIMIT , DEFAULT_OFFSET)
    }
    //saves selected favourite agency to database
    override suspend fun saveFavouriteAgency(agency: Result) {
        dao.saveFavouriteAgency(agency)
    }

    //retrieves list of favourite agencies from database
    override fun getFavouriteAgenciesFromDb(): Flow<List<Result>> {
        return dao.getFavouriteAgencies()
    }

}

private const val DEFAULT_LIMIT = 10
private const val DEFAULT_OFFSET = 0