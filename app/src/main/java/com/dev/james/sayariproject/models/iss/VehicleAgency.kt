package com.dev.james.sayariproject.models.iss

import com.google.gson.annotations.SerializedName

data class VehicleAgency(
    val id : Int,
    val url : String,
    val name : String,
    val featured : Boolean,
    val type : String,
    @SerializedName("country_code")
    val countryCode : String,
    val abbrev : String,
    val description: String,
    val administrator : String,
    @SerializedName("founding_year")
    val foundingYear : String,
    val launchers : String,
    val spacecraft : String,
    val parent : Any,
    @SerializedName("image_url")
    val imageUrl : String
)
