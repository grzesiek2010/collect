package org.odk.collect.android.support

import android.app.Application
import android.webkit.MimeTypeMap
import androidx.work.WorkManager
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.openrosa.OpenRosaHttpInterface
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.android.views.BarcodeViewDecoder
import org.odk.collect.async.Scheduler
import org.odk.collect.utilities.UserAgentProvider

open class TestDependencies : AppDependencyModule() {
    val server = StubOpenRosaServer()
    val scheduler = TestScheduler()
    val storagePathProvider = StoragePathProvider()
    val stubBarcodeViewDecoder = StubBarcodeViewDecoder()
    override fun provideHttpInterface(mimeTypeMap: MimeTypeMap, userAgentProvider: UserAgentProvider, application: Application, versionInformation: VersionInformation): OpenRosaHttpInterface {
        return server
    }

    override fun providesScheduler(workManager: WorkManager): Scheduler {
        return scheduler
    }

    override fun providesBarcodeViewDecoder(): BarcodeViewDecoder {
        return stubBarcodeViewDecoder
    }
}
