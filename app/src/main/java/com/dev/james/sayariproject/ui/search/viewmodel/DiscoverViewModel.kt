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
    private val _articlesFlow = MutableStateFlow<Event<NetworkResource<List<Article>>>>(Event(NetworkResource.Loading))
    val myArticlesListForImages : StateFlow<Event<NetworkResource<List<Article>>>> get() = _articlesFlow

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
    fun getArticlesForImages(
        category : String
    ) = viewModelScope.launch {
        if(category == "Mars"){
           val result =  repository.getArticlesForImages(
                "curiosity" , "perseverance" , "MRO" , "ingenuity" , "MAVEN" ,
               "opportunity" ,null,null,null
            )
            _articlesFlow.value = Event(result)
        }else if(category == "Moon"){
            val result = repository.getArticlesForImages(
                "LRO" , "Artemis" , "Apollo" , "chandrayaan","chang'e" ,"lunar rover",
                "lunar" , null , null
            )
            _articlesFlow.value = Event(result)
        }else if(category == "Solar System"){
            val result = repository.getArticlesForImages(
                "pluto" , "venus" , "mercury" , "saturn","jupiter" ,"asteroid",
                "osiris" , "lucy" , "cassini"
            )
            _articlesFlow.value = Event(result)
        }else if(category == "Astronomy"){
            val result = repository.getArticlesForImages(
                "hubble" , "JWST" , "chandra" , "galaxy","star" ,"spitzer",
                "black hole" , null , null
            )
            _articlesFlow.value = Event(result)
        }else if(category == "Exoplanets"){
            val result = repository.getArticlesForImages(
                "TESS" , "kepler telescope" , "exoplanet" , "habitable",null ,null,
                null , null , null
            )
            _articlesFlow.value = Event(result)
        }else if(category == "Sun"){
            val result = repository.getArticlesForImages(
                "solar" , "parker solar probe" , "coronal" , "habitable","SOHO" ,null,
                null , null , null
            )
            _articlesFlow.value = Event(result)
        }else{
            Log.d("discoverViewModel", "getArticlesForImages: no category found")
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
