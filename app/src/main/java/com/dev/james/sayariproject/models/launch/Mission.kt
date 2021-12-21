package com.dev.james.sayariproject.models.launch

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Mission(
    val id : Int,
    val name : String,
    val description : String?,
    @SerializedName("launch_designator")
    val designator : String?,
    val orbit : Orbit?
): Parcelable
