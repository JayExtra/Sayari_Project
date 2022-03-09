package com.dev.james.sayariproject.ui.iss.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.james.sayariproject.models.events.EventResponse
import com.dev.james.sayariproject.models.iss.IntSpaceStation
import com.dev.james.sayariproject.repository.BaseMainRepository
import com.dev.james.sayariproject.utilities.Event
import com.dev.james.sayariproject.utilities.NetworkResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

@HiltViewModel
class IssViewModel @Inject constructor(
    private val repository : BaseMainRepository
) : ViewModel() {
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

    //get iss response from api using repository
    init {
      viewModelScope.launch {
          val spaceStationData = repository.getSpaceStation()
          val spaceStationEventsData = repository.getSpaceStationEvents()

          _spaceStation.value = spaceStationData
          _spaceStationEvents.value = Event(spaceStationEventsData)
      }
    }

    fun getSelectedChip(chip : String) {
        _selectedChip.value = Event(chip)
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

}

data class DockedVehiclesStats(
    val totalDockedVehicles : Int,
    val percentageDockedVehicle : Int ,
    val percentageCapacity : Int,
    val freePorts : Int,
    val percentageFreePorts : Int
)