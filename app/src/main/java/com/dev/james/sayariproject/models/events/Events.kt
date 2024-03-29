package com.dev.james.sayariproject.models.events

import android.os.Parcelable
import com.dev.james.sayariproject.models.launch.Program
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Events(
    val id : Int,
    val url : String,
    val slug : String,
    val name : String,
    val type : EventType ,
    val description : String,
    @SerializedName("webcast_live")
    val webcast : Boolean,
    val location : String?,
    @SerializedName("news_url")
    val newsUrl : String?,
    @SerializedName("video_url")
    val videoUrl : String?,
    @SerializedName("feature_image")
    val imageUrl : String,
    val date : String,
    val probability : Int?,
    val program : List<Program>
) : Parcelable