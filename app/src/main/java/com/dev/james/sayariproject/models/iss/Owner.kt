package com.dev.james.sayariproject.models.iss

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Owner(
    val abbrev: String,
    val country_code: String,
    val description: String,
    val featured: Boolean,
    val founding_year: String,
    val id: Int,
    val image_url: String?,
    val launchers: String,
    val name: String,
    val spacecraft: String,
    val type: String,
    val url: String
) : Parcelable