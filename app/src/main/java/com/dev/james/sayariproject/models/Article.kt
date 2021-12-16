package com.dev.james.sayariproject.models

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
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
)


