package com.dev.james.sayariproject.data.datasources.discover

import com.dev.james.sayariproject.data.local.room.Dao
import com.dev.james.sayariproject.data.remote.service.NewsApiService
import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.models.discover.ActiveMissions
import com.dev.james.sayariproject.repository.TopArticlesBaseRepo
import com.dev.james.sayariproject.utilities.ARTICLE_STARTING_INDEX
import com.dev.james.sayariproject.utilities.NetworkResource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DiscoverFragmentDatasource @Inject constructor(
    private val newsApiService: NewsApiService,
    private val dao: Dao
) : BaseDiscoverFragmentDatasource , TopArticlesBaseRepo() {
    override suspend fun getFilteredNews(filter: String) = safeApiCall {
        newsApiService.getNewsArticles(
            query = filter,
            limit = LIMIT,
            start = ARTICLE_STARTING_INDEX
        )
    }

    override fun getMission(category: String): Flow<List<ActiveMissions>> {
        return dao.getMissionsByCategory(category)
    }
}

const val LIMIT = 5