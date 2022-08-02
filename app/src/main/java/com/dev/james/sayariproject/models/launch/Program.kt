package com.dev.james.sayariproject.models.launch

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import com.dev.james.sayariproject.models.iss.Agency

@Parcelize
data class Program(
    @SerializedName("id")
    val id : Int,
    @SerializedName("url")
    val url : String,
    @SerializedName("name")
    val name : String,
    @SerializedName("description")
    val description : String,
    @SerializedName("agencies")
    val agencies : List<Agency>,
    @SerializedName("image_url")
    val imageUrl : String,
    @SerializedName("start_date")
    val startDate : String?,
    @SerializedName("wiki_url")
    val wikiUrl : String,
    @SerializedName("info_url")
    val infoUrl : String?,
) : Parcelable
