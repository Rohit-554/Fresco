package io.jadu.fresco.di

import io.jadu.fresco.data.local.FoodLocalDataSource
import io.jadu.fresco.db.FrescoDatabase
import io.jadu.fresco.platform.database.DatabaseDriverFactory
import org.koin.dsl.module

val databaseModule = module {
    single { get<DatabaseDriverFactory>().create() }
    single { FrescoDatabase(get()) }
    single { FoodLocalDataSource(get()) }
}
