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
    private var _agencySearchResult : MutableStateFlow<NetworkResource<AgencyResponse>> = MutableStateFlow(NetworkResource.Loading)
    val agencySearchResult get() = _agencySearchResult.asStateFlow()


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
        _agencySearchResult.value = repository.getFavouriteAgenciesFromApi(name)
    }


}
