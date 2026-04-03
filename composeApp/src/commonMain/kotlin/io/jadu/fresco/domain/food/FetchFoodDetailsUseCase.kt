package io.jadu.fresco.domain.food

class FetchFoodDetailsUseCase(
    private val foodRepository: FoodRepository
) {
    suspend operator fun invoke(label: String): FoodInfo =
        foodRepository.getFoodDetails(label)
}
