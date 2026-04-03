package io.jadu.fresco.di

import io.jadu.fresco.data.network.FoodApiService
import io.jadu.fresco.data.network.FoodApiServiceImpl
import io.jadu.fresco.data.network.createHttpClient
import io.jadu.fresco.data.repository.FoodRepositoryImpl
import io.jadu.fresco.domain.food.FetchFoodDetailsUseCase
import io.jadu.fresco.domain.food.FoodRepository
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val networkModule = module {
    single { createHttpClient() }
    single<FoodApiService> { FoodApiServiceImpl(get()) }
    single<FoodRepository> { FoodRepositoryImpl(get()) }
    factoryOf(::FetchFoodDetailsUseCase)
}
