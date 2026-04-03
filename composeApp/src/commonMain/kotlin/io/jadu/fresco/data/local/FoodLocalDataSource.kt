package io.jadu.fresco.data.local

import io.jadu.fresco.db.FrescoDatabase
import io.jadu.fresco.domain.food.NutritionInfo
import io.jadu.fresco.domain.food.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class FoodLocalDataSource(
    private val database: FrescoDatabase
) {
    private val queries get() = database.foodCacheQueries

    suspend fun getNutrition(label: String): NutritionInfo? = withContext(Dispatchers.IO) {
        queries.selectNutritionByLabel(label).executeAsOneOrNull()?.let { row ->
            NutritionInfo(
                productName = row.productName,
                imageUrl = row.imageUrl,
                energyKcal = row.energyKcal,
                proteins = row.proteins,
                carbohydrates = row.carbohydrates,
                fat = row.fat,
                fiber = row.fiber,
                sugars = row.sugars
            )
        }
    }

    suspend fun getRecipes(label: String): List<Recipe>? = withContext(Dispatchers.IO) {
        val rows = queries.selectRecipesByLabel(label).executeAsList()
        rows.ifEmpty { null }?.map { row ->
            Recipe(id = row.id, name = row.name, thumbnailUrl = row.thumbnailUrl)
        }
    }

    suspend fun saveNutrition(label: String, nutrition: NutritionInfo) = withContext(Dispatchers.IO) {
        queries.insertNutrition(
            label = label,
            productName = nutrition.productName,
            imageUrl = nutrition.imageUrl,
            energyKcal = nutrition.energyKcal,
            proteins = nutrition.proteins,
            carbohydrates = nutrition.carbohydrates,
            fat = nutrition.fat,
            fiber = nutrition.fiber,
            sugars = nutrition.sugars
        )
    }

    suspend fun saveRecipes(label: String, recipes: List<Recipe>) = withContext(Dispatchers.IO) {
        queries.transaction {
            queries.deleteRecipesByLabel(label)
            recipes.forEach { recipe ->
                queries.insertRecipe(
                    id = recipe.id,
                    label = label,
                    name = recipe.name,
                    thumbnailUrl = recipe.thumbnailUrl
                )
            }
        }
    }
}
