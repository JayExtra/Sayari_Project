package com.dev.james.sayariproject.models.iss

import com.google.gson.annotations.SerializedName

data class DockedVehicle(
    val id  :Int,
    val url : String,
    @SerializedName("docking")
    val dockedDate : String,
    val departure : String?,
    @SerializedName("flight_vehicle")
    val flightVehicle : FlightVehicle
)