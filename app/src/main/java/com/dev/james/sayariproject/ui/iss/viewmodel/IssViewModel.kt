package com.dev.james.sayariproject.ui.iss.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.james.sayariproject.models.astronaut.Astronaut
import com.dev.james.sayariproject.models.events.EventResponse
import com.dev.james.sayariproject.models.iss.IntSpaceStation
import com.dev.james.sayariproject.repository.BaseMainRepository
import com.dev.james.sayariproject.utilities.Event
import com.dev.james.sayariproject.utilities.NetworkResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

@HiltViewModel
class IssViewModel @Inject constructor(
    private val repository : BaseMainRepository
) : ViewModel() {

    private var getSpaceStationJob : Job? = null

    //define an observer that we will use monitor data changes in the UI
    private var _spaceStation : MutableStateFlow<NetworkResource<IntSpaceStation>> = MutableStateFlow(NetworkResource.Loading)
    val spaceStation get() = _spaceStation

    //docked vehicles statistics observer
    private var _dockedVehiclesStats : MutableLiveData<DockedVehiclesStats> = MutableLiveData()
    val dockedVehiclesStats get() = _dockedVehiclesStats

    //define an observer that we will use monitor data changes in the UI
    private var _spaceStationEvents : MutableStateFlow<Event<NetworkResource<EventResponse>>> = MutableStateFlow(Event(NetworkResource.Loading))
    val spaceStationEvents get() = _spaceStationEvents

    //setup chip selection observers
    private var _selectedChip : MutableLiveData<Event<String>> = MutableLiveData()
    val selectedChip get() = _selectedChip

    //observer for the astronaut details
    private var _showAstronaut : MutableSharedFlow<NetworkResource<Astronaut>> = MutableSharedFlow()
    val showAstronaut get() = _showAstronaut.asSharedFlow()


    //get iss response from api using repository

    init {
        getSpaceStationData()
    }

    fun getSelectedChip(chip : String) {
        _selectedChip.value = Event(chip)
    }

    fun getAstronaut(id : Int) = viewModelScope.launch {
        val astronaut = repository.getAstronaut(id)
        _showAstronaut.emit(astronaut)
    }

    fun getDockedVehiclesStatistics(value: IntSpaceStation) = viewModelScope.launch{
        //calculate available
        val allDockingPorts = value.dockingLocation.size
        val dockedVehiclesCount = value.dockingLocation.filter { it.docked != null }.size
        val freePorts = allDockingPorts - dockedVehiclesCount

        val percentageCapacity =
            calculateCapacityPercentage((abs(dockedVehiclesCount - freePorts)), allDockingPorts)
        val percentageDocked = calculateCapacityPercentage(dockedVehiclesCount, allDockingPorts)
        val percentageFree = calculateCapacityPercentage(freePorts, allDockingPorts)

        val dockedVehiclesStatistics = DockedVehiclesStats(
          totalDockedVehicles =   dockedVehiclesCount,
            percentageDockedVehicle =  percentageDocked,
            percentageCapacity = percentageCapacity,
            freePorts = freePorts,
            percentageFreePorts = percentageFree
        )
        _dockedVehiclesStats.postValue(dockedVehiclesStatistics)
    }

    private fun calculateCapacityPercentage(d: Int, t: Int): Int {
        return ((d.toDouble() / t) * 100).roundToInt()
    }

    private fun getSpaceStationData() {
        getSpaceStationJob = viewModelScope.launch {
            val spaceStationData = repository.getSpaceStation()
            val spaceStationEventsData = repository.getSpaceStationEvents()

            _spaceStation.value = spaceStationData
            _spaceStationEvents.value = Event(spaceStationEventsData)
        }
    }

    override fun onCleared() {
        super.onCleared()
        getSpaceStationJob?.cancel()
    }


}

data class DockedVehiclesStats(
    val totalDockedVehicles : Int,
    val percentageDockedVehicle : Int ,
    val percentageCapacity : Int,
    val freePorts : Int,
    val percentageFreePorts : Int
)