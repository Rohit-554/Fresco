package io.jadu.fresco.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenFoodFactsSearchResponse(
    val count: Int = 0,
    val products: List<OpenFoodFactsProduct> = emptyList()
)

@Serializable
data class OpenFoodFactsProduct(
    @SerialName("product_name") val productName: String = "",
    @SerialName("image_url") val imageUrl: String = "",
    val nutriments: OpenFoodFactsNutriments = OpenFoodFactsNutriments()
)

@Serializable
data class OpenFoodFactsNutriments(
    @SerialName("energy-kcal_100g") val energyKcal: Double = 0.0,
    @SerialName("proteins_100g") val proteins: Double = 0.0,
    @SerialName("carbohydrates_100g") val carbohydrates: Double = 0.0,
    @SerialName("fat_100g") val fat: Double = 0.0,
    @SerialName("fiber_100g") val fiber: Double = 0.0,
    @SerialName("sugars_100g") val sugars: Double = 0.0
)
