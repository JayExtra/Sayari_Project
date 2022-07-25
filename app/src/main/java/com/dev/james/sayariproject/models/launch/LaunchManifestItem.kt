package com.dev.james.sayariproject.models.launch

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "launch_manifest_table")
data class LaunchManifestItem(
    @PrimaryKey val id : String,
    val slug : String,
    val name : String,
    val window_start : String,
)
