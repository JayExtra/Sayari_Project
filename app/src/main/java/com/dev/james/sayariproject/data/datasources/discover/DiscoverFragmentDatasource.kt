package com.dev.james.sayariproject.data.datasources.discover

import com.dev.james.sayariproject.data.remote.service.NewsApiService
import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.repository.TopArticlesBaseRepo
import com.dev.james.sayariproject.utilities.ARTICLE_STARTING_INDEX
import com.dev.james.sayariproject.utilities.NetworkResource
import javax.inject.Inject

class DiscoverFragmentDatasource @Inject constructor(
    private val newsApiService: NewsApiService
) : BaseDiscoverFragmentDatasource , TopArticlesBaseRepo() {
    override suspend fun getFilteredNews(filter: String) = safeApiCall {
        newsApiService.getNewsArticles(
            query = filter,
            limit = LIMIT,
            start = ARTICLE_STARTING_INDEX
        )
    }
}

const val LIMIT = 5