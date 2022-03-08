package com.dev.james.sayariproject.models.iss

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Astronaut(
    val agency: Agency,
    val id: Int,
    val name: String,
    val profile_image: String,
    val profile_image_thumbnail: String,
    val status: Status,
    val url: String
):Parcelable