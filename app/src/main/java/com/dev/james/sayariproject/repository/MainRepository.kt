package com.dev.james.sayariproject.repository

import androidx.paging.PagingData
import com.dev.james.sayariproject.data.datasources.discover.BaseDiscoverFragmentDatasource
import com.dev.james.sayariproject.data.datasources.events.BaseEventsDatasource
import com.dev.james.sayariproject.data.datasources.events.EventsDatasource
import com.dev.james.sayariproject.data.datasources.home.BaseTopArticlesDataSource
import com.dev.james.sayariproject.data.datasources.launches.LaunchesBaseDatasource
import com.dev.james.sayariproject.data.datasources.home.SpaceFlightApiDataSource
import com.dev.james.sayariproject.data.datasources.iss.BaseIssDataSource
import com.dev.james.sayariproject.data.datasources.iss.IssDataSource
import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.models.discover.ActiveMissions
import com.dev.james.sayariproject.models.events.EventResponse
import com.dev.james.sayariproject.models.events.Events
import com.dev.james.sayariproject.models.iss.IntSpaceStation
import com.dev.james.sayariproject.models.launch.LaunchList
import com.dev.james.sayariproject.models.launch.RocketInstance
import com.dev.james.sayariproject.utilities.NetworkResource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val articlesDataSource: SpaceFlightApiDataSource,
    private val topArticlesDataSource: BaseTopArticlesDataSource,
    private val launchesDataSource: LaunchesBaseDatasource,
    private val discoverFragmentDatasource: BaseDiscoverFragmentDatasource,
    private val eventsDatasource: BaseEventsDatasource,
    private val issDataSource:BaseIssDataSource
) : BaseMainRepository {

    // retrieves articles data stream
    override fun getArticlesStream(query: String): Flow<PagingData<Article>> {
        return articlesDataSource.getArticlesResultStream(query)
    }

    override suspend fun getTopArticles(): NetworkResource<List<Article>> {
       return topArticlesDataSource.getTopArticles()
    }

    override suspend fun getLatestArticles(): NetworkResource<List<Article>> {
        return topArticlesDataSource.getLatestArticles()
    }

    override suspend fun getFilteredNews(query: String): NetworkResource<List<Article>> {
        return discoverFragmentDatasource.getFilteredNews(query)
    }

    override fun getLaunchesStream(query: String, fragId: Int): Flow<PagingData<LaunchList>> {
        return launchesDataSource.getLaunches(query , fragId)
    }

    override fun getMissions(category: String): Flow<List<ActiveMissions>> {
        return discoverFragmentDatasource.getMission(category)
    }

    override fun getEvents(query: String?): Flow<PagingData<Events>> {
        return eventsDatasource.getEvents(query)
    }

    override suspend fun getEventsTopAppbar(): NetworkResource<EventResponse> {
        return eventsDatasource.getEventsAppBar()
    }

    override suspend fun getArticlesForImages(
        query1: String?,
        query2: String?,
        query3: String?,
        query4: String?,
        query5: String?,
        query6: String?,
        query7: String?,
        query8: String?,
        query9: String?
    ): NetworkResource<List<Article>> {
        return discoverFragmentDatasource.getArticlesForImages(
            query1,
            query2,
            query3,
            query4,
            query5,
            query6,
            query7,
            query8,
            query9,
        )
    }

    //return the space station object
    override suspend fun getSpaceStation(): NetworkResource<IntSpaceStation> {
        return issDataSource.getSpaceStation(SPACE_STATION_ID)
    }

    //returns response containing iss upcoming events
    override suspend fun getSpaceStationEvents(): NetworkResource<EventResponse> {
        return issDataSource.getSpaceStationEvents()
    }

    //returns a rocket instance
    override suspend fun getRocketInstance(id: Int): NetworkResource<RocketInstance> {
        return launchesDataSource.getRocketConfiguration(id)
    }

}

private const val SPACE_STATION_ID = 4