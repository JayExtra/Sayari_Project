package com.dev.james.sayariproject.data.remote.service

import com.dev.james.sayariproject.models.articles.Article
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    //this interface is responsible for fetching all news related
    //articles

    //get all news
    @GET("articles")
    suspend fun getNewsArticles(
        @Query("title_contains") query : String?,
        @Query("_start")start : Int,
        @Query("_limit")limit : Int
        ) : List<Article>

    @GET("articles")
    suspend fun getTopArticles(
        @Query("_featured") featured : Boolean,
        @Query("_limit")limit : Int
    ) : List<Article>

}