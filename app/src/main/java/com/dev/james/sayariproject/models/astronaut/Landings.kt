package com.dev.james.sayariproject.models.astronaut

data class Landings(
    val destination: String,
    val id: Int,
    val mission_end: String,
    val spacecraft: Spacecraft,
    val url: String
)