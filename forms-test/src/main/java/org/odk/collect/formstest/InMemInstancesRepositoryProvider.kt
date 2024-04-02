package org.odk.collect.formstest

import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.forms.instances.InstancesRepositoryProvider

class InMemInstancesRepositoryProvider : InstancesRepositoryProvider {
    private val repositories = mutableMapOf<String?, InstancesRepository>()

    override fun get(projectId: String?): InstancesRepository {
        return repositories.getOrPut("$projectId") { InMemInstancesRepository() }
    }
}
