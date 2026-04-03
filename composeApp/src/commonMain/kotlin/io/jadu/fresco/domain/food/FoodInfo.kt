package io.jadu.fresco.domain.food

/**
 * Enriched information about a classified fruit or vegetable.
 *
 * Combines nutritional data from Open Food Facts with recipe suggestions
 * from TheMealDB, keyed by the classification label.
 */
data class FoodInfo(
    val label: String,
    val nutrition: NutritionInfo?,
    val recipes: List<Recipe>
)

data class NutritionInfo(
    val productName: String,
    val imageUrl: String,
    val energyKcal: Double,
    val proteins: Double,
    val carbohydrates: Double,
    val fat: Double,
    val fiber: Double,
    val sugars: Double
)

data class Recipe(
    val id: String,
    val name: String,
    val thumbnailUrl: String
)
