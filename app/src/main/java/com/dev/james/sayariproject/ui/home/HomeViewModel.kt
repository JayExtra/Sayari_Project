package com.dev.james.sayariproject.ui.home


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dev.james.sayariproject.models.Article
import com.dev.james.sayariproject.repository.BaseMainRepository
import com.dev.james.sayariproject.utilities.Event
import com.dev.james.sayariproject.utilities.NetworkResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: BaseMainRepository
)  : ViewModel() {


    private val _topArticlesLiveData : MutableLiveData<Event<NetworkResource<List<Article>>>> =
        MutableLiveData()
    val topArticlesLiveData get() = _topArticlesLiveData


    val uiState : StateFlow<UiState>

    val pagingDataFlow : Flow<PagingData<Article>>

    val accept: (UiAction) -> Unit

    init {
        //on initialization get list of top articles
        getTopArticles()

        val actionStateFlow = MutableSharedFlow<UiAction>()

        val results = actionStateFlow
            .filterIsInstance<UiAction.Fetch>()
            .distinctUntilChanged()
            .onStart { emit(UiAction.Fetch(query = DEFAULT_QUERY)) }

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

    private fun getTopArticles() = viewModelScope.launch {
        _topArticlesLiveData.value = Event(NetworkResource.Loading)
        _topArticlesLiveData.value = Event(repository.getTopArticles())
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
