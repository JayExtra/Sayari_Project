package com.dev.james.sayariproject.ui.search.viewmodel

import android.util.Log
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

    private lateinit var storedQuery : String

    fun updateStringParameter(param : String){
        _stringParameter.value = Event(param)
    }


    fun getFilteredResults(query : String?) = viewModelScope.launch {
        var queryPassed = ""
       query?.let {
            queryPassed = it
       }

        val initialQuery: String = savedStateHandle.get<String>(LAST_SEARCH_QUERY) ?: queryPassed

        storedQuery = initialQuery

        Log.d("DiscoverVm", "getFilteredResults: $initialQuery ")

       if(initialQuery == "Sun"){

           val articles = repository.getFilteredNews(SUN_QUERY)
           _newsList.value = Event(NetworkResource.Loading)
           _newsList.value = Event(articles)
       }else{
           val articles = repository.getFilteredNews(initialQuery)
           _newsList.value = Event(NetworkResource.Loading)
           _newsList.value = Event(articles)
       }

    }

    override fun onCleared() {
        super.onCleared()
        savedStateHandle[LAST_SEARCH_QUERY] = storedQuery
    }
}

private const val LAST_SEARCH_QUERY = "last_search_query"
private const val DEFAULT_QUERY =""
private const val SUN_QUERY = "The sun"
