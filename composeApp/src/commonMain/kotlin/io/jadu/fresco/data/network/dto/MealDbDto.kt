package io.jadu.fresco.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MealDbFilterResponse(
    val meals: List<MealDbMeal>? = null
)

@Serializable
data class MealDbMeal(
    @SerialName("strMeal") val name: String = "",
    @SerialName("strMealThumb") val thumbnailUrl: String = "",
    @SerialName("idMeal") val id: String = ""
)
