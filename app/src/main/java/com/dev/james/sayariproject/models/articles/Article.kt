package com.dev.james.sayariproject.models.articles

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.annotations.SerializedName

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


