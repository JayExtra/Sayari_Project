package com.dev.james.sayariproject.ui.iss.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.james.sayariproject.models.iss.IntSpaceStation
import com.dev.james.sayariproject.repository.BaseMainRepository
import com.dev.james.sayariproject.utilities.Event
import com.dev.james.sayariproject.utilities.NetworkResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IssViewModel @Inject constructor(
    private val repository : BaseMainRepository
) : ViewModel() {
    //define an observer that we will use monitor data changes in the UI
    private var _spaceStation : MutableStateFlow<Event<NetworkResource<IntSpaceStation>>> = MutableStateFlow(Event(NetworkResource.Loading))
    val spaceStation get() = _spaceStation

    //setup chip selection observers
    private var _selectedChip : MutableLiveData<Event<String>> = MutableLiveData()
    val selectedChip get() = _selectedChip

    //get iss response from api using repository
    init {
      viewModelScope.launch {
          val spaceStationData = repository.getSpaceStation()
          _spaceStation.value = Event(spaceStationData)
      }
    }

    fun getSelectedChip(chip : String) {
        _selectedChip.value = Event(chip)
    }

}