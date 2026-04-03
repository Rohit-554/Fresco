package io.jadu.fresco.data.network

import io.jadu.fresco.data.network.dto.MealDbFilterResponse
import io.jadu.fresco.data.network.dto.OpenFoodFactsSearchResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Fetches food data from Open Food Facts and TheMealDB.
 */
interface FoodApiService {
    suspend fun searchNutrition(query: String): OpenFoodFactsSearchResponse
    suspend fun searchRecipes(ingredient: String): MealDbFilterResponse
}

class FoodApiServiceImpl(
    private val httpClient: HttpClient
) : FoodApiService {

    override suspend fun searchNutrition(query: String): OpenFoodFactsSearchResponse =
        httpClient.get("https://world.openfoodfacts.org/cgi/search.pl") {
            parameter("search_terms", query)
            parameter("search_simple", 1)
            parameter("action", "process")
            parameter("json", 1)
            parameter("page_size", 1)
            parameter("fields", "product_name,image_url,nutriments")
        }.body()

    override suspend fun searchRecipes(ingredient: String): MealDbFilterResponse =
        httpClient.get("https://www.themealdb.com/api/json/v1/1/filter.php") {
            parameter("i", ingredient)
        }.body()
}
