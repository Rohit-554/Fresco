package io.jadu.fresco.domain.food

/**
 * Retrieves enriched food information (nutrition + recipes) for a given label.
 */
interface FoodRepository {
    suspend fun getFoodDetails(label: String): FoodInfo
}
