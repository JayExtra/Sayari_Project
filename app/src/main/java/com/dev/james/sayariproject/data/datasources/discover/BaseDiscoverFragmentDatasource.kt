package com.dev.james.sayariproject.data.datasources.discover

import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.utilities.NetworkResource

interface BaseDiscoverFragmentDatasource {
    //news section
    suspend fun getFilteredNews(filter : String) : NetworkResource<List<Article>>

    //mission section in the future
    //gallery section
}