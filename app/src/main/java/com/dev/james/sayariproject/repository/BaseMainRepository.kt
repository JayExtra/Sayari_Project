package com.dev.james.sayariproject.repository

import androidx.paging.PagingData
import com.dev.james.sayariproject.models.Article
import com.dev.james.sayariproject.utilities.NetworkResource
import kotlinx.coroutines.flow.Flow

interface BaseMainRepository {
    //fetch articles
    fun getArticlesStream(query : String) : Flow<PagingData<Article>>

    suspend fun getTopArticles() : NetworkResource<List<Article>>


}