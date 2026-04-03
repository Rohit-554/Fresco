package io.jadu.fresco.platform.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.jadu.fresco.db.FrescoDatabase

class IosDatabaseDriverFactory : DatabaseDriverFactory {
    override fun create(): SqlDriver =
        NativeSqliteDriver(FrescoDatabase.Schema, "fresco.db")
}
