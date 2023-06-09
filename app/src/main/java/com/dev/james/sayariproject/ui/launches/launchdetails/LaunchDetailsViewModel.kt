package com.dev.james.sayariproject.ui.launches.launchdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.james.sayariproject.models.launch.Agency
import com.dev.james.sayariproject.models.launch.RocketInstance
import com.dev.james.sayariproject.repository.BaseMainRepository
import com.dev.james.sayariproject.repository.DatastoreRepository
import com.dev.james.sayariproject.utilities.NetworkResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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

    private val _agencyInstanceResponse : MutableStateFlow<NetworkResource<Agency>> = MutableStateFlow(
        NetworkResource.Loading)
    val agencyInstanceResponse get() = _agencyInstanceResponse.asStateFlow()


    fun getRocketInstance(id : Int) = viewModelScope.launch {
        val rocketResponse = repository.getRocketInstance(id)
        _rocketInstanceResponse.value = rocketResponse
    }



    fun getAgencyResponse(id : Int) = viewModelScope.launch {
        val agencyResponse = repository.getAgency(id)
        _agencyInstanceResponse.value = agencyResponse
    }

    fun getBothInstances(rocketId : Int , agencyId : Int) = viewModelScope.launch {
        val agencyJob = async { repository.getAgency(agencyId) }
        val rocketJob = async { repository.getRocketInstance(rocketId)}

        _rocketInstanceResponse.value = rocketJob.await()
        _agencyInstanceResponse.value = agencyJob.await()

    }

}