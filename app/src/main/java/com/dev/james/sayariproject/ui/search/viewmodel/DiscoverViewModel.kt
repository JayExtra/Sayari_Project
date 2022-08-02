package com.dev.james.sayariproject.ui.search.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.models.discover.ActiveMissions
import com.dev.james.sayariproject.repository.BaseMainRepository
import com.dev.james.sayariproject.utilities.Event
import com.dev.james.sayariproject.utilities.NetworkResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val repository: BaseMainRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var _stringParameter :  MutableLiveData<Event<String>> = MutableLiveData()
    val stringParameter get() = _stringParameter

    //images stateflow
    private var _articles : MutableLiveData<NetworkResource<List<Article>>> = MutableLiveData()
    val myArticlesListForImages get() = _articles

  private var _newsList :  MutableLiveData<NetworkResource<List<Article>>> = MutableLiveData()
    val newsList get() = _newsList

    private val _missionsListFlow :  MutableStateFlow<List<ActiveMissions>> = MutableStateFlow(emptyList())
    val missionsList get() = _missionsListFlow.asStateFlow()

    private lateinit var storedQuery : String
    private lateinit var storedMissionQuery : String

  //  private lateinit var missionsFlow : LiveData<List<ActiveMissions>>

    fun updateStringParameter(param : String){
        _stringParameter.value = Event(param)
    }


    //getting news for the news card
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
           _newsList.value = NetworkResource.Loading
           _newsList.value = articles
       }else{
           val articles = repository.getFilteredNews(initialQuery)
           _newsList.value = NetworkResource.Loading
           _newsList.value = articles
       }

    }

    //getting missions from DB:
    fun getMissionsByCategory(category : String) = viewModelScope.launch {
        val initialQuery: String = savedStateHandle.get<String>(LAST_MISSION_QUERY) ?: category
        repository.getMissions(initialQuery).collect{
               _missionsListFlow.value = it
        }
        storedMissionQuery = initialQuery

  //          missionsFlow = repository.getMissions(category).asLiveData()

    }

    //getting images from network
    fun getArticlesForImages(category : String) = viewModelScope.launch {
        when (category) {
            "Mars" -> {
                Log.d("DiscVm", "getArticlesForImages: mars match found! ")
                val result =  repository.getArticlesForImages(
                    "curiosity" , "perseverance" , "MRO" , "ingenuity" , "MAVEN" ,
                    "opportunity" ,null,null,null
                )
                Log.d("DiscVm", "getArticlesForImages: mars: ${result.toString()} ")
                _articles.value = NetworkResource.Loading
                _articles.value = result
            }
            "Moon" -> {
                val result = repository.getArticlesForImages(
                    "LRO" , "Artemis" , "Apollo" , "chandrayaan","chang'e" ,"lunar rover",
                    "lunar" , null , null
                )
                _articles.value = NetworkResource.Loading
                _articles.value = result
            }
            "Solar System" -> {
                val result = repository.getArticlesForImages(
                    "pluto" , "venus" , "mercury" , "saturn","jupiter" ,null,
                    "osiris" , "lucy" , "cassini"
                )
                _articles.value = NetworkResource.Loading
                _articles.value =  result
            }
            "Astronomy" -> {
                val result = repository.getArticlesForImages(
                    "hubble" , null  , "chandra" , "galaxy","spitzer" ,null,
                    "black hole" , null, null
                )
                _articles.value = NetworkResource.Loading
                _articles.value =  result
            }
            "Exoplanets" -> {
                val result = repository.getArticlesForImages(
                    "TESS" , "kepler telescope" , "exoplanet" , "habitable",null ,null,
                    null , null , null
                )
                _articles.value = NetworkResource.Loading
                _articles.value =  result
            }
            "Sun" -> {
                val result = repository.getArticlesForImages(
                    "parker solar probe" , "coronal" , null , null,null ,null,
                    null , null , null
                )
                _articles.value = NetworkResource.Loading
                _articles.value =  result
            }
            else -> {
                Log.d("discoverViewModel", "getArticlesForImages: no category found")
            }
        }

    }


    override fun onCleared() {
        super.onCleared()
        savedStateHandle[LAST_SEARCH_QUERY] = storedQuery
        savedStateHandle[LAST_MISSION_QUERY]= storedMissionQuery
    }
}

private const val LAST_SEARCH_QUERY = "last_search_query"
private const val LAST_MISSION_QUERY = "last_mission_query"
private const val DEFAULT_QUERY =""
private const val SUN_QUERY = "The sun"
