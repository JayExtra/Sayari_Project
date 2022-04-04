package com.dev.james.sayariproject.models.favourites

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourite_agencies")
data class Result(
    val abbrev: String,
    val administrator: String,
    val country_code: String,
    val description: String,
    val featured: Boolean,
    val founding_year: String,
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val image_url: String,
    val launchers: String,
    val name: String,
    val spacecraft: String,
    val type: String,
    val url: String
)