package com.dev.james.sayariproject.data.datasources.favourites

import com.dev.james.sayariproject.models.favourites.AgencyResponse
import com.dev.james.sayariproject.models.favourites.Result
import com.dev.james.sayariproject.utilities.NetworkResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BaseFavouritesDataSource {
    //searches for agency from api
    suspend fun getAgencyByName(name : String) : NetworkResource<AgencyResponse>

    //will save agency when selected in ui to database
    suspend fun saveFavouriteAgency(agency : Result)

    //will return list of favourite agencies from database
    fun getFavouriteAgenciesFromDb() : Flow<List<Result>>

    //will delete the favourite agency
    suspend fun deleteFavouriteAgency(id : Int)
}