package com.dev.james.sayariproject.models.launch

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class LaunchList(
    val id : String,
    val url : String,
    val slug : String,
    val name : String,
    val status : LaunchStatus?,
    @SerializedName("window_start")
    val date : String,
    @SerializedName("holdreason")
    val hold : String?,
    @SerializedName("failreason")
    val fail : String?,
    @SerializedName("launch_service_provider")
    val serviceProvider : ServiceProvider?,
    val rocket : Rocket?,
    val mission : Mission?,
    val pad : LaunchPad,
    val image : String,
    val probability : Int?
):Parcelable