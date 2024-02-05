package org.odk.collect.formstest

import org.odk.collect.forms.savepoints.Savepoint
import org.odk.collect.forms.savepoints.SavepointsRepository

class InMemSavepointsRepository : SavepointsRepository {
    private val savepoints = mutableListOf<Savepoint>()

    override fun get(formDbId: Long, instanceDbId: Long?): Savepoint? {
        return savepoints.find { savepoint -> savepoint.formDbId == formDbId && savepoint.instanceDbId == instanceDbId }
    }

    override fun save(savepoint: Savepoint) {
        savepoints.add(savepoint)
        savepoints.indexOf(savepoint).toLong()
    }

    override fun delete(savepoint: Savepoint?) {
        savepoints.remove(savepoint)
    }
}
