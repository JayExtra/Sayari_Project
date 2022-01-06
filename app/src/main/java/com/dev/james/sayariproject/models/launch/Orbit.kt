package com.dev.james.sayariproject.models.launch

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Orbit(
    val id : Int,
    val name : String,
    val abbrev : String
): Parcelable
