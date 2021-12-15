package com.dev.james.sayariproject.data.datasources

import com.dev.james.sayariproject.data.remote.service.NewsApiService
import com.dev.james.sayariproject.repository.TopArticlesBaseRepo
import javax.inject.Inject

class TopArticlesDataSource @Inject constructor(
    private val newsApi : NewsApiService
) : BaseTopArticlesDataSource , TopArticlesBaseRepo() {

    override suspend fun getTopArticles() = safeApiCall {
        newsApi.getTopArticles(FEATURED , LIMIT)
    }
}

const val LIMIT = 5
const val FEATURED = true