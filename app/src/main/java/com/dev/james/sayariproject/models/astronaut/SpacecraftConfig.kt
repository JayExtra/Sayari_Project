package com.dev.james.sayariproject.models.astronaut

data class SpacecraftConfig(
    val agency: Agency,
    val id: Int,
    val image_url: String,
    val in_use: Boolean,
    val name: String,
    val type: Type,
    val url: String
)