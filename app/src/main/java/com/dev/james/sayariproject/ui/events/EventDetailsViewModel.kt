package com.dev.james.sayariproject.ui.events

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.james.sayariproject.data.work_manager.launch_scheduler.BaseLaunchScheduler
import com.dev.james.sayariproject.models.events.Events
import com.dev.james.sayariproject.models.events.ScheduledEventAlert
import com.dev.james.sayariproject.repository.BaseMainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    private val mainRepository: BaseMainRepository,
    private val scheduler : BaseLaunchScheduler

) : ViewModel() {

    private val _eventDetailsScreenChannel = Channel<EventDetailsScreenEvents>()
    val eventDetailsScreenChannel = _eventDetailsScreenChannel.receiveAsFlow()

    //scheduled alarm for event
    fun scheduleEventAlarm(event : Events) = viewModelScope.launch {
       //1.schedule new alarm
        scheduler.setEventAlarm(event)
       //2. add event to db for later reboot receiver
        saveEventToDatabase(event)
    }

    private suspend fun saveEventToDatabase(event : Events) {
        val scheduledEvent = ScheduledEventAlert(
            id = event.id ,
            url = event.url,
            slug = event.slug,
            name = event.slug,
            type = event.type.name,
            description = event.description,
            date = event.date
        )

        val saveResult = mainRepository.addEvent(scheduledEvent)
        if(saveResult != 0){
            _eventDetailsScreenChannel.send(EventDetailsScreenEvents.SuccessFullAlertScheduling(eventName = event.name))
        }else {
            _eventDetailsScreenChannel.send(EventDetailsScreenEvents.FailedAlertScheduling( errorMess = "Error scheduling the alert"))
        }
    }

    sealed class EventDetailsScreenEvents {
        data class SuccessFullAlertScheduling(val eventName : String) : EventDetailsScreenEvents()
        data class FailedAlertScheduling(val errorMess : String) : EventDetailsScreenEvents()
    }
}