package com.dev.james.sayariproject.ui.events

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dev.james.sayariproject.models.events.Events
import com.dev.james.sayariproject.repository.BaseMainRepository
import com.dev.james.sayariproject.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val repository: BaseMainRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var _searchState : MutableLiveData<Event<Boolean>>? = null
    val searchState get() = _searchState

    val uiState : StateFlow<UiState>

    val pagingDataFlow : Flow<PagingData<Events>>

    val accept : (UiAction) -> Unit

    init {

        val initialQuery = savedStateHandle.get<String>(LAST_SEARCH_QUERY) ?: DEFAULT_QUERY

        val actionStateFlow = MutableSharedFlow<UiAction>()

        val searches = actionStateFlow
            .filterIsInstance<UiAction.Search>()
            .distinctUntilChanged()
            .onStart { emit(UiAction.Search(query = initialQuery)) }

        pagingDataFlow = searches
            .flatMapLatest {
                searchEvents(queryString = it.query)
            }
            .cachedIn(viewModelScope)

        uiState = searches.map { search ->
            UiState(
                query = search.query
            )
        }.stateIn(
           scope = viewModelScope,
            started =  SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = UiState()
        )

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }

    }

    fun updateSearchState(boolean : Boolean) = viewModelScope.launch {
        _searchState?.value = Event(boolean)
    }

    override fun onCleared() {
        super.onCleared()
        savedStateHandle[LAST_SEARCH_QUERY] = uiState.value.query
    }
    private fun searchEvents(queryString : String?) : Flow<PagingData<Events>> =
        repository.getEvents(queryString).cachedIn(viewModelScope)
}

sealed class UiAction {
    data class Search(val query : String) : UiAction()
}

data class UiState(
    val query : String = DEFAULT_QUERY,
    val pagingData : PagingData<Events> = PagingData.empty()
)
private const val LAST_SEARCH_QUERY = "last_search_query"
private const val  DEFAULT_QUERY = ""

