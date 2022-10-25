package org.odk.collect.audiorecorder.recording

import android.app.Application
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ServiceComponent
import org.odk.collect.audiorecorder.recording.internal.ForegroundServiceAudioRecorder
import org.odk.collect.audiorecorder.recording.internal.RecordingRepository

open class AudioRecorderFactory(private val application: Application) {

    @EntryPoint
    @InstallIn(ServiceComponent::class)
    interface AudioRecorderFactoryEntryPoint {
        fun providesRecordingRepository(): RecordingRepository
    }

    open fun create(): AudioRecorder {
        val daggerHiltModule =
            EntryPointAccessors.fromApplication(application.applicationContext, AudioRecorderFactoryEntryPoint::class.java)

        val recordingRepository = daggerHiltModule.providesRecordingRepository()

        return ForegroundServiceAudioRecorder(application, recordingRepository)
    }
}
