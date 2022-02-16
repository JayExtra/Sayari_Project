package com.dev.james.sayariproject.models.iss

data class DockingLocation(
    val docked: DockedVehicle?,
    val id: Int,
    val name: String
)