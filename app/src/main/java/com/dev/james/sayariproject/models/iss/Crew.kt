package com.dev.james.sayariproject.models.iss

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Crew(
    val astronaut: Astronaut,
    val id: Int,
    val role: Role
):Parcelable