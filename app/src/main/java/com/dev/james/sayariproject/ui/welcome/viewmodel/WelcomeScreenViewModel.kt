package com.dev.james.sayariproject.ui.welcome.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dev.james.sayariproject.repository.DatastoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class WelcomeScreenViewModel @Inject constructor(
    private val datastoreRepository: DatastoreRepository
) : ViewModel() {

 /**   //get on boarding status
    val onBoardingValue : StateFlow<Boolean> get() =
        datastoreRepository.getOnBoardingStatus()
            .stateIn(viewModelScope , SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000) , false )

 **/
    val onBoardingValue = datastoreRepository.getOnBoardingStatus().asLiveData()

    //set on boarding value
    fun setOnBoardingValue(value : Boolean) = viewModelScope.launch {
        try {
            datastoreRepository.setOnBoardingStatus(value)
        }catch (e : Exception){
            Log.i("WelcomeScreenViewModel", "setOnBoardingValue: error - $e")
        }
    }

}