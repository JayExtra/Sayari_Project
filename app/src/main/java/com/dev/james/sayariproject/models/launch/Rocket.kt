package com.dev.james.sayariproject.models.launch

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Rocket(
    val id : Int,
    val configuration : RocketConfiguration?
):Parcelable
