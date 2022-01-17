package com.dev.james.sayariproject.repository

import androidx.paging.PagingData
import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.models.launch.LaunchList
import com.dev.james.sayariproject.utilities.NetworkResource
import kotlinx.coroutines.flow.Flow

interface BaseMainRepository {
    //fetch articles
    fun getArticlesStream(query : String) : Flow<PagingData<Article>>

    suspend fun getTopArticles() : NetworkResource<List<Article>>

    suspend fun getLatestArticles() : NetworkResource<List<Article>>

    suspend fun getFilteredNews(query : String) : NetworkResource<List<Article>>

    fun getLaunchesStream(query: String , fragId : Int) : Flow<PagingData<LaunchList>>





}