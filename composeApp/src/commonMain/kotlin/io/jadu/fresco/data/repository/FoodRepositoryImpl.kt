package io.jadu.fresco.data.repository

import io.jadu.fresco.data.network.FoodApiService
import io.jadu.fresco.domain.food.FoodInfo
import io.jadu.fresco.domain.food.FoodRepository
import io.jadu.fresco.domain.food.NutritionInfo
import io.jadu.fresco.domain.food.Recipe
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class FoodRepositoryImpl(
    private val foodApiService: FoodApiService
) : FoodRepository {

    override suspend fun getFoodDetails(label: String): FoodInfo = coroutineScope {
        val nutritionDeferred = async { fetchNutrition(label) }
        val recipesDeferred = async { fetchRecipes(label) }

        FoodInfo(
            label = label,
            nutrition = nutritionDeferred.await(),
            recipes = recipesDeferred.await()
        )
    }

    private suspend fun fetchNutrition(label: String): NutritionInfo? {
        val response = foodApiService.searchNutrition(label)
        val product = response.products.firstOrNull() ?: return null
        val n = product.nutriments
        return NutritionInfo(
            productName = product.productName,
            imageUrl = product.imageUrl,
            energyKcal = n.energyKcal,
            proteins = n.proteins,
            carbohydrates = n.carbohydrates,
            fat = n.fat,
            fiber = n.fiber,
            sugars = n.sugars
        )
    }

    private suspend fun fetchRecipes(label: String): List<Recipe> {
        val response = foodApiService.searchRecipes(label)
        return response.meals?.map { meal ->
            Recipe(
                id = meal.id,
                name = meal.name,
                thumbnailUrl = meal.thumbnailUrl
            )
        } ?: emptyList()
    }
}
