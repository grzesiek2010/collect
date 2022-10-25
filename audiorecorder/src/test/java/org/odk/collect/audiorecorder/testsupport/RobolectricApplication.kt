package org.odk.collect.audiorecorder.testsupport

import android.app.Application
import org.odk.collect.androidshared.data.AppState
import org.odk.collect.androidshared.data.StateStore

/**
 * Used as the Application in tests in in the `test/src` root. This is setup in `robolectric.properties`
 */
internal class RobolectricApplication : Application(), StateStore {

    private val appState = AppState()

    override fun getState(): AppState {
        return appState
    }
}
