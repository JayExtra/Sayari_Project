package com.dev.james.sayariproject.models.events

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EventResponse(
   val count : Int,
   val next : String?,
   val previous : String?,
   val results : List<Events>
) : Parcelable