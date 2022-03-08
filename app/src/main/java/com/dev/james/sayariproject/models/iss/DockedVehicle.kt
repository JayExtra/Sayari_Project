package com.dev.james.sayariproject.models.iss

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DockedVehicle(
    val id  :Int,
    val url : String,
    @SerializedName("docking")
    val dockedDate : String,
    val departure : String?,
    @SerializedName("flight_vehicle")
    val flightVehicle : FlightVehicle
): Parcelable