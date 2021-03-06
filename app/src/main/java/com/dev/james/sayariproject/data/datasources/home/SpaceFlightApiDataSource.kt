package com.dev.james.sayariproject.data.datasources.home

import androidx.paging.PagingData
import com.dev.james.sayariproject.models.articles.Article
import kotlinx.coroutines.flow.Flow

interface SpaceFlightApiDataSource {

    //returns news article data stream
    fun getArticlesResultStream(query : String?) : Flow<PagingData<Article>>


}