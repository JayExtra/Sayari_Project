package com.dev.james.sayariproject.models.favourites

data class AgencyResponse(
    val count: Int,
    val next: Any,
    val previous: Any,
    val results: List<Result>
)