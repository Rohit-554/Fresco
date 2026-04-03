package io.jadu.fresco.platform.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.jadu.fresco.db.FrescoDatabase

class AndroidDatabaseDriverFactory(
    private val context: Context
) : DatabaseDriverFactory {
    override fun create(): SqlDriver =
        AndroidSqliteDriver(FrescoDatabase.Schema, context, "fresco.db")
}
