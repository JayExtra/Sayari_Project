package com.dev.james.sayariproject.models.iss

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Agency(
    val id: Int,
    val url: String,
    val name: String,
    val type: String
):Parcelable