package org.odk.collect.projects

import dagger.Component
import dagger.Module
import dagger.Provides
import org.odk.collect.shared.UUIDGenerator
import javax.inject.Singleton

interface ProjectsDependencyComponentProvider {
    val projectsDependencyComponent: ProjectsDependencyComponent
}

@Component(modules = [ProjectsDependencyModule::class])
@Singleton
interface ProjectsDependencyComponent {

    fun inject(manualProjectCreatorDialog: ManualProjectCreatorDialog)
}

@Module
open class ProjectsDependencyModule {

    @Provides
    open fun providesProjectsRepository(): ProjectsRepository {
        return InMemProjectsRepository(UUIDGenerator())
    }
}
