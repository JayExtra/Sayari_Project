package com.dev.james.sayariproject.models.astronaut

import com.dev.james.sayariproject.models.launch.LaunchList


data class Astronaut(
    val agency: Agency,
    val bio: String,
    val date_of_birth: String,
    val date_of_death: String?,
    val first_flight: String,
    val id: Int,
    val instagram: String?,
    val last_flight: String,
    val name: String,
    val nationality: String,
    val profile_image: String,
    val profile_image_thumbnail: String,
    val status: Status,
    val twitter: String?,
    val type: Type,
    val url: String,
    val wiki: String,
    val flights : List<LaunchList>,
    val landings : List<Landings>
)