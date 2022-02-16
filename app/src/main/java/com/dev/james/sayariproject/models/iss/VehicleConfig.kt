package com.dev.james.sayariproject.models.iss

import com.google.gson.annotations.SerializedName

data class VehicleConfig(
    val id : Int,
    val url : String,
    val name : String,
    val type : VehicleType,
    val agency : VehicleAgency,
    @SerializedName("in_use")
    val inUse : Boolean,
    val capability : String,
    val history : String,
    val details : String,
    @SerializedName("maiden_flight")
    val maidenFlight : String,
    val height : Double,
    val diameter : Double,
    @SerializedName("human_rated")
    val humanRated : Boolean,
    @SerializedName("crew_capacity")
    val crewCapacity : Int,
    @SerializedName("payload_capacity")
    val payloadCapacity : Int,
    @SerializedName("flight_life")
    val flightLife : String,
    @SerializedName("image_url")
    val imageUrl : String,
    @SerializedName("nation_url")
    val nationUrl : Any?,
    @SerializedName("wiki_url")
    val wikiUrl : String,
    @SerializedName("info_link")
    val infoLink : String
)
