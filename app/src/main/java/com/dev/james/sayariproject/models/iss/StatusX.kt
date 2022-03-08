package com.dev.james.sayariproject.models.iss

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StatusX(
    val id: Int,
    val name: String
) : Parcelable