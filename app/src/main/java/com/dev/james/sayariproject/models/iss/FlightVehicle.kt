package com.dev.james.sayariproject.models.iss

import com.dev.james.sayariproject.models.launch.LaunchList

data class FlightVehicle(
    val id : Int,
    val url : String,
    val spacecraft : SpaceCraft,
    val launch : LaunchList
)
