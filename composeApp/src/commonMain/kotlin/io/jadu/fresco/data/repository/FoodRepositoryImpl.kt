package io.jadu.fresco.data.repository

import io.jadu.fresco.data.local.FoodLocalDataSource
import io.jadu.fresco.data.network.FoodApiService
import io.jadu.fresco.domain.food.FoodInfo
import io.jadu.fresco.domain.food.FoodRepository
import io.jadu.fresco.domain.food.NutritionInfo
import io.jadu.fresco.domain.food.Recipe
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class FoodRepositoryImpl(
    private val foodApiService: FoodApiService,
    private val localDataSource: FoodLocalDataSource
) : FoodRepository {

    override suspend fun getFoodDetails(label: String): FoodInfo = coroutineScope {
        val nutritionDeferred = async { getCachedOrFetchNutrition(label) }
        val recipesDeferred = async { getCachedOrFetchRecipes(label) }

        FoodInfo(
            label = label,
            nutrition = nutritionDeferred.await(),
            recipes = recipesDeferred.await()
        )
    }

    private suspend fun getCachedOrFetchNutrition(label: String): NutritionInfo? {
        localDataSource.getNutrition(label)?.let { return it }

        val response = foodApiService.searchNutrition(label)
        val product = response.products.firstOrNull() ?: return null
        val n = product.nutriments
        val nutrition = NutritionInfo(
            productName = product.productName,
            imageUrl = product.imageUrl,
            energyKcal = n.energyKcal,
            proteins = n.proteins,
            carbohydrates = n.carbohydrates,
            fat = n.fat,
            fiber = n.fiber,
            sugars = n.sugars
        )
        localDataSource.saveNutrition(label, nutrition)
        return nutrition
    }

    private suspend fun getCachedOrFetchRecipes(label: String): List<Recipe> {
        localDataSource.getRecipes(label)?.let { return it }

        val response = foodApiService.searchRecipes(label)
        val recipes = response.meals?.map { meal ->
            Recipe(id = meal.id, name = meal.name, thumbnailUrl = meal.thumbnailUrl)
        } ?: emptyList()
        if (recipes.isNotEmpty()) {
            localDataSource.saveRecipes(label, recipes)
        }
        return recipes
    }
}
