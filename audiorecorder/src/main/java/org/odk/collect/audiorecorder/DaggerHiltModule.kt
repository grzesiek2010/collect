package org.odk.collect.audiorecorder

import android.app.Application
import android.media.MediaRecorder
import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import kotlinx.coroutines.Dispatchers
import org.odk.collect.androidshared.data.getState
import org.odk.collect.async.CoroutineScheduler
import org.odk.collect.async.Scheduler
import org.odk.collect.audiorecorder.mediarecorder.AACRecordingResource
import org.odk.collect.audiorecorder.mediarecorder.AMRRecordingResource
import org.odk.collect.audiorecorder.recorder.Output
import org.odk.collect.audiorecorder.recorder.Recorder
import org.odk.collect.audiorecorder.recorder.RecordingResourceRecorder
import org.odk.collect.audiorecorder.recording.internal.RecordingRepository

@Module
@InstallIn(ServiceComponent::class)
object DaggerHiltModule {

    @Provides
    fun providesCacheDir(@ApplicationContext application: Application): File {
        val externalFilesDir = application.getExternalFilesDir(null)
        return File(externalFilesDir, "recordings").also { it.mkdirs() }
    }

    @Provides
    fun providesRecorder(cacheDir: File): Recorder {
        return RecordingResourceRecorder(cacheDir) { output ->
            when (output) {
                Output.AMR -> {
                    AMRRecordingResource(MediaRecorder(), Build.VERSION.SDK_INT)
                }

                Output.AAC -> {
                    AACRecordingResource(MediaRecorder(), Build.VERSION.SDK_INT, 64)
                }

                Output.AAC_LOW -> {
                    AACRecordingResource(MediaRecorder(), Build.VERSION.SDK_INT, 24)
                }
            }
        }
    }

    @Provides
    fun providesScheduler(): Scheduler {
        return CoroutineScheduler(Dispatchers.Main, Dispatchers.IO)
    }

    @Provides
    fun providesRecordingRepository(@ApplicationContext application: Application): RecordingRepository {
        return RecordingRepository(application.getState())
    }
}
