package org.odk.collect.forms.instances

interface InstancesRepositoryProvider {
    fun get(projectId: String? = null): InstancesRepository
}
