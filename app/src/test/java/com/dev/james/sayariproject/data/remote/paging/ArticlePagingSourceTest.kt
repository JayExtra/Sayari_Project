package com.dev.james.sayariproject.data.remote.paging

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingSource.LoadResult.Page
import androidx.paging.PagingSource.LoadParams.Refresh

import com.dev.james.sayariproject.CoroutineTestRule
import com.dev.james.sayariproject.data.remote.service.FakeApiService
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.jupiter.api.Test
import java.sql.Ref


@ExperimentalCoroutinesApi
class ArticlePagingSourceTest {

    companion object {
        const val DEFAULT_QUERY = ""
        const val ACTUAL_QUERY = "China"
    }

    @get:Rule
    val instantTaskExecutor = InstantTaskExecutorRule()

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()


    private val fakeApiService = FakeApiService()


    @Test
    fun `requesting first data returns first page with previousKey null and next 2 ` () = runBlockingTest {

        //Given - a list of articles contained within a response and
        // the paging source
        val articles = fakeApiService.getNewsArticles(DEFAULT_QUERY , 0 , 0)
        val pagingSource = ArticlePagingSource(DEFAULT_QUERY , fakeApiService)

        //When - the first page response is gotten
        val response = fakeApiService.getFirstPage(articles)

        //then - confirm whether the first page loaded index is 0
        // and previous key is null

        assertEquals(
            Page(
                data = articles,
                prevKey = response.previous,
                nextKey = response.next
            ),
           pagingSource.load(
                Refresh(
                    key = null,
                    loadSize = 2,
                    placeholdersEnabled = false
                )
            )
        )

    }

    @Test
   fun `search query returns actual result` () = runBlockingTest {
       //GIVEN - a list of articles and paging source
        val articles = fakeApiService.getNewsArticles("DEFAULT_QUERY" , 0 , 0)
        val pagingSource = ArticlePagingSource(DEFAULT_QUERY , fakeApiService)

        //WHEN - search action is performed
        val result = fakeApiService.searchArticle(ACTUAL_QUERY , articles)
        val article = result.articles[0]

        //THEN - check whether the result we get from the source is the actual search
        assertEquals(article.title , ACTUAL_QUERY)


   }



}