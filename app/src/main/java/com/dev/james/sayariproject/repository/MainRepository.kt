package com.dev.james.sayariproject.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.dev.james.sayariproject.data.datasources.ArticlesDataSource
import com.dev.james.sayariproject.data.remote.paging.ArticlePagingSource
import com.dev.james.sayariproject.data.remote.service.NewsApiService
import com.dev.james.sayariproject.models.Article
import com.dev.james.sayariproject.utilities.ARTICLE_NETWORK_PAGE_SIZE
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val articlesDataSource: ArticlesDataSource
) : BaseMainRepository {
    override fun getArticlesStream(query: String): Flow<PagingData<Article>> {
        return articlesDataSource.getArticlesResultStream(query)
    }

}