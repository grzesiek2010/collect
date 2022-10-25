package org.odk.collect.hilttest

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object HiltTestModule {
    @Provides
    fun provideHiltTestNameProvider(): HiltTestNameProvider {
        return HiltTestNameProvider()
    }
}
