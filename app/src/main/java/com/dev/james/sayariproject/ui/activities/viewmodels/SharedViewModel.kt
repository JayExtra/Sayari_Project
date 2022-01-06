package com.dev.james.sayariproject.ui.activities.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dev.james.sayariproject.utilities.Event

class SharedViewModel : ViewModel() {

    private val _queryPassed = MutableLiveData<Event<String?>>()
    val queryPassed: LiveData<Event<String?>> get() = _queryPassed

    fun receiveQuery(query: String?) {
        _queryPassed.value = Event(query)
    }
}