package org.odk.collect.maps

import dagger.Component
import dagger.Module
import dagger.Provides
import org.odk.collect.maps.layers.OfflineMapLayersPicker
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.SettingsProvider
import javax.inject.Singleton

interface MapsDependencyComponentProvider {
    val mapsDependencyComponent: MapsDependencyComponent
}

@Component(modules = [MapsDependencyModule::class])
@Singleton
interface MapsDependencyComponent {
    fun inject(offlineMapLayersPicker: OfflineMapLayersPicker)
}

@Module
open class MapsDependencyModule {

    @Provides
    open fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesSettingsProvider(): SettingsProvider {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }
}
