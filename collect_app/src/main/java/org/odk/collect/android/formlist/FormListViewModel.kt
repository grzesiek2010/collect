package org.odk.collect.android.formlist

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.formmanagement.FormsUpdater
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusAppState
import org.odk.collect.android.preferences.utilities.FormUpdateMode
import org.odk.collect.android.preferences.utilities.SettingsUtils
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsDirDiskFormsSynchronizer
import org.odk.collect.androidshared.data.Consumable
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.async.Scheduler
import org.odk.collect.forms.FormSourceException
import org.odk.collect.forms.FormSourceException.AuthRequired
import org.odk.collect.forms.FormsRepository
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings
import org.odk.collect.shared.strings.Md5.getMd5Hash
import java.io.ByteArrayInputStream

class FormListViewModel(
    private val formsRepository: FormsRepository,
    private val application: Application,
    private val syncRepository: SyncStatusAppState,
    private val formsUpdater: FormsUpdater,
    private val scheduler: Scheduler,
    private val generalSettings: Settings,
    private val analytics: Analytics,
    private val changeLockProvider: ChangeLockProvider,
    private val projectId: String
) : ViewModel() {

    private val _allForms: MutableNonNullLiveData<List<FormListItem>> = MutableNonNullLiveData(emptyList())
    private val _formsToDisplay: MutableLiveData<List<FormListItem>> = MutableLiveData()
    val formsToDisplay: LiveData<List<FormListItem>> = _formsToDisplay

    private val _syncResult: MutableLiveData<Consumable<String>> = MutableLiveData()
    val syncResult: LiveData<Consumable<String>> = _syncResult

    private val _showProgressBar: MutableLiveData<Boolean> = MutableLiveData()
    val showProgressBar: LiveData<Boolean> = _showProgressBar

    var sortingOrder: Int = generalSettings.getInt("formChooserListSortingOrder")
        get() { return generalSettings.getInt("formChooserListSortingOrder") }

        set(value) {
            field = value
            generalSettings.save("formChooserListSortingOrder", value)
            sortAndFilter()
        }

    var filterText: String = ""
        set(value) {
            field = value
            sortAndFilter()
        }

    init {
        viewModelScope.launch {
            _showProgressBar.value = true
            loadFromDatabase()
            syncWithStorage()
            _showProgressBar.value = false
        }
    }

    private fun loadFromDatabase() {
        _allForms.value = formsRepository
            .all
            .map { form ->
                FormListItem(
                    databaseId = form.dbId,
                    formId = form.dbId,
                    formName = form.displayName,
                    formVersion = form.version ?: "",
                    geometryPath = form.geometryXpath ?: "",
                    dateOfCreation = form.date,
                    dateOfLastUsage = 0,
                    contentUri = FormsContract.getUri(projectId, form.dbId)
                )
            }
            .toList()

        sortAndFilter()
    }

    private fun syncWithStorage() {
        changeLockProvider.getFormLock(projectId).withLock {
            val result = FormsDirDiskFormsSynchronizer().synchronizeAndReturnError()
            loadFromDatabase()
            _syncResult.value = Consumable(result)
        }
    }

    fun syncWithServer(): LiveData<Boolean> {
        logManualSyncWithServer()
        val result = MutableLiveData<Boolean>()
        scheduler.immediate(
            { formsUpdater.matchFormsWithServer(projectId) },
            { value: Boolean -> result.setValue(value) }
        )
        return result
    }

    fun isMatchExactlyEnabled(): Boolean {
        return SettingsUtils.getFormUpdateMode(
            application,
            generalSettings
        ) == FormUpdateMode.MATCH_EXACTLY
    }

    fun isSyncingWithServer(): LiveData<Boolean> {
        return syncRepository.isSyncing(projectId)
    }

    fun isOutOfSyncWithServer(): LiveData<Boolean> {
        return Transformations.map(
            syncRepository.getSyncError(projectId)
        ) { obj: FormSourceException? ->
            obj != null
        }
    }

    fun isAuthenticationRequired(): LiveData<Boolean> {
        return Transformations.map(
            syncRepository.getSyncError(projectId)
        ) { error: FormSourceException? ->
            if (error != null) {
                error is AuthRequired
            } else {
                false
            }
        }
    }

    private fun logManualSyncWithServer() {
        val uri = Uri.parse(generalSettings.getString(ProjectKeys.KEY_SERVER_URL))
        val host = if (uri.host != null) uri.host else ""
        val urlHash = getMd5Hash(ByteArrayInputStream(host!!.toByteArray())) ?: ""
        analytics.logEvent(AnalyticsEvents.MATCH_EXACTLY_SYNC, "Manual", urlHash)
    }

    private fun sortAndFilter() {
        _formsToDisplay.value = when (sortingOrder) {
            0 -> _allForms.value.sortedBy { it.formName }
            1 -> _allForms.value.sortedByDescending { it.formName }
            2 -> _allForms.value.sortedBy { it.dateOfCreation }
            3 -> _allForms.value.sortedByDescending { it.dateOfCreation }
            else -> { _allForms.value }
        }.filter {
            filterText.isBlank() || it.formName.contains(filterText, true)
        }
    }

    class Factory(
        private val formsRepository: FormsRepository,
        private val application: Application,
        private val syncRepository: SyncStatusAppState,
        private val formsUpdater: FormsUpdater,
        private val scheduler: Scheduler,
        private val generalSettings: Settings,
        private val analytics: Analytics,
        private val changeLockProvider: ChangeLockProvider,
        private val projectId: String
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FormListViewModel(
                formsRepository,
                application,
                syncRepository,
                formsUpdater,
                scheduler,
                generalSettings,
                analytics,
                changeLockProvider,
                projectId
            ) as T
        }
    }
}
