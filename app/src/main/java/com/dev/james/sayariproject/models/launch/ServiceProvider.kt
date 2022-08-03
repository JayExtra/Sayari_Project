package com.dev.james.sayariproject.models.launch

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServiceProvider(
    @SerializedName("id")
    val id : Int,
    @SerializedName("url")
    val url : String,
    @SerializedName("name")
    val name : String,
    @SerializedName("type")
    val type : String?
):Parcelable
