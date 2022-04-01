package com.dev.james.sayariproject.ui.activities.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dev.james.sayariproject.repository.DatastoreRepository
import com.dev.james.sayariproject.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val datastoreRepository: DatastoreRepository
) : ViewModel() {
    val checkSavedPreferences = datastoreRepository.getAllSettingsPreferences().asLiveData()
}