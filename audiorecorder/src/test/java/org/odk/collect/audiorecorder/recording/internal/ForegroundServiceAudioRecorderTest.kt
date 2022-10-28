package org.odk.collect.audiorecorder.recording.internal

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidshared.data.Consumable
import org.odk.collect.async.Scheduler
import org.odk.collect.audiorecorder.AudioRecorderDependencyModule
import org.odk.collect.audiorecorder.recorder.Output
import org.odk.collect.audiorecorder.recorder.Recorder
import org.odk.collect.audiorecorder.recording.AudioRecorder
import org.odk.collect.audiorecorder.recording.AudioRecorderFactory
import org.odk.collect.audiorecorder.recording.AudioRecorderTest
import org.odk.collect.audiorecorder.recording.MicInUseException
import org.odk.collect.audiorecorder.support.FakeRecorder
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.RobolectricHelpers
import java.io.File
import org.junit.Before
import org.junit.rules.RuleChain
import org.odk.collect.androidshared.data.AppState
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@UninstallModules(AudioRecorderDependencyModule::class)
@Config(application = HiltTestApplication::class)
@HiltAndroidTest
class ForegroundServiceAudioRecorderTest : AudioRecorderTest() {

    @get:Rule
    var rule: RuleChain = RuleChain
        .outerRule(HiltAndroidRule(this))
        .around(InstantTaskExecutorRule())

    private val application by lazy { getApplicationContext<Application>() }

    override val viewModel: AudioRecorder by lazy {
        AudioRecorderFactory(application).create()
    }

    override fun runBackground() {
        RobolectricHelpers.runServices(true)
    }

    override fun getLastRecordedFile(): File? {
        return fakeRecorder.file
    }

    @Before
    fun setup() {
        fakeRecorder = FakeRecorder()
        scheduler = FakeScheduler()
    }

    @After
    fun clearServices() {
        RobolectricHelpers.clearServices()
    }

    @Test
    fun start_passesOutputToRecorder() {
        Output.values().forEach {
            viewModel.start("blah", it)
            viewModel.stop()
            runBackground()
            assertThat(fakeRecorder.output, equalTo(it))
        }
    }

    @Test
    fun start_incrementsDurationEverySecond() {
        viewModel.start("blah", Output.AAC)
        runBackground()

        val currentSession = viewModel.getCurrentSession()
        scheduler.runForeground(0)
        assertThat(currentSession.value?.duration, equalTo(0))

        scheduler.runForeground(500)
        assertThat(currentSession.value?.duration, equalTo(0))

        scheduler.runForeground(1000)
        assertThat(currentSession.value?.duration, equalTo(1000))
    }

    @Test
    fun start_updatesAmplitude() {
        viewModel.start("blah", Output.AAC)
        runBackground()

        val currentSession = viewModel.getCurrentSession()

        fakeRecorder.amplitude = 12
        scheduler.runForeground()
        assertThat(currentSession.value?.amplitude, equalTo(12))

        fakeRecorder.amplitude = 45
        scheduler.runForeground()
        assertThat(currentSession.value?.amplitude, equalTo(45))
    }

    @Test
    fun start_whenRecorderStartThrowsException_setsSessionToNull() {
        val exception = MicInUseException()
        fakeRecorder.failOnStart(exception)

        viewModel.start("blah", Output.AAC)
        runBackground()
        assertThat(viewModel.getCurrentSession().value, equalTo(null))
    }

    @Test
    fun start_whenRecorderStartThrowsException_setsFailedToStart() {
        val exception = MicInUseException()
        fakeRecorder.failOnStart(exception)

        viewModel.start("blah", Output.AAC)
        runBackground()
        assertThat(viewModel.failedToStart().value, equalTo(Consumable(exception)))
    }

    @Test
    fun start_whenRecorderStartThrowsException_thenSucceeds_setsFailedToStartToNull() {
        val exception = MicInUseException()
        fakeRecorder.failOnStart(exception)
        viewModel.start("blah", Output.AAC)
        runBackground()

        fakeRecorder.failOnStart(null)
        viewModel.start("blah", Output.AAC)
        runBackground()

        assertThat(viewModel.failedToStart().value, equalTo(Consumable(null)))
    }

    companion object {
        private var fakeRecorder = FakeRecorder()
        private var scheduler = FakeScheduler()
        private var recordingRepository = RecordingRepository(AppState())
    }

    @Module
    @InstallIn(ServiceComponent::class)
    object TestModule {
        @Provides
        fun providesCacheDir(@ApplicationContext application: Context): File {
            val externalFilesDir = application.getExternalFilesDir(null)
            return File(externalFilesDir, "recordings").also { it.mkdirs() }
        }

        @Provides
        fun providesRecorder(): Recorder {
            return fakeRecorder
        }

        @Provides
        fun providesScheduler(): Scheduler {
            return scheduler
        }

        @Provides
        internal fun providesRecordingRepository(): RecordingRepository {
            return recordingRepository
        }
    }
}
