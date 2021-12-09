package com.dev.james.sayariproject.models

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
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
    val launches : List<ArticlesLaunches>,
    val events : List<ArticleEvents>
){
    private val dateFormat : SimpleDateFormat
        get() =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

    private val newDate : Date get() = dateFormat.parse(date)
    val createdDateFormatted : String
        get() = dateFormat.format(newDate)
}


