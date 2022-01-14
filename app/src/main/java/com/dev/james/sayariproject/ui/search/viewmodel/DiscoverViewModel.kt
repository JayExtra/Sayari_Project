package com.dev.james.sayariproject.ui.search.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dev.james.sayariproject.repository.BaseMainRepository
import com.dev.james.sayariproject.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val repository: BaseMainRepository
) : ViewModel() {

    private var _stringParameter :  MutableLiveData<Event<String>> = MutableLiveData()
    val stringParameter get() = _stringParameter

    fun updateStringParameter(param : String){
        _stringParameter.value = Event(param)
    }

}