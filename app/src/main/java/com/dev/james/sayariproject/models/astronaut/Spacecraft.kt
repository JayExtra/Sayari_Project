package com.dev.james.sayariproject.models.astronaut

data class Spacecraft(
    val description: String,
    val id: Int,
    val name: String,
    val serial_number: String,
    val spacecraft_config: SpacecraftConfig,
    val status: Status,
    val url: String
)