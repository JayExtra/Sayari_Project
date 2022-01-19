package com.dev.james.sayariproject.models.discover

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missions_table")
data class ActiveMissions(
    @PrimaryKey(autoGenerate = true)
    val id : Int ,
    val title : String,
    val missionPatch : String,
    val category : String
)