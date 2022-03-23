package com.dev.james.sayariproject.ui.launches.launchdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.james.sayariproject.models.launch.RocketInstance
import com.dev.james.sayariproject.repository.BaseMainRepository
import com.dev.james.sayariproject.utilities.NetworkResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaunchDetailsViewModel @Inject constructor(
    private val repository: BaseMainRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _rocketInstanceResponse : MutableStateFlow<NetworkResource<RocketInstance>> = MutableStateFlow(
        NetworkResource.Loading)
    val rocketInstanceResponse get() = _rocketInstanceResponse.asStateFlow()


    fun getRocketInstance(id : Int) = viewModelScope.launch {
        val rocketResponse = repository.getRocketInstance(id)
        _rocketInstanceResponse.value = rocketResponse
    }

}