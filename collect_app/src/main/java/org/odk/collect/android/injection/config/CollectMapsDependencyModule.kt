package org.odk.collect.android.injection.config

import org.odk.collect.maps.MapsDependencyModule
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.SettingsProvider

class CollectMapsDependencyModule(
    private val referenceLayerRepository: ReferenceLayerRepository,
    private val settingsProvider: SettingsProvider
) : MapsDependencyModule() {
    override fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        return referenceLayerRepository
    }

    override fun providesSettingsProvider(): SettingsProvider {
        return settingsProvider
    }
}
