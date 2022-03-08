package com.dev.james.sayariproject.models.iss

import ActiveExpedition
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class IntSpaceStation(
    @SerializedName("active_expeditions")
    val activeExpeditions: List<ActiveExpedition>,
    @SerializedName("deorbited")
    val deOrbited: Boolean?,
    val description: String,
    @SerializedName("docking_location")
    val dockingLocation: List<DockingLocation>,
    val founded: String,
    val height: Double,
    val id: Int,
    @SerializedName("image_url")
    val imageUrl: String,
    val mass: Double,
    val name: String,
    @SerializedName("onboard_crew")
    val onboardCrew: Int,
    val orbit: String,
    val owners: List<Owner>,
    val status: StatusX,
    val type: Type,
    val url: String,
    val volume: Int,
    val width: Double
) : Parcelable