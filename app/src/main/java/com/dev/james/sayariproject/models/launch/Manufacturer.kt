package com.dev.james.sayariproject.models.launch

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Manufacturer(
    val id : Int,
    val name : String,
    val type  :String,
    @SerializedName("country_code")
    val country : String
): Parcelable
