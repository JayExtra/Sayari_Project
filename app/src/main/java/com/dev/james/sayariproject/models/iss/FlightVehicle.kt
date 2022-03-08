package com.dev.james.sayariproject.models.iss

import android.os.Parcelable
import com.dev.james.sayariproject.models.launch.LaunchList
import kotlinx.parcelize.Parcelize

@Parcelize
data class FlightVehicle(
    val id : Int,
    val url : String,
    val spacecraft : SpaceCraft,
    val launch : LaunchList
): Parcelable
