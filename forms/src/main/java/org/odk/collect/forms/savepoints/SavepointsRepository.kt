package org.odk.collect.forms.savepoints

interface SavepointsRepository {
    fun get(formDbId: Long, instanceDbId: Long?): Savepoint?

    fun save(savepoint: Savepoint)

    fun delete(savepoint: Savepoint?)
}
