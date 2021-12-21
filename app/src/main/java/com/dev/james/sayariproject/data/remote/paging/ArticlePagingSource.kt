package com.dev.james.sayariproject.data.remote.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dev.james.sayariproject.data.remote.service.NewsApiService
import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.utilities.ARTICLE_SEARCH_LOAD_SIZE
import com.dev.james.sayariproject.utilities.ARTICLE_STARTING_INDEX
import retrofit2.HttpException
import java.io.IOException

class ArticlePagingSource(
    private val queryString : String?,
    private val articlesApi : NewsApiService
) : PagingSource<Int , Article>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val start = params.key ?: ARTICLE_STARTING_INDEX

        val limit = if(queryString == null) params.loadSize else ARTICLE_SEARCH_LOAD_SIZE
        return try {
            val articles = articlesApi.getNewsArticles(
                    queryString,
                    start,
                    limit
                )
            LoadResult.Page(
                data = articles,
                prevKey = if(start == ARTICLE_STARTING_INDEX) null else start - limit,
                nextKey = if(articles.isEmpty()) null else start + limit
            )

        }catch (e : IOException){
            Log.i("ArticlesDataSource", "load whoops! IOException: $e ")
            LoadResult.Error(e)
        }catch (e :HttpException){
            Log.i("ArticlesDataSource", "load whoops! HttpException: ${e.message()} ")
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
       return state.anchorPosition?.let { anchorPos ->
           state.closestPageToPosition(anchorPos)?.prevKey?.plus(30)
               ?: state.closestPageToPosition(anchorPos)?.nextKey?.minus(30)
       }
    }
}