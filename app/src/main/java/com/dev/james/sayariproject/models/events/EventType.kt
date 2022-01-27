package com.dev.james.sayariproject.models.events

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EventType(
    val id : Int,
    val name : String
):Parcelable