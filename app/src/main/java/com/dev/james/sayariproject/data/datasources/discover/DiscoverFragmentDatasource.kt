package com.dev.james.sayariproject.data.datasources.discover

import android.util.Log
import com.dev.james.sayariproject.data.local.room.Dao
import com.dev.james.sayariproject.data.remote.service.NewsApiService
import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.models.discover.ActiveMissions
import com.dev.james.sayariproject.repository.TopArticlesBaseRepo
import com.dev.james.sayariproject.utilities.ARTICLE_STARTING_INDEX
import com.dev.james.sayariproject.utilities.NetworkResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
    ) = safeApiCall {
        newsApiService.getArticlesMultiQuery(
            query1,
            query2,
            query3,
            query4,
            query5,
            query6,
            query7,
            query8,
            query9,
            IMAGE_LIMIT
        )
    }

}

const val LIMIT = 5
const val IMAGE_LIMIT = 10