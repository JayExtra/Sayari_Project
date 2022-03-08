package com.dev.james.sayariproject.models.iss

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SpaceCraft(
    val id : Int,
    val url : String,
    val name : String,
    @SerializedName("serial_number")
    val serialNumber : String?,
    @SerializedName("status")
    val vehicleStatus : VehicleStatus,
    val description : String,
    @SerializedName("spacecraft_config")
    val vehicleConfig : VehicleConfig,

    ) : Parcelable
