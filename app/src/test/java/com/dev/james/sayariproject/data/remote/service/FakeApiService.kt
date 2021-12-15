package com.dev.james.sayariproject.data.remote.service

import android.util.Log
import com.dev.james.sayariproject.models.Article
import com.dev.james.sayariproject.models.ArticleEvents
import com.dev.james.sayariproject.models.ArticlesLaunches
import java.lang.Exception

class FakeApiService : NewsApiService {

    override suspend fun getNewsArticles(query: String?, start: Int, limit: Int): List<Article> {
        val article1 = Article(
            id =  13244,
            title =  "China",
            url= "https://spacenews.com/china-sends-classified-shijian-satellites-into-orbit-with-milestone-long-march-launch/",
            imageUrl =  "https://spacenews.com/wp-content/uploads/2021/12/shijian-6-05-longmarch4b-jslc-10dec2021-CNSA.jpg",
            site =  "SpaceNews",
            summary = "A Long March 4B launched the Shijian-06 (05) group of satellites Dec. 9, marking the 400th launch of China’s Long March family of launch vehicles.",
            date = "2021-12-10T09:40:11.000Z",
            featured= false,
            launches = listOf(ArticlesLaunches(id=1 , "China")),
            events = listOf(ArticleEvents(id = 1 , "China"))
        )

        val article2 = Article(
            id =  13246,
            title =  "America",
            url= "https://spacenews.com/china-sends-classified-shijian-satellites-into-orbit-with-milestone-long-march-launch/",
            imageUrl =  "https://spacenews.com/wp-content/uploads/2021/12/shijian-6-05-longmarch4b-jslc-10dec2021-CNSA.jpg",
            site =  "SpaceNews",
            summary = "A Long March 4B launched the Shijian-06 (05) group of satellites Dec. 9, marking the 400th launch of China’s Long March family of launch vehicles.",
            date = "2021-12-10T09:40:11.000Z",
            featured= false,
            launches = listOf(ArticlesLaunches(id=1 , "China")),
            events = listOf(ArticleEvents(id = 1 , "China"))
        )

        return listOf(article1 , article2)
    }

    fun getFirstPage( data : List<Article>) : FakeNetworkResponse {
        return FakeNetworkResponse(
            articles = data,
            count = 2 ,
            limit = 2 ,
            next = 2 ,
            previous = null
        )
    }

   fun searchArticle(queryString : String , articles: List<Article>): FakeNetworkResponse {
       val searchResult = mutableListOf<Article>()
       articles.forEach {
           if (it.title == queryString)
               searchResult.add(it)
       }

       return FakeNetworkResponse(
           articles = searchResult,
           count = 2 ,
           limit = 1 ,
           next = null ,
           previous = null
       )



   }

}

data class FakeNetworkResponse(
    val articles : List<Article>,
    val count : Int ,
    val limit : Int,
    val next : Int?,
    val previous : Int?
)

