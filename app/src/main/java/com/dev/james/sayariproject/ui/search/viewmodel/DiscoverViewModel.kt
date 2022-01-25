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
    private var _articles : MutableLiveData<Event<NetworkResource<List<Article>>>> = MutableLiveData()
    val myArticlesListForImages get() = _articles

  private var _newsList :  MutableLiveData<Event<NetworkResource<List<Article>>>> = MutableLiveData()
    val newsList get() = _newsList

    private var _missionsList :  MutableLiveData<Event<List<ActiveMissions>>> = MutableLiveData()
    val missionsList get() = _missionsList

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
           _newsList.value = Event(NetworkResource.Loading)
           _newsList.value = Event(articles)
       }else{
           val articles = repository.getFilteredNews(initialQuery)
           _newsList.value = Event(NetworkResource.Loading)
           _newsList.value = Event(articles)
       }

    }

    //getting missions from DB:
    fun getMissionsByCategory(category : String) {
        val initialQuery: String = savedStateHandle.get<String>(LAST_MISSION_QUERY) ?: category
        try {
            val missions = repository.getMissions(initialQuery)
            storedMissionQuery = initialQuery
            viewModelScope.launch {
                missions.collect { missions ->
                    _missionsList.value = Event(missions)
                }
            }
  //          missionsFlow = repository.getMissions(category).asLiveData()

        }catch (e : Exception){
            Log.d("DiscoverVm", "getMissionsByCategory: ${e.toString()}")
        }
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
                _articles.value = Event(NetworkResource.Loading)
                _articles.value = Event(result)
            }
            "Moon" -> {
                val result = repository.getArticlesForImages(
                    "LRO" , "Artemis" , "Apollo" , "chandrayaan","chang'e" ,"lunar rover",
                    "lunar" , null , null
                )
                _articles.value = Event(NetworkResource.Loading)
                _articles.value =  Event(result)
            }
            "Solar System" -> {
                val result = repository.getArticlesForImages(
                    "pluto" , "venus" , "mercury" , "saturn","jupiter" ,null,
                    "osiris" , "lucy" , "cassini"
                )
                _articles.value = Event(NetworkResource.Loading)
                _articles.value =  Event(result)
            }
            "Astronomy" -> {
                val result = repository.getArticlesForImages(
                    "hubble" , null  , "chandra" , "galaxy","spitzer" ,null,
                    "black hole" , null, null
                )
                _articles.value = Event(NetworkResource.Loading)
                _articles.value =  Event(result)
            }
            "Exoplanets" -> {
                val result = repository.getArticlesForImages(
                    "TESS" , "kepler telescope" , "exoplanet" , "habitable",null ,null,
                    null , null , null
                )
                _articles.value = Event(NetworkResource.Loading)
                _articles.value =  Event(result)
            }
            "Sun" -> {
                val result = repository.getArticlesForImages(
                    "parker solar probe" , "coronal" , null , null,null ,null,
                    null , null , null
                )
                _articles.value = Event(NetworkResource.Loading)
                _articles.value =  Event(result)
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
