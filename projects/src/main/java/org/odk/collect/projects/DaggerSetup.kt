package org.odk.collect.projects

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.odk.collect.androidshared.SoftKeyboardController
import org.odk.collect.shared.UUIDGenerator
import javax.inject.Singleton

interface ProjectsDependencyComponentProvider {
    val projectsDependencyComponent: ProjectsDependencyComponent
}

@Component(modules = [ProjectsDependencyModule::class])
@Singleton
interface ProjectsDependencyComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun dependencyModule(projectsDependencyModule: ProjectsDependencyModule): Builder

        fun build(): ProjectsDependencyComponent
    }

    fun inject(addProjectDialog: AddProjectDialog)
}

@Module
open class ProjectsDependencyModule {

    @Provides
    open fun providesProjectsRepository(): ProjectsRepository {
        return InMemProjectsRepository(UUIDGenerator())
    }

    @Provides
    open fun provideSoftKeyboardController(application: Application): SoftKeyboardController {
        return SoftKeyboardController(application)
    }
}
