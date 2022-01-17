package com.dev.james.sayariproject.ui.search.viewmodel

import androidx.lifecycle.*
import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.repository.BaseMainRepository
import com.dev.james.sayariproject.utilities.Event
import com.dev.james.sayariproject.utilities.NetworkResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val repository: BaseMainRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var _stringParameter :  MutableLiveData<Event<String>> = MutableLiveData()
    val stringParameter get() = _stringParameter

    private var _newsList :  MutableLiveData<Event<NetworkResource<List<Article>>>> = MutableLiveData()
    val newsList get() = _newsList

    fun updateStringParameter(param : String){
        _stringParameter.value = Event(param)
    }

    fun getFilteredResults(query : String?) = viewModelScope.launch {
        var queryPassed = ""
        query?.let {
            queryPassed = it
        }

        val initialQuery: String = savedStateHandle.get<String>(LAST_SEARCH_QUERY) ?: queryPassed

        val articles = repository.getFilteredNews(initialQuery)

        _newsList.value = Event(articles)

        saveQuery(initialQuery)

    }

    private fun saveQuery(query : String){
        savedStateHandle[LAST_SEARCH_QUERY] = query
    }
}

private const val LAST_SEARCH_QUERY = "last_search_query"
private const val DEFAULT_QUERY =""