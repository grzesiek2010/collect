package org.odk.collect.db.sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.os.StrictMode
import timber.log.Timber
import java.io.File

/**
 * Allows access to a database file. The actual underlying connection (an instance of
 * [SQLiteOpenHelper] to this database will be reused for different instances of
 * [DatabaseConnection] that refer to the same file.
 *
 * @param migrator used to migrate or create the database automatically before access
 */
open class DatabaseConnection @JvmOverloads constructor(
    private val context: Context,
    private val path: String,
    private val name: String,
    private val migrator: DatabaseMigrator,
    private val databaseVersion: Int,
    private val strict: Boolean = false
) {

    private val databasePath = path + File.separator + name

    val writeableDatabase: SQLiteDatabase
        get() {
            StrictMode.noteSlowCall("Accessing writable DB")
            return dbHelper.writableDatabase
        }

    val readableDatabase: SQLiteDatabase
        get() {
            if (strict) {
                StrictMode.noteSlowCall("Accessing readable DB")
            }
            return dbHelper.readableDatabase
        }

    private val dbHelper: SQLiteOpenHelper
        get() {
            return synchronized(openHelpers) {
                if (openHelpers.containsKey(databasePath) && !File(databasePath).exists()) {
                    /**
                     * Ideally we should close the database here as well but it was causing crashes in
                     * our tests as DB connections seem to be getting used after being closed. These
                     * "removed" helpers will be closed in [closeAll] rather than when they are
                     * replaced.
                     */
                    openHelpers.remove(databasePath)?.let {
                        toClose.add(it)
                    }
                }

                openHelpers.getOrPut(databasePath) {
                    DatabaseMigratorSQLiteOpenHelper(
                        AltDatabasePathContext(path, context),
                        name,
                        null,
                        databaseVersion,
                        migrator
                    )
                }
            }
        }

    fun reset() {
        openHelpers.remove(databasePath)?.close()
    }

    fun <T> withSynchronizedConnection(block: DatabaseConnection.() -> T): T {
        return synchronized(dbHelper) {
            block(this)
        }
    }

    companion object {

        private val openHelpers = mutableMapOf<String, SQLiteOpenHelper>()
        private val toClose = mutableListOf<SQLiteOpenHelper>()

        @JvmStatic
        fun cleanUp() {
            val openHelpersToClear = mutableListOf<String>()

            openHelpers.forEach { (databasePath, openHelper) ->
                if (!File(databasePath).exists()) {
                    openHelper.close()
                    openHelpersToClear.add(databasePath)
                }
            }

            openHelpersToClear.forEach {
                openHelpers.remove(it)
            }
        }

        @JvmStatic
        fun closeAll() {
            openHelpers.forEach { (_, openHelper) -> openHelper.close() }
            openHelpers.clear()

            toClose.forEach(SQLiteOpenHelper::close)
            toClose.clear()
        }
    }
}

/**
 * [SQLiteOpenHelper] that delegates `onCreate`, `onUpdate` to a [DatabaseMigrator].
 */
private class DatabaseMigratorSQLiteOpenHelper(
    context: Context,
    name: String,
    cursorFactory: CursorFactory?,
    version: Int,
    private val databaseMigrator: DatabaseMigrator
) : SQLiteOpenHelper(context, name, cursorFactory, version) {

    override fun onCreate(db: SQLiteDatabase?) {
        databaseMigrator.onCreate(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Timber.i("Upgrading database from version %d to %d", oldVersion, newVersion)
        databaseMigrator.onUpgrade(db, oldVersion)
        Timber.i(
            "Upgrading database from version %d to %d completed with success.",
            oldVersion,
            newVersion
        )
    }
}
