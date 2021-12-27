package com.dev.james.sayariproject.ui.launches.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dev.james.sayariproject.models.launch.LaunchList
import com.dev.james.sayariproject.repository.BaseMainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LaunchesViewModel @Inject constructor(
    private val repository : BaseMainRepository ,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    //private val


     lateinit var uiState : StateFlow<UiState>

     lateinit var pagingDataFlow : Flow<PagingData<LaunchList>>

     lateinit var accept: (UiAction) -> Unit

    fun getLaunches(fragId : Int) = viewModelScope.launch {

        Log.d("LaunchesViewModel", "getLaunches: function called")

        val initialQuery: String = savedStateHandle.get<String>(LAST_SEARCH_QUERY) ?: DEFAULT_QUERY

        val actionStateFlow = MutableSharedFlow<UiAction>()

        val searches = actionStateFlow
            .filterIsInstance<UiAction.Search>()
            .distinctUntilChanged()
            .onStart { emit(UiAction.Search(query = initialQuery))}

        pagingDataFlow = searches
            .flatMapLatest {
                searchLaunches(queryString = it.query , fragId)
            }
            .cachedIn(viewModelScope)

        uiState = searches.map { search ->
            UiState(
                query = search.query
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000 ),
            initialValue = UiState()
        )

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action)}
        }

    }


    override fun onCleared() {
        savedStateHandle[LAST_SEARCH_QUERY] = uiState.value.query
        super.onCleared()
    }


    private fun searchLaunches(queryString : String, fragId:Int) : Flow<PagingData<LaunchList>> =
        repository.getLaunchesStream(queryString , fragId).cachedIn(viewModelScope)



}

sealed class UiAction {
    data class Search(val query : String) : UiAction()
}

data class UiState(
    val query : String = DEFAULT_QUERY,
    val pagingData : PagingData<LaunchList> = PagingData.empty()
)

private const val LAST_SEARCH_QUERY = "last_search_query"
private const val  DEFAULT_QUERY = ""
