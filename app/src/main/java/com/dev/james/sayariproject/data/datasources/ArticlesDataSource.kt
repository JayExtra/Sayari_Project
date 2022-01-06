package com.dev.james.sayariproject.data.datasources

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dev.james.sayariproject.data.remote.paging.ArticlePagingSource
import com.dev.james.sayariproject.data.remote.service.NewsApiService
import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.utilities.ARTICLE_NETWORK_PAGE_SIZE
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ArticlesDataSource @Inject constructor(
    private val articleApi : NewsApiService
) : SpaceFlightApiDataSource {

    //returns a flow of articles wrapped around a PagingData object
    override fun getArticlesResultStream(query: String?): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = ARTICLE_NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ArticlePagingSource(query , articleApi) }
        ).flow
    }
}

