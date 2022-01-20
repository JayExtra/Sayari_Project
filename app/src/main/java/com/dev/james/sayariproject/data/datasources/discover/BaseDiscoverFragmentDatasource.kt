package com.dev.james.sayariproject.data.datasources.discover

import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.models.discover.ActiveMissions
import com.dev.james.sayariproject.utilities.NetworkResource
import kotlinx.coroutines.flow.Flow

interface BaseDiscoverFragmentDatasource {
    //news section
    suspend fun getFilteredNews(filter : String) : NetworkResource<List<Article>>

    //mission section in the future
    fun getMission(category : String) : Flow<List<ActiveMissions>>
    //gallery section
}