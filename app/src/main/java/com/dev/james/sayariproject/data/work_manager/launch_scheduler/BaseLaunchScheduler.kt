package com.dev.james.sayariproject.data.work_manager.launch_scheduler

import com.dev.james.sayariproject.models.events.Events

interface BaseLaunchScheduler {
    suspend fun initScheduler()
    suspend fun setEventAlarm(event : Events)
}