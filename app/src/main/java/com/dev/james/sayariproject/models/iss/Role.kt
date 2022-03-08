package com.dev.james.sayariproject.models.iss

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Role(
    val id: Int,
    val priority: Int,
    val role: String
) : Parcelable