package com.dev.james.sayariproject.data.datasources.home

import com.dev.james.sayariproject.data.remote.service.NewsApiService
import com.dev.james.sayariproject.repository.TopArticlesBaseRepo
import com.dev.james.sayariproject.utilities.ARTICLE_STARTING_INDEX
import javax.inject.Inject

class TopArticlesDataSource @Inject constructor(
    private val newsApi : NewsApiService
) : BaseTopArticlesDataSource, TopArticlesBaseRepo() {

    override suspend fun getTopArticles() = safeApiCall {
        newsApi.getTopArticles(FEATURED , LIMIT)
    }

    override suspend fun getLatestArticles() = safeApiCall {
        newsApi.getNewsArticles(DEFAULT_QUERY , limit = LATEST_ARTICLES_LIMIT , start = ARTICLE_STARTING_INDEX )
    }
}

const val LIMIT = 5
const val LATEST_ARTICLES_LIMIT = 100
const val FEATURED = true
const val DEFAULT_QUERY = ""