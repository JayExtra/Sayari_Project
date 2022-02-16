package com.dev.james.sayariproject.models.iss

data class Owner(
    val abbrev: String,
    val administrator: Any,
    val country_code: String,
    val description: String,
    val featured: Boolean,
    val founding_year: String,
    val id: Int,
    val image_url: Any,
    val launchers: String,
    val name: String,
    val parent: Any,
    val spacecraft: String,
    val type: String,
    val url: String
)