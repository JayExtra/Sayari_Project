package com.dev.james.sayariproject.repository

import androidx.paging.PagingData
import com.dev.james.sayariproject.data.datasources.discover.IMAGE_LIMIT
import com.dev.james.sayariproject.di.modules.EventsRetrofitResponse
import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.models.astronaut.Astronaut
import com.dev.james.sayariproject.models.discover.ActiveMissions
import com.dev.james.sayariproject.models.events.EventResponse
import com.dev.james.sayariproject.models.events.Events
import com.dev.james.sayariproject.models.events.ScheduledEventAlert
import com.dev.james.sayariproject.models.favourites.AgencyResponse
import com.dev.james.sayariproject.models.favourites.Result
import com.dev.james.sayariproject.models.iss.IntSpaceStation
import com.dev.james.sayariproject.models.launch.Agency
import com.dev.james.sayariproject.models.launch.LaunchList
import com.dev.james.sayariproject.models.launch.RocketInstance
import com.dev.james.sayariproject.utilities.NetworkResource
import kotlinx.coroutines.flow.Flow

interface BaseMainRepository {
    //fetch articles
    fun getArticlesStream(query : String) : Flow<PagingData<Article>>

    suspend fun getTopArticles() : NetworkResource<List<Article>>

    suspend fun getLatestArticles() : NetworkResource<List<Article>>

    suspend fun getFilteredNews(query : String) : NetworkResource<List<Article>>

    fun getLaunchesStream(query: String , fragId : Int) : Flow<PagingData<LaunchList>>

    fun getMissions(category : String) : Flow<List<ActiveMissions>>

    fun getEvents(query : String?) : Flow<PagingData<Events>>

    suspend fun getEventsTopAppbar():NetworkResource<EventResponse>

    suspend fun getArticlesForImages(
        query1: String?,
        query2: String?,
        query3: String?,
        query4: String?,
        query5: String?,
        query6: String?,
        query7: String?,
        query8: String?,
        query9: String?
    ) : NetworkResource<List<Article>>

    suspend fun getSpaceStation() : NetworkResource<IntSpaceStation>

    suspend fun getSpaceStationEvents() : NetworkResource<EventResponse>

    suspend fun getRocketInstance(id : Int) : NetworkResource<RocketInstance>

    suspend fun getAgency(id : Int) : NetworkResource<Agency>

    suspend fun getAstronaut(id : Int) : NetworkResource<Astronaut>

    suspend fun getFavouriteAgenciesFromApi(name : String) : NetworkResource<AgencyResponse>

    suspend fun saveFavouriteAgency(agency : Result)

    fun getFavouriteAgenciesFromDb() : Flow<List<Result>>

    suspend fun deleteFavouriteAgency(id : Int)

    suspend fun getFavouriteAgencies() : List<Result>

    suspend fun getEvents() : List<ScheduledEventAlert>
    suspend fun addEvent(event : ScheduledEventAlert) : Int
    suspend fun deleteEvent(id : Int)

}