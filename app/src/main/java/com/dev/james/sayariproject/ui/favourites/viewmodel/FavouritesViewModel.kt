package com.dev.james.sayariproject.ui.favourites.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.james.sayariproject.models.favourites.AgencyResponse
import com.dev.james.sayariproject.models.favourites.Result
import com.dev.james.sayariproject.repository.BaseMainRepository
import com.dev.james.sayariproject.utilities.NetworkResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val repository: BaseMainRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var _agencySearchResult : MutableStateFlow<List<Result>> = MutableStateFlow(emptyList())
    val agencySearchResult get() = _agencySearchResult.asStateFlow()

    private var _uiActions = MutableSharedFlow<UiActions>()
    val uiActions get() = _uiActions.asSharedFlow()


    private var favouriteAgenciesList : StateFlow<List<Result>?> =
        repository.getFavouriteAgenciesFromDb().stateIn(
            viewModelScope , SharingStarted.Lazily , null
        )

    fun saveFavouriteAgency(agency : Result) = viewModelScope.launch {
        //save favourite agency to database
        try {
            repository.saveFavouriteAgency(agency)
        }catch (e : Exception){
            Log.d("FavouritesVm", "saveFavouriteAgency: exception => ${e.localizedMessage} ")
        }
    }

    fun searchAgencyFromApi(name : String) = viewModelScope.launch {

        when(val agencyResponse = repository.getFavouriteAgenciesFromApi(name)){
            is NetworkResource.Loading -> {
                _uiActions.emit(
                    UiActions(
                        errorMessage = null ,
                        showProgressBar = true,
                        showNetErrMessage = false,
                        showNetImage = false,
                        showRetryButton = false
                    )
                )
            }
            is NetworkResource.Success -> {
                _uiActions.emit(
                    UiActions(
                        errorMessage = null ,
                        showProgressBar = false,
                        showNetErrMessage = false,
                        showNetImage = false,
                        showRetryButton = false
                    )
                )
                val agencyList = agencyResponse.value.results
                if(agencyList.isEmpty()){
                    _uiActions.emit(
                        UiActions(
                            errorMessage = "Could not find the agency defined , please try again",
                            showProgressBar = false,
                            showNetErrMessage = true,
                            showNetImage = false,
                            showRetryButton = false
                        )
                    )
                }else {
                    _agencySearchResult.value = agencyList
                }
            }
            is NetworkResource.Failure -> {
                val error = agencyResponse.errorBody
                val errorCode = agencyResponse.errorCode
                errorCode?.let {
                    if (errorCode == NET_ERROR_CODE){
                        _uiActions.emit(
                            UiActions(
                                errorMessage = null,
                                showProgressBar = false,
                                showNetErrMessage = true,
                                showNetImage = true,
                                showRetryButton = true
                            )
                        )
                    }else{
                        error?.let {
                            _uiActions.emit(
                                UiActions(
                                    errorMessage = it.toString(),
                                    showProgressBar = false,
                                    showNetErrMessage = true,
                                    showNetImage = true,
                                    showRetryButton = true
                                )
                            )
                        }
                    }
                }


            }
        }


    }

}

data class UiActions(
    val errorMessage : String?,
    val showProgressBar : Boolean,
    val showNetImage : Boolean,
    val showNetErrMessage : Boolean,
    val showRetryButton : Boolean
)

private const val NET_ERROR_CODE = 404