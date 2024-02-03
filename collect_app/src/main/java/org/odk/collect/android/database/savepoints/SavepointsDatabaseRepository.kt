package org.odk.collect.android.database.savepoints

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import android.os.StrictMode
import org.odk.collect.android.database.DatabaseConnection
import org.odk.collect.android.database.DatabaseConstants
import org.odk.collect.android.database.DatabaseConstants.SAVEPOINTS_DATABASE_NAME
import org.odk.collect.android.database.DatabaseConstants.SAVEPOINTS_DATABASE_VERSION
import org.odk.collect.android.database.savepoints.SavepointDatabaseObjectMapper.getSavepointFromCurrentCursorPosition
import org.odk.collect.android.database.savepoints.SavepointDatabaseObjectMapper.getValuesFromSavepoint
import org.odk.collect.android.database.savepoints.SavepointsDatabaseColumns.FORM_DB_ID
import org.odk.collect.android.database.savepoints.SavepointsDatabaseColumns.INSTANCE_DB_ID

class SavepointsDatabaseRepository(
    context: Context,
    dbPath: String,
    private val cachePath: String,
    private val instancesPath: String
) : SavepointsRepository {
    private val databaseConnection: DatabaseConnection = DatabaseConnection(
        context,
        dbPath,
        SAVEPOINTS_DATABASE_NAME,
        SavepointsDatabaseMigrator(),
        SAVEPOINTS_DATABASE_VERSION
    )

    override fun get(formDbId: Long, instanceDbId: Long?): Savepoint? {
        StrictMode.noteSlowCall("Accessing readable DB")

        val cursor = queryAndReturnCursor(
            selection = "$FORM_DB_ID=? AND $INSTANCE_DB_ID=?",
            selectionArgs = arrayOf(formDbId.toString(), instanceDbId?.toString())
        )
        val savepoints = getSavepointsFromCursor(cursor)

        return if (savepoints.isNotEmpty()) savepoints[0] else null
    }

    override fun save(savepoint: Savepoint): Long {
        val values = getValuesFromSavepoint(savepoint, cachePath, instancesPath)

        return databaseConnection
            .writeableDatabase
            .insertWithOnConflict(DatabaseConstants.SAVEPOINTS_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    override fun delete(formDbId: Long, instanceDbId: Long?) {
        val selection = "$FORM_DB_ID=? AND $INSTANCE_DB_ID=?"
        val selectionArgs = arrayOf(formDbId.toString(), instanceDbId?.toString())

        databaseConnection
            .writeableDatabase
            .delete(DatabaseConstants.SAVEPOINTS_TABLE_NAME, selection, selectionArgs)
    }

    override fun deleteAll() {
        databaseConnection
            .writeableDatabase
            .delete(DatabaseConstants.SAVEPOINTS_TABLE_NAME, null, null)
    }

    private fun queryAndReturnCursor(
        projectionMap: Map<String, String>? = null,
        projection: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String?>? = null,
        groupBy: String? = null,
        having: String? = null,
        sortOrder: String? = null
    ): Cursor {
        val readableDatabase = databaseConnection.readableDatabase
        val qb = SQLiteQueryBuilder().apply {
            tables = DatabaseConstants.SAVEPOINTS_TABLE_NAME
            if (projectionMap != null) {
                this.projectionMap = projectionMap
            }
        }
        return qb.query(readableDatabase, projection, selection, selectionArgs, groupBy, having, sortOrder)
    }

    private fun getSavepointsFromCursor(cursor: Cursor?): List<Savepoint> {
        val savepoints: MutableList<Savepoint> = ArrayList()
        if (cursor != null) {
            cursor.moveToPosition(-1)
            while (cursor.moveToNext()) {
                val savepoint = getSavepointFromCurrentCursorPosition(cursor, cachePath, instancesPath)
                savepoints.add(savepoint)
            }
        }
        return savepoints
    }
}

interface SavepointsRepository {
    fun get(formDbId: Long, instanceDbId: Long?): Savepoint?

    fun save(savepoint: Savepoint): Long

    fun delete(formDbId: Long, instanceDbId: Long?)

    fun deleteAll()
}
