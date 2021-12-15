package com.dev.james.sayariproject.models

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

data class Article(
    val id : Int,
    val title : String,
    val url : String,
    val imageUrl : String,
    @SerializedName("newsSite")
    val site : String,
    @SerializedName("publishedAt")
    val date : String,
    val featured : Boolean,
    val summary : String,
    val events : List<ArticleEvents>
){

    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFormat = ZonedDateTime.parse(date)

    @RequiresApi(Build.VERSION_CODES.O)
    val createdDateFormatted = dateFormat.withZoneSameInstant(ZoneId.of("Africa/Nairobi"))
}


