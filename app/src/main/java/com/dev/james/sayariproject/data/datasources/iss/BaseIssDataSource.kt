package com.dev.james.sayariproject.data.datasources.iss

import com.dev.james.sayariproject.models.iss.IntSpaceStation
import com.dev.james.sayariproject.utilities.NetworkResource

interface BaseIssDataSource {
    //get the iss response
    suspend fun getSpaceStation(id : Int) : NetworkResource<IntSpaceStation>
}