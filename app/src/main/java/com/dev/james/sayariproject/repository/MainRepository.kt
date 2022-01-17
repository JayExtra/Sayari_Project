package com.dev.james.sayariproject.repository

import androidx.paging.PagingData
import com.dev.james.sayariproject.data.datasources.discover.BaseDiscoverFragmentDatasource
import com.dev.james.sayariproject.data.datasources.home.BaseTopArticlesDataSource
import com.dev.james.sayariproject.data.datasources.launches.LaunchesBaseDatasource
import com.dev.james.sayariproject.data.datasources.home.SpaceFlightApiDataSource
import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.models.launch.LaunchList
import com.dev.james.sayariproject.utilities.NetworkResource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val articlesDataSource: SpaceFlightApiDataSource,
    private val topArticlesDataSource: BaseTopArticlesDataSource,
    private val launchesDataSource: LaunchesBaseDatasource,
    private val discoverFragmentDatasource: BaseDiscoverFragmentDatasource
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

}