package com.dev.james.sayariproject.ui.settings.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dev.james.sayariproject.repository.DatastoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val datastoreRepository: DatastoreRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val settingsPreferencesFlow = datastoreRepository.getAllSettingsPreferences().asLiveData()

    fun setLightDarkModeValue(value : Boolean) = viewModelScope.launch {
        datastoreRepository.setDarLightModeStatus(value)
    }
    fun setFavouritesNotificationValue(value : Boolean) = viewModelScope.launch {
        datastoreRepository.setFavouriteAgenciesStatus(value)
    }
    fun setThirtyMinValue(value : Boolean) = viewModelScope.launch {
        datastoreRepository.setThrityMinIntervalStatus(value)
    }
    fun setFifteenMinuteStatus(value : Boolean) = viewModelScope.launch {
        datastoreRepository.setFifteenMinIntervalStatus(value)
    }
    fun setFiveMinuteValue(value : Boolean) = viewModelScope.launch {
        datastoreRepository.setFiveMinIntervalStatus(value)
    }

}