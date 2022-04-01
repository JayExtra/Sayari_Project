package com.dev.james.sayariproject.ui.home


import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.repository.BaseMainRepository
import com.dev.james.sayariproject.repository.DatastoreRepository
import com.dev.james.sayariproject.utilities.Event
import com.dev.james.sayariproject.utilities.NetworkResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: BaseMainRepository,
    private val savedStateHandle: SavedStateHandle
)  : ViewModel() {


    private val _topArticlesLiveData : MutableLiveData<Event<NetworkResource<List<Article>>>> =
        MutableLiveData()
    val topArticlesLiveData get() = _topArticlesLiveData

    private val _latestArticlesLiveData : MutableLiveData<Event<NetworkResource<List<Article>>>> =
        MutableLiveData()
    val latestArticlesLiveData get() = _latestArticlesLiveData



    lateinit var uiState : StateFlow<UiState>

    lateinit var pagingDataFlow : Flow<PagingData<Article>>

    lateinit var accept: (UiAction) -> Unit

    fun getAllNews(queryReceived: String?) {
        var queryPassed = ""
        queryReceived?.let {
            queryPassed = it
        }
        //on initialization get list of top articles
        val initialQuery: String = savedStateHandle.get<String>(LAST_SEARCH_QUERY) ?: queryPassed


        val actionStateFlow = MutableSharedFlow<UiAction>()

        val results = actionStateFlow
            .filterIsInstance<UiAction.Fetch>()
            .distinctUntilChanged()
            .onStart { emit(UiAction.Fetch(query = initialQuery)) }

        pagingDataFlow = results
            .flatMapLatest { getResults(queryString = it.query) }
            .cachedIn(viewModelScope)

        uiState = results.map { results ->
            UiState(
                query = results.query
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = UiState()
        )

        accept = { action ->
        viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    private fun getResults(queryString : String) : Flow<PagingData<Article>> =
        repository.getArticlesStream(queryString).cachedIn(viewModelScope)

    fun getTopArticles() = viewModelScope.launch {
        _topArticlesLiveData.value = Event(NetworkResource.Loading)
        _topArticlesLiveData.value = Event(repository.getTopArticles())
    }

    fun getLatestArticles() = viewModelScope.launch {
        _latestArticlesLiveData.value = Event(NetworkResource.Loading)
        _latestArticlesLiveData.value = Event(repository.getLatestArticles())
    }

    override fun onCleared() {
        super.onCleared()
        savedStateHandle[LAST_SEARCH_QUERY] = uiState.value.query
    }

}



sealed class UiAction {
  data class Fetch(val query : String) : UiAction()
}

data class UiState(
    val query : String = DEFAULT_QUERY,
    val pagingData : PagingData<Article> = PagingData.empty()
)

private const val  DEFAULT_QUERY = ""
private const val LAST_SEARCH_QUERY = "last_search_query"
