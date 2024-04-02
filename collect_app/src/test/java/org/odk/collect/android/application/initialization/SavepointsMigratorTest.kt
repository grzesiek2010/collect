package org.odk.collect.android.application.initialization

import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.utilities.DatabaseInstancesRepositoryProvider
import org.odk.collect.formstest.InMemInstancesRepositoryProvider
import org.odk.collect.projects.InMemProjectsRepository

class SavepointsMigratorTest {
    private val projectsRepository = InMemProjectsRepository()
    private val instancesRepositoryProvider = InMemInstancesRepositoryProvider()
    private val
}