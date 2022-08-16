package com.dev.james.sayariproject.models.events

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_events_alerts")
data class ScheduledEventAlert(
    @PrimaryKey(autoGenerate = false)
    val id : Int,
    val url : String,
    val slug : String,
    val name : String,
    val type : String ,
    val description : String,
    val date : String
)
