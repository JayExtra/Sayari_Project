package com.dev.james.sayariproject.data.datasources.home

import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.utilities.NetworkResource

interface BaseTopArticlesDataSource {
    suspend fun getTopArticles() : NetworkResource<List<Article>>

    suspend fun getLatestArticles() : NetworkResource<List<Article>>
}